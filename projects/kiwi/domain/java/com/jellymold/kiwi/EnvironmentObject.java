package com.jellymold.kiwi;

import com.jellymold.utils.domain.PersistentObject;

public interface EnvironmentObject extends PersistentObject {

    public Environment getEnvironment();

    public void setEnvironment(Environment environment);
}