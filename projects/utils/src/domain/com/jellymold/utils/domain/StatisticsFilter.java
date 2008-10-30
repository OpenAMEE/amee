package com.jellymold.utils.domain;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFilter implements Serializable {

    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public static final int DATE_INDEX = 0;
    public static final int YEAR_INDEX = 1;
    public static final int MONTH_INDEX = 2;
    public static final int YEARWEEK_INDEX = 3;
    public static final int HOUR_INDEX = 4;

    private String uid;
    private Date start = Calendar.getInstance().getTime();
    private boolean forward = false;
    private int calendarField = Calendar.DAY_OF_YEAR;
    private int intervals = 5;
    private String classNames = "";
    private String sortBy = "start";
    private String sortOrder = "ASC";
    private String showAs = "chart";
    private String path = "";

    public StatisticsFilter() {
        super();
        setUid(UidGen.getUid());
    }

    /**
     * Will return a Date based on current filter state and the date part results from a query.
     * Elements of results array are assumed to be from a Query like this:
     * <p/>
     * SELECT
     * DATE(CREATED) AS DATE,
     * YEAR(CREATED) AS YEAR,
     * MONTH(CREATED) AS MONTH,
     * YEARWEEK(CREATED,3) AS YEARWEEK,
     * HOUR(CREATED) AS HOUR,
     * etc.
     *
     * @param o
     * @return
     */
    public Date getDateFromQueryResults(Object[] o) {
        String yearWeek;
        Calendar cal = Calendar.getInstance();
        if (getCalendarField() == Calendar.HOUR_OF_DAY) {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setTime((Date) o[DATE_INDEX]);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.add(Calendar.HOUR_OF_DAY, (Integer) o[HOUR_INDEX]);
        } else if (getCalendarField() == Calendar.WEEK_OF_YEAR) {
            // TODO: not sure about this
            yearWeek = "" + o[YEARWEEK_INDEX]; // '200803'
            yearWeek = yearWeek.substring(4, 6); // '03'
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.YEAR, (Integer) o[YEAR_INDEX]);
            cal.set(Calendar.WEEK_OF_YEAR, Integer.valueOf(yearWeek) - 1); // '03' becomes 2
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else if (getCalendarField() == Calendar.MONTH) {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.MONTH, (Integer) o[MONTH_INDEX]);
            cal.set(Calendar.YEAR, (Integer) o[YEAR_INDEX]);
        } else {
            // default to DAY_OF_YEAR
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setTime((Date) o[DATE_INDEX]);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }
        return cal.getTime();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Calendar getStartCalendar() {
        // initialise calendar
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(getStart());
        startCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        // roll back to start of current period as defined in calendarField
        switch (getCalendarField()) {
            case Calendar.YEAR:
                startCalendar.set(Calendar.MONTH, Calendar.JANUARY);
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                break;
            case Calendar.MONTH:
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                break;
            case Calendar.WEEK_OF_YEAR:
                startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                break;
            case Calendar.DAY_OF_YEAR:
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                break;
            case Calendar.HOUR_OF_DAY:
            default:
                break;
        }
        return startCalendar;
    }

    public Boolean isFilteredByPath() {
        return path.length() > 0;
    }

    public String getUidSubQuery() {
        return ("SELECT b.uid FROM Board b WHERE b.path LIKE :SFPath");
    }

    public Map<String, String> getUidSubQueryParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("SFPath", getPathPattern());
        return parameters;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        if (start != null) {
            this.start = start;
        }
    }

    public void setStart(String startStr) {
        try {
            setStart(DATE_TIME_FORMAT.parse(startStr));
        } catch (ParseException e) {
            // swallow
        }
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public int getCalendarField() {
        return calendarField;
    }

    public void setCalendarField(int calendarField) {
        this.calendarField = calendarField;
    }

    public int getIntervals() {
        return intervals;
    }

    public void setIntervals(int intervals) {
        this.intervals = intervals;
    }

    public String getClassNames() {
        return classNames;
    }

    public List<String> getClassNamesAsList() {
        return new ArrayList<String>(Arrays.asList(getClassNames().split(",")));
    }

    public void setClassNames(String classNames) {
        if (classNames == null) {
            classNames = "";
        }
        this.classNames = classNames;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getShowAs() {
        return showAs;
    }

    public void setShowAs(String showAs) {
        if (showAs == null) {
            showAs = "chart";
        }
        this.showAs = showAs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            path = "";
        }
        this.path = path;
    }

    public String getPathPattern() {
        if (isFilteredByPath()) {
            return path.toLowerCase().replace('*', '%').replace('?', '_') + '%';
        } else {
            return "";
        }
    }

    public String getPeriodString() {
        String period = "day";
        switch (calendarField) {
            case Calendar.YEAR:
                period = "year";
                break;
            case Calendar.MONTH:
                period = "month";
                break;
            case Calendar.WEEK_OF_YEAR:
                period = "week";
                break;
            case Calendar.DAY_OF_YEAR:
                period = "day";
                break;
            case Calendar.HOUR_OF_DAY:
                period = "hour";
                break;
        }
        return period;
    }
}
