package com.amee.domain.profile;

import com.amee.domain.TimeZoneHolder;
import com.amee.platform.science.StartEndDate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    public ProfileDate(String profileDate) {
        super(profileDate);
    }

    protected long parseStr(String dateStr) {
        try {
            return MONTH_DATE.parseDateTime(dateStr).getMillis();
        } catch (IllegalArgumentException e) {
            log.warn("parseStr() Caught IllegalArgumentException: " + e.getMessage());
            return defaultDate();
        }
    }

    protected long defaultDate() {
        // Beginning of current month in the user's time zone.
        TimeZone timeZone = TimeZoneHolder.getTimeZone();
        return StartEndDate.getStartOfMonthDate(timeZone).getTime();
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
