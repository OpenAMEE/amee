package com.jellymold.utils.cache;

import com.jellymold.utils.domain.PersistentObject;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.List;

public class CacheHelper implements Serializable {

    private final static Logger log = Logger.getLogger(CacheHelper.class);

    private static CacheHelper instance = new CacheHelper();
    MemcachedClient client;

    private CacheHelper() {
        super();
        try {
            client = new MemcachedClient(new InetSocketAddress("localhost", 11211));
        } catch (IOException e) {
            // swallow
        }
    }

    public static CacheHelper getInstance() {
        return instance;
    }

    // TODO: replace usages of this with encapsulated functionality here
    public Object getInternal() {
        return client;
    }

    public Cacheable getCacheable(CacheableFactory cacheableFactory) {
        Object obj;
        Cacheable cacheable;
        String namespace = cacheableFactory.getCacheName();
        String key = cacheableFactory.getKey();
        if (client != null) {
            log.debug("namespace: " + namespace + " key: " + key);
            obj = client.get(key);
            if ((obj == null) || !(obj instanceof Cacheable)) {
                // element is not cached - build it
                cacheable = cacheableFactory.createCacheable();
                client.set("key", 3600, cacheable);
            } else {
                cacheable = (Cacheable) obj;
            }
        } else {
            log.warn("client NOT found");
            cacheable = cacheableFactory.createCacheable();
        }
        return cacheable;
    }

    @Deprecated
    public void clearCache(String cacheName, String keyPrefix) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void clearCache(String cacheName) {
        // TODO
    }

    @Deprecated
    public void remove(String cacheName, String key) {
        throw new UnsupportedOperationException();
    }

    public void add(String cacheName, String key, Object o) {
        client.set("key", 3600, o);
    }

    @Deprecated
    public List getKeys(String cacheName) {
        throw new UnsupportedOperationException();
    }

    public Object get(String cacheName, Object key) {
        return client.get(key.toString());
    }

    @Deprecated
    public Object getAndBlock(String cacheName, Object key) {
        return get(cacheName, key);
    }

    @Deprecated
    public Object getAndRemove(String cacheName, Object key) {
        throw new UnsupportedOperationException();
    }

    public void add(String cacheName, PersistentObject persistentObject) {
        add(cacheName, persistentObject.getUid(), persistentObject);
    }

    @Deprecated
    public void remove(String cacheName, PersistentObject persistentObject) {
        remove(cacheName, persistentObject.getUid());
    }
}