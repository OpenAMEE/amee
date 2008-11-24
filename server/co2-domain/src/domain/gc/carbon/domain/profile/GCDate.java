package gc.carbon.domain.profile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
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
public abstract class GCDate extends java.util.Date {

    protected String dateStr;

    public GCDate(long time) {
        setTime(time);
        setDefaultDateStr();
    }

    public GCDate(String dateStr) {
        super();
        if (dateStr != null) {
            setTime(parseStr(dateStr));
            this.dateStr = dateStr;
        } else {
            setTime(defaultDate());
            setDefaultDateStr();
        }
    }

    protected abstract long parseStr(String dateStr);

    protected abstract void setDefaultDateStr();

    protected long defaultDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.clear();
        cal.set(year, month, 1);
        return cal.getTimeInMillis();
    }

    public String toString() {
        return dateStr;    
    }

    public Date toDate() {
        return new DateTime(this.getTime()).toDate();
    }
}
