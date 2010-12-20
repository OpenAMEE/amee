package com.amee.restlet.site;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.site.ISite;
import com.amee.restlet.AMEEApplication;
import com.amee.restlet.BaseFilter;
import com.amee.restlet.utils.HeaderUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.VirtualHost;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class SiteFilter extends BaseFilter {

    private final Log log = LogFactory.getLog(getClass());

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
            // get ISite
            ISite site = (ISite) host;
            // setup request/thread values for current request
            Map<String, Object> attributes = request.getAttributes();
            // globally useful values
            attributes.put("activeSite", site);
            ThreadBeanHolder.set("activeSite", site); // used in FreeMarkerConfigurationFactory
            attributes.put("activeServer", getServer());
            // continue
            return super.doHandle(request, response);
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

    private Server getServer() {
        Server currentServer = null;

        // Use the port of the current request to match.
        int currentPort = Response.getCurrent().getServerInfo().getPort();
        for (Server s : ameeContainer.getServers()) {
            if (s.getPort() == currentPort) {
                currentServer = s;
            }
        }

        return currentServer;
    }
}