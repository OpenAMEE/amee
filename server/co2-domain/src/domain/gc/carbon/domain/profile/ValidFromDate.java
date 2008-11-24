package gc.carbon.domain.profile;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    private static DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyyMMdd");

    public ValidFromDate(String profileDate) {
        super(profileDate);
    }

    protected long parseStr(String dateStr) {
        try {
            DateTime date = FMT.parseDateTime(dateStr);
            return date.dayOfMonth().withMinimumValue().toDateMidnight().getMillis();
        } catch (Exception ex) {
            return defaultDate();
        }
    }

    protected long defaultDate() {
        DateTime dt = new DateTime();
        return dt.dayOfMonth().withMinimumValue().getMillis();
    }

    protected void setDefaultDateStr() {
        this.dateStr = FMT.print(this.getTime());
    }
}