package com.amee.restlet.site;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.environment.Environment;
import com.amee.domain.site.ISite;
import com.amee.restlet.AMEEApplication;
import com.amee.restlet.BaseFilter;
import com.amee.restlet.utils.HeaderUtils;
import com.amee.service.environment.EnvironmentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.VirtualHost;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class SiteFilter extends BaseFilter {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private Component ameeContainer;

    public SiteFilter(Application application) {
        super(application.getContext());
    }

    protected int doHandle(Request request, Response response) {
        log.debug("do handle");
        // get the VirtualHost for this request
        VirtualHost host = getVirtualHost();
        // can only continue if the VirtualHost is an ISite
        if (host instanceof ISite) {
            // get ISite and Environment
            ISite site = (ISite) host;
            Environment environment = environmentService.getEnvironmentByName(site.getEnvironmentName());
            // must have an Environment to continue
            if (environment != null) {
                // setup request/thread values for current request
                Map<String, Object> attributes = request.getAttributes();
                // environmentUid can be part of the URL (environment admin site)
                // if environmentUid is not already set then we add it to enable admin
                // resources outside the main environment admin site
                if (!attributes.containsKey("environmentUid")) {
                    attributes.put("environmentUid", environment.getUid());
                }
                // globally useful values
                attributes.put("activeEnvironment", environment);
                attributes.put("activeSite", site);
                ThreadBeanHolder.set("activeSite", site); // used in FreeMarkerConfigurationFactory
                // continue
                return super.doHandle(request, response);
            }
        }
        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return STOP;
    }

    protected void afterHandle(Request request, Response response) {
        log.debug("after handle");
        if ((getApplication() != null) && (getApplication() instanceof AMEEApplication)) {
            AMEEApplication app = (AMEEApplication) getApplication();
            if (!app.isAllowClientCache()) {
                // ensure client does not cache response
                HeaderUtils.addHeader("Pragma", "no-cache", response);
                HeaderUtils.addHeader("Cache-Control", "no-cache, must-revalidate", response);
            }
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
}