package com.jellymold.kiwi;

import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.HeaderUtils;
import com.jellymold.utils.ThreadBeanHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.context.ApplicationContext;

// TODO: may be better to have a global filter that intelligently hooks into per module init functions
// TODO: define attributes required from site/application objects

public class SiteFilter extends Filter {

    private final Log log = LogFactory.getLog(getClass());

    private String applicationName;

    public SiteFilter(Application application, String applicationName) {
        super(application.getContext(), application);
        this.applicationName = applicationName;
    }

    protected int doHandle(Request request, Response response) {
        log.debug("do handle");
        String host = request.getResourceRef().getHostDomain();
        // insert host and SiteApp UID into Request and Seam event context
        request.getAttributes().put("host", host);
        ThreadBeanHolder.set("host", host);
        // get the Site for this request
        ApplicationContext springContext = (ApplicationContext) request.getAttributes().get("springContext");
        SiteService siteService = (SiteService) springContext.getBean("siteService");
        Site site = siteService.getSiteByHost(host);
        if (site != null) {
            SiteApp siteApp = siteService.getSiteApp(site, this.applicationName);
            if (siteApp != null) {
                // siteAppUid is used by SiteAppResource 
                request.getAttributes().put("siteAppUid", siteApp.getUid());
                // globally useful values
                ThreadBeanHolder.set("springContext", springContext);
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
}
