package com.amee.domain.profile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    // V1 API does not support resolution less than 1 month so time zones are not valid.
    private static DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);

    public ValidFromDate(String validFrom) {
        super(validFrom);
    }

    protected long parseStr(String dateStr) {
        try {
            DateTime date = FMT.parseDateTime(dateStr);
            // v1 dates are always rounded down to the start of the month.
            return date.dayOfMonth().withMinimumValue().toDateMidnight().getMillis();
        } catch (IllegalArgumentException e) {
            log.warn("parseStr() Caught IllegalArgumentException: " + e.getMessage());
            return defaultDate();
        }
    }

    protected void setDefaultDateStr() {
        this.dateStr = FMT.print(this.getTime());
    }

    @Override
    protected long defaultDate() {
        // Beginning of current month in UTC.
        DateMidnight startOfMonth = new DateMidnight(DateTimeZone.UTC).withDayOfMonth(1);
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