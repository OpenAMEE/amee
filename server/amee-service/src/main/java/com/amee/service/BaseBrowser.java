package com.amee.service;

import com.amee.domain.profile.StartEndDate;

import java.util.Date;

public class BaseBrowser {

    protected StartEndDate startDate;
    protected StartEndDate endDate;

    public void setStartDate(String date) {
        if (date != null) {
            startDate = new StartEndDate(date);
        }
    }

    public void setStartDate(Date startDate) {
        if (startDate != null) {
            this.startDate = new StartEndDate(startDate);
        }
    }

    // START_OF_MONTH is default start date for GET requests.
    public StartEndDate getStartDate() {
        return (startDate != null ? startDate : StartEndDate.getStartOfMonthDate());
    }

    public void setEndDate(String date) {
        if (date != null) {
            endDate = new StartEndDate(date);
        }
    }

    public void setEndDate(Date endDate) {
        if (endDate != null) {
            this.endDate = new StartEndDate(endDate);
        }
    }

    public StartEndDate getEndDate() {
        return endDate;
    }

    public boolean isQuery() {
        return (startDate != null) || (endDate != null);
    }
}
