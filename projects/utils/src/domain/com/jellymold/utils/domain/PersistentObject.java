package com.jellymold.utils.domain;

public interface PersistentObject extends APIObject {

    public Long getId();

    public void setId(Long id);

    public String getUid();

    public void setUid(String uid);
}