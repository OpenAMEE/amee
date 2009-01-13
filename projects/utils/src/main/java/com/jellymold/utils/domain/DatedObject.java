package com.jellymold.utils.domain;

import java.util.Date;

public interface DatedObject extends PersistentObject {

    public void onCreate();

    public Date getCreated();

    public void setCreated(Date created);

    public void onModify();

    public Date getModified();

    public void setModified(Date modified);
}