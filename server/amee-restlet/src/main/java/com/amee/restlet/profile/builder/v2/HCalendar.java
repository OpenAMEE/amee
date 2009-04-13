package com.amee.restlet.profile.builder.v2;

import com.amee.domain.StartEndDate;

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
public class HCalendar {

    private StringBuilder builder;

    public HCalendar() {
        builder = new StringBuilder("<div class=\"vevent\">");
    }

    public void addStartDate(StartEndDate date) {
        addDate(date, "dtstart");
    }

    public void addEndDate(StartEndDate date) {
        builder.append(" - ");
        addDate(date, "dtend");
    }

    private void addDate(StartEndDate date, String clazz) {
        builder.append("<abbr class=\""+ clazz +"\" title=\"");
        builder.append(date);
        builder.append("\"> ");
        builder.append(AtomFeed.getInstance().format(date));
        builder.append("</abbr>");
    }

    public void addSummary(String summary) {
        builder.append("<div class=\"summary\">");
        builder.append(summary);
        builder.append("</div>");

    }

    public String toString() {
        return builder.append("</div>").toString();
    }
}