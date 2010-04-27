package com.amee.domain.environment;

import com.amee.base.domain.IdentityObject;

public interface EnvironmentObject extends IdentityObject {

    public Environment getEnvironment();

    public void setEnvironment(Environment environment);
}