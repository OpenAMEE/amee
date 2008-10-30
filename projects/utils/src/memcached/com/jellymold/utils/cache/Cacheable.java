package com.jellymold.utils.cache;

import java.io.Serializable;

// TODO: is this needed?
public interface Cacheable extends Serializable {
    public String getKey(); // TODO: is this needed or ever used?
}