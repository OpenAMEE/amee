package com.amee.domain.profile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
public class ProfileDate extends GCDate {

    private final static Log log = LogFactory.getLog(ProfileDate.class);

    private static final DateTimeFormatter MONTH_DATE = DateTimeFormat.forPattern("yyyyMM");

    public ProfileDate() {
        super(System.currentTimeMillis());
    }

    // Called if profileDate is null (empty)
    public ProfileDate(String profileDate) {
        super(profileDate);
    }

    // Default time in the given time zone
    public ProfileDate(TimeZone timeZone) {
        super(System.currentTimeMillis());
        setTime(defaultDate(timeZone));
        setDefaultDateStr();
    }

    protected long parseStr(String dateStr) {
        try {
            return MONTH_DATE.parseDateTime(dateStr).getMillis();
        } catch (IllegalArgumentException e) {
            log.warn("parseStr() Caught IllegalArgumentException: " + e.getMessage());
            return defaultDate(TimeZone.getTimeZone("UTC"));
        }
    }

    protected long defaultDate(TimeZone timeZone) {
        // Beginning of current month in the given time zone.
        DateMidnight startOfMonth = new DateMidnight(DateTimeZone.forTimeZone(timeZone)).withDayOfMonth(1);
        return startOfMonth.getMillis();
    }

    public static boolean validate(String dateStr) {
        try {
            MONTH_DATE.parseDateTime(dateStr);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }

    protected void setDefaultDateStr() {
        this.dateStr = MONTH_DATE.print(this.getTime());
    }
}
