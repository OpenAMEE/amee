package com.jellymold.kiwi;

import com.jellymold.utils.domain.DatedObject;

public interface AuditableObject extends EnvironmentObject, DatedObject {

    public User getCreatedBy();

    public void setCreatedBy(User createdBy);

    public User getModifiedBy();

    public void setModifiedBy(User modifiedBy);
}
