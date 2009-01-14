package com.jellymold.kiwi.auth;

import com.jellymold.kiwi.*;
import com.jellymold.utils.ThreadBeanHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.*;

@Service
@Scope("prototype")
public class AuthService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    public final static String AUTH_TOKEN = "authToken";

    @PersistenceContext
    private EntityManager entityManager;

    public User doGuestSignIn() {
        signOut();
        reset();
        User user = getUserByUsername("guest");
        if (user != null) {
            log.debug("Guest user authenticated and signed in");
            ThreadBeanHolder.set("user", user);
            getAndExportGroups();
            return user;
        } else {
            log.warn("Guest user NOT authenticated and NOT signed in");
            return null;
        }
    }

    public void reset() {
        ThreadBeanHolder.set("user", null);
        ThreadBeanHolder.set("groupUsers", null);
        ThreadBeanHolder.set("group", null);
        ThreadBeanHolder.set("permission", null);
    }

    public String isAuthenticated(String authToken, String remoteAddress) {

        User user;
        Map<String, String> values;
        boolean remoteAddressCheckPassed = false;
        boolean maxAuthDurationCheckPassed = false;
        boolean maxAuthIdleCheckPassed = false;
        long now = Calendar.getInstance().getTimeInMillis();
        String oldAuthToken;
        Site site = (Site) ThreadBeanHolder.get("site");

        // signed out by default
        signOut();

        // has authToken been supplied?
        if (authToken != null) {

            log.debug("authToken supplied");

            // must have a Site object
            if (site == null) {
                log.error("Site object missing.");
                throw new RuntimeException("Site object missing.");
            }

            // get authToken values
            oldAuthToken = authToken;
            authToken = AuthToken.decryptToken(authToken);
            values = AuthToken.explodeToken(authToken);

            // check remote address
            if (site.isCheckRemoteAddress()) {
                String remoteAddressHash = values.get(AuthToken.REMOTE_ADDRESS_HASH);
                if (remoteAddressHash != null) {
                    try {
                        if (remoteAddress.hashCode() == Integer.valueOf(remoteAddressHash)) {
                            log.debug("remote address check passed: " + remoteAddress);
                            remoteAddressCheckPassed = true;
                        }
                    } catch (NumberFormatException e) {
                        // swallow
                    }
                }
            } else {
                // ignore remote address check
                remoteAddressCheckPassed = true;
            }
            if (!remoteAddressCheckPassed) {
                log.debug("user NOT authenticated, remote address check failed: " + remoteAddress);
                return null;
            }

            // check auth duration
            if (site.getMaxAuthDuration() >= 0) {
                try {
                    Long created = new Long(values.get(AuthToken.CREATED));
                    maxAuthDurationCheckPassed = (created + site.getMaxAuthDuration() > now);
                } catch (NumberFormatException e) {
                    // swallow
                }
            } else {
                // ignore max auth duration check
                maxAuthDurationCheckPassed = true;
            }
            if (!maxAuthDurationCheckPassed) {
                log.debug("user NOT authenticated, max auth duration check failed");
                return null;
            }

            // check auth idle
            if (site.getMaxAuthIdle() >= 0) {
                try {
                    Long touched = new Long(values.get(AuthToken.MODIFIED));
                    maxAuthIdleCheckPassed = (touched + site.getMaxAuthIdle() > now);
                } catch (NumberFormatException e) {
                    // swallow
                }
            } else {
                // ignore max auth idle check
                maxAuthIdleCheckPassed = true;
            }
            if (!maxAuthIdleCheckPassed) {
                log.debug("user NOT authenticated, max auth idle check failed");
                return null;
            }

            // get and check user
            String userUid = values.get(AuthToken.USER_UID);
            if (userUid != null) {
                user = getUserByUid(userUid);
                if (user != null) {
                    log.debug("user authenticated and signed in: " + user.getUsername());
                    ThreadBeanHolder.set("user", user);
                    getAndExportGroups();
                    Long touched = new Long(values.get(AuthToken.MODIFIED));
                    // only touch token if older than 60 seconds (60*1000ms)
                    if (now > (touched + 60 * 1000)) {
                        return AuthToken.touchToken(authToken);
                    } else {
                        return oldAuthToken;
                    }
                } else {
                    reset();
                }
            }

        } else {
            log.debug("authToken NOT supplied");
        }

        log.debug("user NOT authenticated");
        return null;
    }

    public boolean authenticate(User sampleUser) {
        // signed out by default
        reset();
        signOut();
        // try to find user based on 'sampleUser' User 'template'
        User user = getUserByUsername(sampleUser.getUsername());
        if (user != null) {
            log.debug("comparing passwords: '" + user.getPassword() + "' = '" + sampleUser.getPassword() + "'");
            if (user.getPassword().equals(sampleUser.getPassword())) {
                log.debug("authenticate() - user authenticated and signed in: " + sampleUser.getUsername());
                ThreadBeanHolder.set("user", user);
                getAndExportGroups();
                return true;
            } else {
                log.debug("authenticate() - user NOT authenticated, bad password: " + sampleUser.getUsername());
                return false;
            }
        } else {
            log.debug("authenticate() - user NOT authenticated, not found: " + sampleUser.getUsername());
            return false;
        }
    }

    public String authenticateAndGenerateAuthToken(User sampleUser, String remoteAddress) {
        if (authenticate(sampleUser)) {
            return AuthToken.createToken((User) ThreadBeanHolder.get("user"), remoteAddress);
        } else {
            return null;
        }
    }

    public String switchToUser(User newUser, String remoteAddress) {
        reset();
        ThreadBeanHolder.set("user", newUser);
        getAndExportGroups();
        return AuthToken.createToken(newUser, remoteAddress);
    }

    public boolean isSuperUser() {
        User user = getUser();
        return (user != null) && user.isSuperUser();
    }

    public boolean hasGroup(String name) {
        List<GroupUser> groupUsers = getAndExportGroups();
        if (groupUsers != null) {
            for (GroupUser groupUser : groupUsers) {
                if (groupUser.getGroup().getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasRoles(String roles) {
        boolean result = false;
        List<GroupUser> groupUsers;
        User user = getUser();
        if (user != null) {
            if (user.isSuperUser()) {
                result = true;
            } else {
                if (roles != null) {
                    groupUsers = getAndExportGroups();
                    if (groupUsers != null) {
                        for (GroupUser groupUser : groupUsers) {
                            if (groupUser.hasRoles(roles)) {
                                result = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        log.debug("roles: " + roles + " result: " + result);
        return result;
    }

    public boolean hasActions(String actions) {
        boolean result = false;
        List<GroupUser> groupUsers;
        User user = getUser();
        if (user != null) {
            if (user.isSuperUser()) {
                result = true;
            } else {
                if (actions != null) {
                    groupUsers = getAndExportGroups();
                    if (groupUsers != null) {
                        for (GroupUser groupUser : groupUsers) {
                            if (groupUser.hasActions(actions)) {
                                result = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        log.debug("actions: " + actions + " result: " + result);
        return result;
    }

    public boolean isAllowView() {
        User user = getUser();
        Group group = getGroup();
        Permission permission = getPermission();
        if ((permission != null) && (user != null) && (group != null)) {
            getAndExportGroups();
            // check permission
            if (permission.getUser().equals(user)) {
                // matching User is always allowed
                return true;
            } else if (permission.isGroupAllowView() && permission.getGroup().equals(group)) {
                // matching Group is allowed to view
                return true;
            } else {
                // can others view?
                return permission.isOtherAllowView();
            }
        } else {
            // allow view by default - permission data not available
            return true;
        }
    }

    public boolean isAllowModify() {
        User user = getUser();
        Group group = getGroup();
        Permission permission = getPermission();
        if ((permission != null) && (user != null) && (group != null)) {
            getAndExportGroups();
            // check permission
            if (permission.getUser().equals(user)) {
                // matching User is always allowed
                return true;
            } else if (permission.isGroupAllowModify() && permission.getGroup().equals(group)) {
                // matching Group is allowed to modify
                return true;
            } else {
                // can others modify?
                return permission.isOtherAllowModify();
            }
        } else {
            // allow modify by default - permission data not available
            return true;
        }
    }

    public User getUserByUid(String userUid) {
        Site site = (Site) ThreadBeanHolder.get("site");
        List<User> users = entityManager.createQuery(
                "SELECT DISTINCT u " +
                        "FROM User u " +
                        "WHERE u.environment.id = :environmentId " +
                        "AND u.uid = :userUid")
                .setParameter("environmentId", site.getEnvironment().getId())
                .setParameter("userUid", userUid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.authService")
                .getResultList();
        if (users.size() == 1) {
            log.debug("user found: " + userUid);
            return users.get(0);
        }
        log.debug("user NOT found: " + userUid);
        return null;
    }

    public User getUserByUsername(String username) {
        Site site = (Site) ThreadBeanHolder.get("site");
        List<User> users = entityManager.createQuery(
                "SELECT DISTINCT u " +
                        "FROM User u " +
                        "WHERE u.environment.id = :environmentId " +
                        "AND u.username = :username")
                .setParameter("environmentId", site.getEnvironment().getId())
                .setParameter("username", username)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.authService")
                .getResultList();
        if (users.size() == 1) {
            log.debug("user found: " + username);
            return users.get(0);
        }
        log.debug("user NOT found: " + username);
        return null;
    }

    public List<GroupUser> getGroupUsersByUser(User user) {
        List<GroupUser> groupUsers = entityManager.createQuery(
                "SELECT DISTINCT gu " +
                        "FROM GroupUser gu " +
                        "LEFT JOIN FETCH gu.user u " +
                        "WHERE u.id = :userId")
                .setParameter("userId", user.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.authService")
                .getResultList();
        return groupUsers;
    }

    public Set<String> getGroupNamesByUser(User user) {
        Set<String> groupNames = new HashSet<String>();
        List<GroupUser> groupUsers = getGroupUsersByUser(user);
        for (GroupUser groupUser : groupUsers) {
            groupNames.add(groupUser.getGroup().getName());
        }
        return groupNames;
    }

    /**
     * Get list of Group names for given User in order of priority. Priority is from real Groups to the
     * virtual groups Standard, Anonymous and All in that sequence.
     */
    public List<String> getGroupNames(User user) {
        List<String> groupNames = new ArrayList<String>();
        if (!user.isGuestUser() && !user.isAnonymousUser()) {
            groupNames.addAll(getGroupNamesByUser(user));
        }
        if (user.isStandardUser()) {
            groupNames.add("Standard");
        }
        if (user.isAnonymousUser() || user.isGuestUser()) {
            groupNames.add("Anonymous");
        }
        groupNames.add("All");
        return groupNames;
    }

    public void signOut() {
        log.debug("signed out");
        reset();
    }

    // TODO: better way to select default group
    public List<GroupUser> getAndExportGroups() {

        Group group;
        List<GroupUser> groupUsers = null;
        User user = getUser();

        // reset
        ThreadBeanHolder.set("group", null);
        ThreadBeanHolder.set("groupUsers", null);

        // find GroupUsers and Group for current User
        if (user != null) {
            groupUsers = getGroupUsersByUser(user);
            ThreadBeanHolder.set("groupUsers", groupUsers);
            if (groupUsers.size() > 0) {
                group = groupUsers.get(0).getGroup();
                ThreadBeanHolder.set("group", group);
            }
            user.setGroupNames(getGroupNames(user));
        }

        return groupUsers;
    }

    public static User getUser() {
        return (User) ThreadBeanHolder.get("user");
    }

    public static Group getGroup() {
        return (Group) ThreadBeanHolder.get("group");
    }

    public static List<GroupUser> getGroupUsers() {
        return (List<GroupUser>) ThreadBeanHolder.get("groupUsers");
    }

    public static Permission getPermission() {
        return (Permission) ThreadBeanHolder.get("permission");
    }
}
