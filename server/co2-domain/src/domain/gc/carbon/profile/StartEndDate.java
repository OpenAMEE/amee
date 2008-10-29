package gc.carbon.profile;

import org.joda.time.DateTime;
import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
public class StartEndDate extends GCDate {

    // Support legacy validFrom date formats.
    private static final String DAY_DATE = "yyyyMMdd";
    private static final String ISO_DATE = "yyyyMMdd'T'HHmm";
    private static final String[] ALLOWABLE_DATE_FORMATS = new String[]{ISO_DATE, DAY_DATE};

    public StartEndDate(String dateStr) {
        super(dateStr);
    }

    // Parse the date string acccording to the allowed formats.
    // If successful, return a Date with the minute field floored to the nearest 30min.
    // If a ParseException occurs, return the defaultDate.
    protected long parseStr(String dateStr) {
        try {
            DateTime requestedDate = new DateTime(DateUtils.parseDate(dateStr,ALLOWABLE_DATE_FORMATS));
            DateTime dateFlooredToNearest30Mins = requestedDate.withMinuteOfHour( (requestedDate.getMinuteOfHour() < 30) ? 0 : 30);
            return dateFlooredToNearest30Mins.toDate().getTime();
        } catch (ParseException e) {
            return defaultDate();
        }
    }

}
