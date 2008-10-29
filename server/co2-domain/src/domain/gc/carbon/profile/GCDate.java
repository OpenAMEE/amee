package gc.carbon.profile;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

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
public abstract class GCDate extends java.util.Date {

    public GCDate(String dateStr) {
        super();
        setTime(parseStr(dateStr));
    }

    protected abstract long parseStr(String dateStr);

    protected long defaultDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.clear();
        cal.set(year, month, 1);
        return cal.getTimeInMillis();
    }

    public Element toXML(Document document) {
        //String elementName = name.substring(0,1).toUpperCase() + name.substring(1, name.length());
        //return APIUtils.getElement(document, elementName, dt.toString(fmt));
        return null;
    }
}
