/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.domain;

import com.amee.base.utils.ThreadBeanHolder;

/**
 * A thread local helper for retrieving the locale string of the request
 */
public class LocaleHolder extends ThreadBeanHolder {

    public static final String KEY = "locale";

    /**
     * Does the current request thread have the default locale.
     *
     * @return - true if the current request has the default locale, otherwise false
     *
     * {@see LocaleName.DEFAULT_LOCALE}
     */
    public static boolean isDefaultLocale() {
        String locale = (String) get(KEY);
        return LocaleConstants.DEFAULT_LOCALE.toString().equals(locale);
    }

    /**
     * Get the locale of the current request thread. This will be the {@link com.amee.domain.auth.User} locale or a
     * locale specified as a request override.  
     *  
     * @return - the locale string of the current request thread.
     */
    public static String getLocale() {
        return (String) get(KEY);    
    }
}
