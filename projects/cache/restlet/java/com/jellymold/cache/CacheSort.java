package com.jellymold.cache;

import com.jellymold.utils.SortOrder;
import com.jellymold.utils.domain.UidGen;

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