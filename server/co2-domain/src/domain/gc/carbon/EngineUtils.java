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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EngineUtils {

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
        DateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        return dateFormat.format(date);
    }

    @Transient
    public static Date getMonthlyDate(String date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMM");
            try {
                return dateFormat.parse(date);
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Transient
    public static String getFullDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    @Transient
    public static Date getFullDate(String date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            try {
                return dateFormat.parse(date);
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
