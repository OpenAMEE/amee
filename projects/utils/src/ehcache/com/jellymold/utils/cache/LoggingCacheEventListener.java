package com.jellymold.utils.cache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingCacheEventListener implements CacheEventListener {

    private final Log log = LogFactory.getLog(getClass());

    public LoggingCacheEventListener() {
        log.debug("LoggingCacheEventListener()");
    }

    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        log.debug("Cache: " + ehcache.getName() + " Element: " + element.getKey());
    }

    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        log.debug("Cache: " + ehcache.getName() + " Element: " + element.getKey());
    }

    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        log.debug("Cache: " + ehcache.getName() + " Element: " + element.getKey());
    }

    public void notifyElementExpired(Ehcache ehcache, Element element) {
        log.debug("Cache: " + ehcache.getName() + " Element: " + element.getKey());
    }

    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        log.debug("Cache: " + ehcache.getName() + " Element: " + element.getKey());
    }

    public void notifyRemoveAll(Ehcache ehcache) {
        log.debug("Cache: " + ehcache.getName());
    }

    public void dispose() {
    }

    public Object clone() throws CloneNotSupportedException {
        return new LoggingCacheEventListener();
    }
}