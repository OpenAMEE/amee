package gc.carbon.profile;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import com.jellymold.utils.domain.APIUtils;

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
public class ProfileDateTime {

    public static final DateTimeFormatter MONTH = DateTimeFormat.forPattern("yyyyMM");
    public static final DateTimeFormatter FULL = DateTimeFormat.forPattern("yyyyMMdd");
    public static final DateTimeFormatter ISO = DateTimeFormat.forPattern("yyyyMMdd'T'HHmm");

    private String name = "ProfileDateTime";
    private DateTime dt;
    private DateTimeFormatter fmt;

    public ProfileDateTime() {
        this.dt = new DateTime();
    }
   
    public ProfileDateTime(String dtstr, String name, DateTimeFormatter fmt) {
        this.dt = getDateTime(dtstr,fmt);
        this.name = name;
        this.fmt = fmt;
    }

    /*
        Parse a date string acccording to the allowed formats.
        If successful, return a Date with the minute field floored to the nearest 30min.
        If a ParseException occurs, return the defaultDate.
    */
    private DateTime getDateTime(String date, DateTimeFormatter fmt) {
        if (date == null)
            return defaultDate();

        try {
            DateTime dt = fmt.parseDateTime(date);
            return dt.withMinuteOfHour( (dt.getMinuteOfHour() < 30) ? 0 : 30);
        } catch (IllegalArgumentException  e) {
            return defaultDate();
        }
    }

    private DateTime defaultDate() {
        return new DateTime().dayOfMonth().withMinimumValue();
    }

    public void setOutputFormatTo(DateTimeFormatter fmt) {
        this.fmt = fmt;
    }

    public Date toDate() {
        return dt.toDate();    
    }

    public String toString() {
        return dt.toString(fmt);
    }

    public String getName() {
        return name;    
    }

    public Element toXML(Document document) {
        String elementName = name.substring(0,1).toUpperCase() + name.substring(1, name.length());
        return APIUtils.getElement(document, elementName, dt.toString(fmt));
    }
}
