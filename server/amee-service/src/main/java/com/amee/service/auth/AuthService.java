package com.amee.service.auth;

import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupUser;
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.User;
import com.amee.domain.auth.crypto.Crypto;
import com.amee.domain.auth.crypto.CryptoException;
import com.amee.domain.site.Site;
import com.amee.service.ThreadBeanHolder;
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
            log.debug("Guest auth authenticated and signed in");
            ThreadBeanHolder.set("user", user);
            getAndExportGroups();
            return user;
        } else {
            log.warn("Guest auth NOT authenticated and NOT signed in");
            return null;
        }
    }

    public void reset() {
        ThreadBeanHolder.set("auth", null);
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
                log.debug("auth NOT authenticated, remote address check failed: " + remoteAddress);
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
                log.debug("auth NOT authenticated, max auth duration check failed");
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
                log.debug("auth NOT authenticated, max auth idle check failed");
                return null;
            }

            // get and check auth
            String userUid = values.get(AuthToken.USER_UID);
            if (userUid != null) {
                user = getUserByUid(userUid);
                if (user != null) {
                    log.debug("auth authenticated and signed in: " + user.getUsername());
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

        log.debug("auth NOT authenticated");
        return null;
    }

    public boolean authenticate(User sampleUser) {
        // signed out by default
        reset();
        signOut();
        // try to find auth based on 'sampleUser' User 'template'
        User user = getUserByUsername(sampleUser.getUsername());
        if (user != null) {
            if (user.getPassword().equals(sampleUser.getPassword())) {
                log.debug("authenticate() - auth authenticated and signed in: " + sampleUser.getUsername());
                ThreadBeanHolder.set("user", user);
                getAndExportGroups();
                return true;
            } else {
                log.debug("authenticate() - auth NOT authenticated, bad password: " + sampleUser.getUsername());
                return false;
            }
        } else {
            log.debug("authenticate() - auth NOT authenticated, not found: " + sampleUser.getUsername());
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
        ThreadBeanHolder.set("auth", newUser);
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

    @SuppressWarnings(value = "unchecked")
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
            log.debug("auth found: " + userUid);
            return users.get(0);
        }
        log.debug("auth NOT found: " + userUid);
        return null;
    }

    @SuppressWarnings(value = "unchecked")
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
            log.debug("auth found: " + username);
            return users.get(0);
        }
        log.debug("auth NOT found: " + username);
        return null;
    }

    @SuppressWarnings(value = "unchecked")
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

    @SuppressWarnings(value = "unchecked")
    public static List<GroupUser> getGroupUsers() {
        return (List<GroupUser>) ThreadBeanHolder.get("groupUsers");
    }

    public static Permission getPermission() {
        return (Permission) ThreadBeanHolder.get("permission");
    }

    private static class AuthToken implements Serializable {

        public final static String ENVIRONMENT_UID = "en";
        public final static String USER_UID = "us";
        public final static String REMOTE_ADDRESS_HASH = "ra";
        public final static String CREATED = "cr";
        public final static String MODIFIED = "mo";

        public static Map<String, String> explodeToken(String token) {
            String[] pairsArr;
            String[] pairArr;
            String name;
            String value;
            Map<String, String> values = new HashMap<String, String>();
            pairsArr = token.split("\\|"); // | is the delimiter in this regex
            for (String pair : pairsArr) {
                pairArr = pair.split("=");
                if (pairArr.length > 0) {
                    name = pairArr[0];
                    if (pairArr.length > 1) {
                        value = pairArr[1];
                    } else {
                        value = "";
                    }
                    values.put(name, value);
                }
            }
            return values;
        }

        public static String implodeToken(Map<String, String> values) {
            String token = "";
            for (String name : values.keySet()) {
                if (token.length() > 0) {
                    token += "|";
                }
                token += name + "=" + values.get(name);
            }
            return token;
        }

        public static String createToken(User user, String remoteAddress) {
            String now = "" + Calendar.getInstance().getTimeInMillis();
            Map<String, String> values = new HashMap<String, String>();
            values.put(ENVIRONMENT_UID, user.getEnvironment().getUid());
            values.put(USER_UID, user.getUid());
            values.put(REMOTE_ADDRESS_HASH, "" + remoteAddress.hashCode());
            values.put(CREATED, now);
            values.put(MODIFIED, now);
            return encryptToken(implodeToken(values));
        }

        public static String touchToken(String token) {
            String now = "" + Calendar.getInstance().getTimeInMillis();
            Map<String, String> values = explodeToken(token);
            values.put(MODIFIED, now);
            return encryptToken(implodeToken(values));
        }

        public static String decryptToken(String token) {
            try {
                return Crypto.decrypt(token);
            } catch (CryptoException e) {
                // log.error("caught CryptoException: " + e);
                // TODO: do something now
                return "";
            }
        }

        public static String encryptToken(String token) {
            try {
                return Crypto.encrypt(token);
            } catch (CryptoException e) {
                // log.error("caught CryptoException: " + e);
                // TODO: do something now
                return "";
            }
        }
    }
}
