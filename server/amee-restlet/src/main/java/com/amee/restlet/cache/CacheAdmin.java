package com.amee.restlet.cache;

import com.amee.domain.cache.CacheHelper;
import com.amee.domain.sheet.SortOrder;
import com.amee.service.auth.ResourceActions;
import net.sf.ehcache.Ehcache;
import org.restlet.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CacheAdmin {

    @Autowired
    @Qualifier("cacheActions")
    private ResourceActions cacheActions;

    private Ehcache cache;

    private CacheSort cacheSort;

    // sorting

    public CacheSort getCacheSort(Request request, Response response, Form form) {
        String filterUid = getCacheSortUid(request);
        if (filterUid != null) {
            cacheSort = (CacheSort) CacheHelper.getInstance().get("CacheSort", filterUid);
        }
        if (cacheSort == null) {
            cacheSort = new CacheSort();
            setCacheSortUid(response, cacheSort);
        }
        if (form.getNames().contains("sortBy")) {
            cacheSort.setSortBy(form.getFirstValue("sortBy"));
        }
        if (form.getNames().contains("sortOrder")) {
            try {
                cacheSort.setSortOrder(SortOrder.valueOf(form.getFirstValue("sortOrder")));
            } catch (IllegalArgumentException e) {
                // swallow
            }
        }
        CacheHelper.getInstance().add("CacheSort", cacheSort.getUid(), cacheSort);
        return cacheSort;
    }

    public String getCacheSortUid(Request request) {
        Cookie cacheSortUidCookie = request.getCookies().getFirst("cacheSortUid");
        if (cacheSortUidCookie != null) {
            return cacheSortUidCookie.getValue();
        } else {
            return null;
        }
    }

    public void setCacheSortUid(Response response, CacheSort cacheSort) {
        CookieSetting cacheSortUidCookie = new CookieSetting(0, "cacheSortUid", cacheSort.getUid(), "/", null);
        CookieSetting oldCacheSortUidCookie = response.getCookieSettings().getFirst("cacheSortUid");
        if (oldCacheSortUidCookie != null) {
            response.getCookieSettings().remove(oldCacheSortUidCookie);
        }
        response.getCookieSettings().add(cacheSortUidCookie);
    }


    public ResourceActions getCacheActions() {
        return cacheActions;
    }

    public void setCacheActions(ResourceActions cacheActions) {
        this.cacheActions = cacheActions;
    }

    public Ehcache getCache() {
        return cache;
    }

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }
}