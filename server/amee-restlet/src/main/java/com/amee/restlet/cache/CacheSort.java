package com.amee.restlet.cache;

import com.amee.domain.UidGen;
import com.amee.domain.sheet.SortOrder;

import java.io.Serializable;

public class CacheSort implements Serializable {

    private String uid;
    private String sortBy = "name";
    private SortOrder sortOrder = SortOrder.DESC;

    public CacheSort() {
        super();
        setUid(UidGen.getUid());
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid != null) {
            this.uid = uid;
        }
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        if (sortBy == null) {
            sortBy = "name";
        }
        this.sortBy = sortBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
    }
}