package com.amee.service.auth;

import com.amee.domain.AMEEStatus;
import com.amee.domain.environment.Environment;
import com.amee.domain.auth.User;
import com.amee.domain.auth.crypto.Crypto;
import com.amee.domain.auth.crypto.CryptoException;
import com.amee.domain.site.ISite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthenticationService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.authenticationService";

    public static final String AUTH_TOKEN = "authToken";

    @PersistenceContext
    private EntityManager entityManager;

    public User doGuestSignIn(Environment environment) {
        return getUserByUsername(environment, "guest");
    }

    public String isAuthenticated(Environment environment, ISite site, String authToken, String remoteAddress) {

        User activeUser;
        Map<String, String> values;
        boolean remoteAddressCheckPassed = false;
        boolean maxAuthDurationCheckPassed = false;
        boolean maxAuthIdleCheckPassed = false;
        long now = Calendar.getInstance().getTimeInMillis();
        String oldAuthToken;

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
                activeUser = getUserByUid(environment, userUid);
                if (activeUser != null) {
                    log.debug("auth authenticated and signed in: " + activeUser.getUsername());
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
            log.debug("authToken NOT supplied");
        }

        log.debug("auth NOT authenticated");
        return null;
    }

    /**
     * Get the current active user from the supplied AuthToken.
     *
     * @param authToken representing the active user.
     * @return the active user
     */
    public User getActiveUser(Environment environment, String authToken) {

        if (authToken == null) {
            throw new IllegalArgumentException("AuthToken String must not be null.");
        }

        // get authToken values
        authToken = AuthToken.decryptToken(authToken);
        Map<String, String> values = AuthToken.explodeToken(authToken);

        // get and check auth
        String userUid = values.get(AuthToken.USER_UID);
        if (userUid != null) {
            return getUserByUid(environment, userUid);
        } else {
            log.debug("getActiveUser() - active user NOT found");
            return null;
        }
    }

    /**
     * Authenticates based on the supplied sample user. The sample user must have an environment, username and
     * password set. If authentication is successful the persistent User is returned.
     *
     * @param sampleUser sample User to authenticate against
     * @return the authenticated User
     */
    public User authenticate(User sampleUser) {
        // try to find auth based on 'sampleUser' User 'template'
        User activeUser = getUserByUsername(sampleUser.getEnvironment(), sampleUser.getUsername());
        if (activeUser != null) {
            if (activeUser.getPassword().equals(sampleUser.getPassword())) {
                log.debug("authenticate() - auth authenticated and signed in: " + sampleUser.getUsername());
                return activeUser;
            } else {
                log.debug("authenticate() - auth NOT authenticated, bad password: " + sampleUser.getUsername());
                return null;
            }
        } else {
            log.debug("authenticate() - auth NOT authenticated, not found: " + sampleUser.getUsername());
            return null;
        }
    }

    public String generateAuthToken(User activeUser, String remoteAddress) {
        return AuthToken.createToken(activeUser, remoteAddress);
    }

    @SuppressWarnings(value = "unchecked")
    public User getUserByUid(Environment environment, String uid) {
        List<User> users = entityManager.createQuery(
                "SELECT DISTINCT u " +
                        "FROM User u " +
                        "WHERE u.environment.id = :environmentId " +
                        "AND u.uid = :userUid " +
                        "AND u.status != :trash")
                .setParameter("environmentId", environment.getId())
                .setParameter("userUid", uid)
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        if (users.size() == 1) {
            log.debug("auth found: " + uid);
            return users.get(0);
        }
        log.debug("auth NOT found: " + uid);
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public User getUserByUsername(Environment environment, String username) {
        List<User> users = entityManager.createQuery(
                "SELECT DISTINCT u " +
                        "FROM User u " +
                        "WHERE u.environment.id = :environmentId " +
                        "AND u.username = :username " +
                        "AND u.status != :trash")
                .setParameter("environmentId", environment.getId())
                .setParameter("username", username)
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        if (users.size() == 1) {
            log.debug("auth found: " + username);
            return users.get(0);
        }
        log.debug("auth NOT found: " + username);
        return null;
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