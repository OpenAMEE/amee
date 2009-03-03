package com.amee.domain.cache;

public interface CacheableFactory {

    public String getKey();

    public String getCacheName();

    public Object create();
}
