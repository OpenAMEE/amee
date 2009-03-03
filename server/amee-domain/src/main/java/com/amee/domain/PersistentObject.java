package com.amee.domain;

public interface PersistentObject {

    public final static int UID_SIZE = 12;

    public Long getId();

    public void setId(Long id);

    public String getUid();

    public void setUid(String uid);
}