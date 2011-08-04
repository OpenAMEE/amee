package com.amee.restlet.profile.builder.v2;

import com.amee.platform.science.StartEndDate;


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
        builder.append("<abbr class=\"" + clazz + "\" title=\"");
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