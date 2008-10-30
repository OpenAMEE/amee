package com.jellymold.kiwi;

import java.util.Set;

public interface TargetBuilder {

    public Set<Target> getTargets();

    public Environment getEnvironment();

    public void setEnvironment(Environment environment);
}
