package gc.carbon.profile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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

    private String profileDate;

    private static final DateFormat MONTH_DATE = new SimpleDateFormat("yyyyMM");

    public ProfileDate(String profileDate) {
        super(profileDate);
        this.profileDate = profileDate;
    }

    protected long parseStr(String dateStr) {
        try {
            return MONTH_DATE.parse(dateStr).getTime();
        } catch (Exception ex) {
            return defaultDate();
        }
    }

    public String toString() {
        return profileDate;     
    }
}
