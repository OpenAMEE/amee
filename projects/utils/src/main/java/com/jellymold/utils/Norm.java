package com.jellymold.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Norm {

    public final static Pattern ALNUM_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    public static String varName(String value) {
        return alnum(value, false, false, true);
    }

    public static String alnum(String value) {
        return alnum(value, false, false, false);
    }

    public static String alnum(String value, boolean lowerCase, boolean upperCase, boolean dropFirst) {
        String result = "";
        if (value != null) {
            if (lowerCase) {
                result = value.toLowerCase();
            } else if (upperCase) {
                result = value.toUpperCase();
            } else {
                result = value;
            }
            Matcher matcher = ALNUM_PATTERN.matcher(result);
            result = matcher.replaceAll("");
        }
        if (dropFirst && (result.length() > 0)) {
            result = result.substring(0, 1).toLowerCase() + result.substring(1);
        }
        return result;
    }
}
