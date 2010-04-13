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
public class ValidFromDate extends GCDate {

    private final static Log log = LogFactory.getLog(ValidFromDate.class);

    private static DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyyMMdd");

    public ValidFromDate(String validFrom) {
        super(validFrom);
    }

    /**
     * Use the static factory method to get a default value object.
     * @param timeZone
     */
    private ValidFromDate(TimeZone timeZone) {
        super(timeZone);
    }

    /**
     * A static factory method to create a default ValidFromDate
     * @param timeZone the time zone to use when creating the default date.
     * @return the default date
     */
    public static ValidFromDate getDefaultValidFromDate(TimeZone timeZone) {
        return new ValidFromDate(timeZone);
    }

    protected long parseStr(String dateStr) {
        try {
            DateTime date = FMT.parseDateTime(dateStr);
            return date.dayOfMonth().withMinimumValue().toDateMidnight().getMillis();
        } catch (IllegalArgumentException e) {
            log.warn("parseStr() Caught IllegalArgumentException: " + e.getMessage());
            return defaultDate(TimeZone.getTimeZone("UTC"));
        }
    }

    protected void setDefaultDateStr() {
        this.dateStr = FMT.print(this.getTime());
    }

    @Override
    protected long defaultDate(TimeZone timeZone) {
        DateMidnight startOfMonth = new DateMidnight(DateTimeZone.forTimeZone(timeZone)).withDayOfMonth(1);
        return startOfMonth.getMillis();
    }

    public static boolean validate(String dateStr) {
        try {
            FMT.parseDateTime(dateStr);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }
}