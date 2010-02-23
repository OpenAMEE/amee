package com.amee.service;

import com.amee.domain.StartEndDate;

public class BaseBrowser {

    protected StartEndDate startDate;
    protected StartEndDate endDate;

    /**
     * Get the query start date.
     *
     * @return the {@link com.amee.domain.StartEndDate StartEndDate} submitted with the GET request. If no date
     * was submitted, the default date corresponding to the start of the month is returned.
     */
    public StartEndDate getQueryStartDate() {
        return (startDate != null ? startDate : StartEndDate.getStartOfMonthDate());
    }

    public void setQueryStartDate(String date) {
        if (date != null) {
            startDate = new StartEndDate(date);
        }
    }

    /**
     * Get the query end date.
     *
     * @return the {@link com.amee.domain.StartEndDate StartEndDate} submitted with the GET request. If no date
     * was submitted, then Null is returned.
     */
    public StartEndDate getQueryEndDate() {
        return endDate;
    }

    public void setQueryEndDate(String date) {
        if (date != null) {
            endDate = new StartEndDate(date);
        }
    }

    public boolean isQuery() {
        return (startDate != null) || (endDate != null);
    }
}
