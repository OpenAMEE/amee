package com.amee.restlet.site;

import com.amee.domain.site.App;
import com.amee.domain.site.Site;
import com.amee.domain.site.SiteApp;
import com.amee.restlet.utils.HeaderUtils;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.environment.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Filter;
import org.restlet.VirtualHost;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

// TODO: may be better to have a global filter that intelligently hooks into per module init functions
// TODO: define attributes required from site/application objects

public class SiteFilter extends Filter implements ApplicationContextAware {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SiteService siteService;

    @Autowired
    private Component ameeContainer;

    private ApplicationContext applicationContext;

    public SiteFilter(Application application) {
        super(application.getContext());
    }

    protected int doHandle(Request request, Response response) {
        log.debug("do handle");
        // get the Site for this request
        Site site = siteService.getSiteByName(getSiteName());
        if (site != null) {
            SiteApp siteApp = siteService.getSiteApp(site, Application.getCurrent().getName());
            if (siteApp != null) {
                // siteAppUid is used by SiteAppResource 
                request.getAttributes().put("siteAppUid", siteApp.getUid());
                // globally useful values
                request.getAttributes().put("environment", site.getEnvironment());
                ThreadBeanHolder.set("springContext", applicationContext); // used in BaseResource
                ThreadBeanHolder.set("environment", site.getEnvironment());
                // set details about the SiteApp & App being visited
                ThreadBeanHolder.set("site", site);
                ThreadBeanHolder.set("siteApp", siteApp);
                ThreadBeanHolder.set("app", siteApp.getApp());
                ThreadBeanHolder.set("skinPath", siteApp.getSkinPath());
                return super.doHandle(request, response);
            }
        }
        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return STOP;
    }

    protected void afterHandle(Request request, Response response) {
        log.debug("after handle");
        App app = (App) ThreadBeanHolder.get("app");
        if ((app != null) && (!app.isAllowClientCache())) {
            // ensure client does not cache response
            HeaderUtils.addHeader("Pragma", "no-cache", response);
            HeaderUtils.addHeader("Cache-Control", "no-cache, must-revalidate", response);
        }
    }

    private String getSiteName() {
        VirtualHost host = getVirtualHost();
        if (host != null) {
            return host.getName();
        } else {
            log.error("Could not find VirtualHost for Site.");
            throw new RuntimeException("Could not find VirtualHost for Site.");
        }
    }

    // code here is inspired by code in com.noelios.restlet.component.ComponentClientDispatcher
    private VirtualHost getVirtualHost() {

        VirtualHost host = null;
        final int hostHashCode = VirtualHost.getCurrent();

        for (VirtualHost h : ameeContainer.getHosts()) {
            if (h.hashCode() == hostHashCode) {
                host = h;
            }
        }
        if ((host == null) && (ameeContainer.getDefaultHost() != null)) {
            if (ameeContainer.getDefaultHost().hashCode() == hostHashCode) {
                host = ameeContainer.getDefaultHost();
            }
        }

        return host;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
