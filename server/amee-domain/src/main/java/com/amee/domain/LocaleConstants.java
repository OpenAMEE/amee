package com.amee.domain;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides constants for Locale related functionality.
 */
public abstract class LocaleConstants {

    // The default {@link Locale} within AMEE.
    // Typical use would be for initialising a new User locale.
    public static final Locale DEFAULT_LOCALE = Locale.UK;

    // The static map of available {@link Locale}.
    public static final Map<String, Locale> AVAILABLE_LOCALES = initLocales();

    private static Map<String, Locale> initLocales() {
        Map<String, Locale> localeMap = new TreeMap<String, Locale>();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale l : locales) {
            localeMap.put(l.toString(), l);
        }
        return localeMap;
    }
}
