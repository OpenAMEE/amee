package com.jellymold.kiwi.auth;

import com.jellymold.kiwi.User;
import com.jellymold.utils.crypto.Crypto;
import com.jellymold.utils.crypto.CryptoException;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthToken implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

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