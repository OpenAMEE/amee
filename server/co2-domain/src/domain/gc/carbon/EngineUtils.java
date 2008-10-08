/**
* This file is part of AMEE.
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
package gc.carbon;

import gc.carbon.path.Pathable;

import javax.persistence.Transient;
import java.util.Date;
import java.util.Calendar;
import java.text.ParseException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;

public class EngineUtils {

    private static final String MONTHLY_DATE = "yyyyMM";
    private static final String FULL_DATE = "yyyyMMDD";
    private static final String ISO_DATE = "yyyyMMdd'T'HHmm";
    private static final String[] ALLOWABLE_DATE_FORMATS = new String[]{ISO_DATE, FULL_DATE};

    public static String getDisplayPath(Pathable p) {
        if (p.getPath().length() > 0) {
            return p.getPath();
        } else {
            return p.getUid();
        }
    }

    public static String getDisplayName(Pathable p) {
        if (p.getName().length() > 0) {
            return p.getName();
        } else {
            return p.getDisplayPath();
        }
    }

    @Transient
    public static String getMonthlyDate(Date date) {
        return DateFormatUtils.format(date, MONTHLY_DATE);
    }

    @Transient
    public static String getFullDate(Date date) {
        return DateFormatUtils.format(date, FULL_DATE);
    }

    @Transient
    public static String getISODate(Date date) {
        return DateFormatUtils.format(date, ISO_DATE);
    }

    @Transient
    public static Date getFullDate(String date) {
        if (date == null)
            return defaultDate();

        try {
            // Parse the date string acccording to the allowed formats.
            // If successful, return a Date with the minute field floored to the nearest 30min.
            // If a ParseException occurs, return the defaultDate.
            DateTime dt = new DateTime(DateUtils.parseDate(date,ALLOWABLE_DATE_FORMATS));
            return dt.withMinuteOfHour( (dt.getMinuteOfHour() < 30) ? 0 : 30).toDate();          

        } catch (ParseException e) {
            return defaultDate();
        }
    }

    private static Date defaultDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.clear();
        cal.set(year, month, 1);
        return cal.getTime();
    }
}
