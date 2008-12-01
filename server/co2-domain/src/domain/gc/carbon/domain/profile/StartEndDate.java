package gc.carbon.domain.profile;

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.ISOPeriodFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

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

    public static final String ISO_DATE = "yyyyMMdd'T'HHmm";

    public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat(ISO_DATE);

    public StartEndDate(String dateStr) {
        super(dateStr);
    }

    public StartEndDate(Date date) {
        super(date.getTime());
    }
    
    // Parse the date string acccording to the allowed formats.
    // If successful, return a Date with the minute field floored to the nearest 30min.
    // If a ParseException occurs, return the defaultDate.
    protected long parseStr(String dateStr) {
        try {
            DateTime requestedDate = new DateTime(ISO_DATE_FORMAT.parse(dateStr));
            DateTime dateFlooredToPreceeding30Mins = requestedDate.withMinuteOfHour( (requestedDate.getMinuteOfHour() < 30) ? 0 : 30);
            return dateFlooredToPreceeding30Mins.toDate().getTime();
        } catch (ParseException e) {
            return defaultDate();
        }
    }

    protected void setDefaultDateStr() {
        this.dateStr = ISO_DATE_FORMAT.format(this);
    }

    protected long defaultDate() {
        return new Date().getTime();
    }

    public StartEndDate plus(String duration) {
        Period period = ISOPeriodFormat.standard().parsePeriod(duration);
        DateTime thisPlusPeriod = new DateTime(getTime()).plus(period);
        return new StartEndDate(thisPlusPeriod.toDate());
    }
}
