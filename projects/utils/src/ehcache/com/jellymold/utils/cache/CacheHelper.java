package com.jellymold.utils.cache;

import com.jellymold.utils.domain.PersistentObject;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheHelper implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static CacheHelper instance = new CacheHelper();
    private CacheManager cacheManager;

    private CacheHelper() {
        super();
        cacheManager = CacheManager.getInstance();
    }

    public static CacheHelper getInstance() {
        return instance;
    }

    public Object getInternal() {
        return cacheManager;
    }

    public BlockingCache getBlockingCache(String cacheName) {
        Ehcache cache = getCacheManager().getEhcache(cacheName);
        if ((cache != null) && !(cache instanceof BlockingCache)) {
            synchronized (this) {
                cache = getCacheManager().getEhcache(cacheName);
                if ((cache != null) && !(cache instanceof BlockingCache)) {
                    BlockingCache newBlockingCache = new BlockingCache(cache);
                    getCacheManager().replaceCacheWithDecoratedCache(cache, newBlockingCache);
                }
            }
        }
        return (BlockingCache) getCacheManager().getEhcache(cacheName);
    }

    public Object getCacheable(CacheableFactory factory) {
        Object o = null;
        String cacheName = factory.getCacheName();
        String elementKey = factory.getKey();
        BlockingCache cache = getBlockingCache(cacheName);
        if (cache != null) {
            log.debug("cache: " + cacheName + " elementKey: " + elementKey);
            String originalThreadName = Thread.currentThread().getName();
            try {
                Element element = cache.get(elementKey);
                if (element == null) {
                    try {
                        // element is not cached - build it
                        o = factory.create();
                        cache.put(new Element(elementKey, o));
                    } catch (final Throwable throwable) {
                        // must unlock the cache if the above fails
                        cache.put(new Element(elementKey, null));
                        // TODO: what should we really be throwing here?
                        throw new RuntimeException(throwable);
                    }
                } else {
                    o = element.getObjectValue();
                }
            } finally {
                Thread.currentThread().setName(originalThreadName);
            }
        } else {
            log.warn("cache NOT found: " + cacheName);
            o = factory.create();
        }
        return o;
    }

    // TODO: profile this method to see if it's cost effective
    public void clearCache(String cacheName, String elementKeyPrefix) {
        BlockingCache cache = getBlockingCache(cacheName);
        if (cache != null) {
            log.debug("cache: " + cacheName + " elementKeyPrefix: " + elementKeyPrefix);
            for (Object o : cache.getKeys()) {
                String elementKey = (String) o;
                if (elementKey.startsWith(elementKeyPrefix)) {
                    log.debug("removing: " + elementKey);
                    cache.remove(elementKey);
                }
            }
        }
    }

    public void clearCache(String cacheName) {
        BlockingCache cache = getBlockingCache(cacheName);
        if (cache != null) {
            log.debug("cache: " + cacheName);
            cache.removeAll();
            cache.getKeys();
        }
    }

    private String getCacheKey(String key, String environmentuid){
        return key + "_" + environmentuid;
    }

    public void remove(String cacheName, String elementKey, String environmentUid){
        remove(cacheName, getCacheKey(elementKey,environmentUid));
    }

    public void remove(String cacheName, String elementKey) {
        BlockingCache cache = getBlockingCache(cacheName);
        if (cache != null) {
            log.debug("cache: " + cacheName + " elementKey: " + elementKey);
            cache.remove(elementKey);
        }
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void add(String cacheName, String environmentUid, String elementKey, Object o){
        add(cacheName,getCacheKey(elementKey,environmentUid),o);
    }

    public void add(String cacheName, String elementKey, Object o) {
        BlockingCache cache = getBlockingCache(cacheName);
        if (cache != null) {
            log.debug("cache: " + cacheName + " elementKey: " + elementKey);
            cache.put(new Element(elementKey, o));
        }
    }

    public List getKeys(String cacheName) {
        BlockingCache cache = getBlockingCache(cacheName);
        return cache.getKeys();
    }

    public Object get(String cacheName, String environmentUid, String elementKey){
        return get(cacheName, getCacheKey(elementKey,environmentUid));
    }

    public Object get(String cacheName, Object elementKey) {
        BlockingCache cache = getBlockingCache(cacheName);
        if ((cache != null) && cache.isKeyInCache(elementKey)) {
            Element element = cache.get(elementKey);
            if (element != null) {
                return element.getValue();
            } else {
                // unlock the blocking cache if cache.get fails
                cache.put(new Element(elementKey, null));
            }
        }
        return null;
    }

    public Object getAndBlock(String cacheName, String elementKey, String environmentUid){
        return getAndBlock(cacheName, getCacheKey(elementKey,environmentUid));
    }

    public Object getAndBlock(String cacheName, Object elementKey) {
        BlockingCache cache = getBlockingCache(cacheName);
        if ((cache != null) && cache.isKeyInCache(elementKey)) {
            Element element = cache.get(elementKey);
            if (element != null) {
                return element.getValue();
            }
        }
        return null;
    }

    public Object getAndRemove(String cacheName, String elementKey, String environmentUid){
        return getAndRemove(cacheName, getCacheKey(elementKey,environmentUid));
    }

    public Object getAndRemove(String cacheName, Object elementKey) {
        BlockingCache cache = getBlockingCache(cacheName);
        if ((cache != null) && cache.isKeyInCache(elementKey)) {
            Element element = cache.get(elementKey);
            if (element != null) {
                cache.remove(elementKey);
                return element.getValue();
            } else {
                // unlock the blocking cache if cache.get fails
                cache.put(new Element(elementKey, null));
            }
        }
        return null;
    }

    public void add(String cacheName, PersistentObject persistentObject, String environmentUid){
        add(cacheName,getCacheKey(persistentObject.getUid(),environmentUid),persistentObject);
    }

    public void add(String cacheName, PersistentObject persistentObject) {
        add(cacheName, persistentObject.getUid(), persistentObject);

    }

    public void remove(String cacheName, PersistentObject persistentObject, String environmentUid){
        remove(cacheName,getCacheKey(persistentObject.getUid(),environmentUid));
    }

    public void remove(String cacheName, PersistentObject persistentObject) {
        remove(cacheName, persistentObject.getUid());
    }
}