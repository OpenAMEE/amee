package com.jellymold.utils.cache;

public interface CacheableFactory {

    public String getKey();

    public String getCacheName();

    public Object create();
}
