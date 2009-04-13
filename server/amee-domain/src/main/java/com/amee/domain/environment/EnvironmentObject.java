package com.amee.domain.environment;

import com.amee.core.PersistentObject;

public interface EnvironmentObject extends PersistentObject {

    public Environment getEnvironment();

    public void setEnvironment(Environment environment);
}