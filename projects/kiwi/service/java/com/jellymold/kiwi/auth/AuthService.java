package com.jellymold.kiwi.auth;

import com.jellymold.kiwi.*;
import com.jellymold.utils.ThreadBeanHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Scope;

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

    // TODO: SPRINGIFY
    @Autowired(required = false)
    // @Out(scope = ScopeType.EVENT, required = false)
    private User user;

    // TODO: SPRINGIFY
    @Autowired(required = false)
    // @Out(scope = ScopeType.EVENT, required = false)
    private List<GroupUser> groupUsers;

    // TODO: SPRINGIFY
    @Autowired(required = false)
    // @Out(scope = ScopeType.EVENT, required = false)
    private Group group;

    @Autowired(required = false)
    private Permission permission;

    public User doGuestSignIn() {
        signOut();
        user = getUserByUsername("guest");
        if (user != null) {
            log.debug("Guest user authenticated and signed in");
            getAndExportGroups();
            return user;
        } else {
            log.warn("Guest user NOT authenticated and NOT signed in");
            return null;
        }
    }

    public String isAuthenticated(String authToken, String remoteAddress) {

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
            log.info("authToken supplied");

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
                            log.info("remote address check passed: " + remoteAddress);
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
                log.info("user NOT authenticated, remote address check failed: " + remoteAddress);
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
                log.info("user NOT authenticated, max auth duration check failed");
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
                log.info("user NOT authenticated, max auth idle check failed");
                return null;
            }

            // get and check user
            String userUid = values.get(AuthToken.USER_UID);
            if (userUid != null) {
                user = getUserByUid(userUid);
                if (user != null) {
                    log.info("user authenticated and signed in: " + user.getUsername());
                    getAndExportGroups();
                    Long touched = new Long(values.get(AuthToken.MODIFIED));
                    // only touch token if older than 60 seconds (60*1000ms)
                    if (now > (touched + 60 * 1000)) {
                        return AuthToken.touchToken(authToken);
                    } else {
                        return oldAuthToken;
                    }
                }
            }

        } else {
            log.info("authToken NOT supplied");
        }

        log.info("user NOT authenticated");
        return null;
    }

    public String authenticate(User sampleUser, String remoteAddress) {
        // signed out by default
        signOut();
        // try to find user based on 'sampleUser' User 'template'
        user = getUserByUsername(sampleUser.getUsername());
        if (user != null) {
            if (user.getPassword().equals(sampleUser.getPassword())) {
                log.info("user authenticated and signed in: " + sampleUser.getUsername());
                getAndExportGroups();
                return AuthToken.createToken(user, remoteAddress);
            } else {
                log.info("user NOT authenticated, bad password: " + sampleUser.getUsername());
                signOut();
                return null;
            }
        } else {
            log.info("user NOT authenticated, not found: " + sampleUser.getUsername());
            return null;
        }
    }

    public String switchToUser(User newUser, String remoteAddress) {
        // TODO: Springify
        // Contexts.getEventContext().set("user", newUser);
        user = newUser;
        groupUsers = null;
        group = null;
        getAndExportGroups();
        return AuthToken.createToken(user, remoteAddress);
    }

    public boolean isSuperUser() {
        return (user != null) && user.isSuperUser();
    }

    public boolean hasGroup(String name) {
        getAndExportGroups();
        for (GroupUser groupUser : groupUsers) {
            if (groupUser.getGroup().getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRoles(String roles) {
        boolean result = false;
        if (user != null) {
            if (user.isSuperUser()) {
                result = true;
            } else {
                if (roles != null) {
                    getAndExportGroups();
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
        if (user != null) {
            if (user.isSuperUser()) {
                result = true;
            } else {
                if (actions != null) {
                    getAndExportGroups();
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
        if (permission != null) {
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
        if (permission != null) {
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
                "SELECT DISTINCT gm " +
                        "FROM GroupUser gm " +
                        "LEFT JOIN FETCH gm.user u " +
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
        user = null;
        groupUsers = null;
        group = null;
    }

    public User getUser() {
        return user;
    }

    // TODO: better way to select default group
    public void getAndExportGroups() {
        if (user != null) {
            if (groupUsers == null || groupUsers.size() == 0) {
                groupUsers = getGroupUsersByUser(user);
                if (groupUsers.size() > 0) {
                    group = groupUsers.get(0).getGroup();
                } else {
                    group = null;
                }
                user.setGroupNames(getGroupNames(user));
            }
        }
    }
}