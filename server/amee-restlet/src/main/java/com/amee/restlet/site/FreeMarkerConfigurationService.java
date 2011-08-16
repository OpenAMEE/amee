package com.amee.restlet.site;

import com.amee.domain.cache.CacheHelper;
import freemarker.template.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class FreeMarkerConfigurationService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private FreeMarkerConfigurationFactory freeMarkerConfigurationFactory;

    private CacheHelper cacheHelper = CacheHelper.getInstance();

    public FreeMarkerConfigurationService() {
        log.debug("FreeMarkerConfigurationService()");
    }

    public Configuration getConfiguration() {
        return (Configuration) cacheHelper.getCacheable(freeMarkerConfigurationFactory);
    }
}