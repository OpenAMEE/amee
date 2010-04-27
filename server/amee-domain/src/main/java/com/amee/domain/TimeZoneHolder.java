package com.amee.domain;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.auth.User;

import java.util.TimeZone;

/**
 * A thread local helper for retrieving the TimeZone of the request
 */
public class TimeZoneHolder extends ThreadBeanHolder {

    /**
     * Get the time zone of the current request thread. This will be the {@link com.amee.domain.auth.User} timeZone.
     *
     * @return - the TimeZone of the current request thread.
     */
    public static TimeZone getTimeZone() {
        User currentUser = (User) get("activeUser");
        return currentUser.getTimeZone();
    }
}
