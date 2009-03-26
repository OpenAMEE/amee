package com.amee.engine;

import com.amee.restlet.site.FreeMarkerConfigurationService;
import com.amee.restlet.utils.MediaTypeUtils;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.auth.AuthService;
import freemarker.template.Configuration;
import org.apache.commons.lang.StringUtils;
import org.restlet.Application;
import org.restlet.data.*;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Service
public class EngineStatusService extends StatusService {

    @Autowired
    private AuthService authService;

    @Autowired
    private FreeMarkerConfigurationService freeMarkerConfigurationService;

    public EngineStatusService() {
        super();
    }

    @Override
    public Representation getRepresentation(Status status, Request request, Response response) {
        if (MediaTypeUtils.isStandardWebBrowser(request)) {
            Configuration configuration = freeMarkerConfigurationService.getConfiguration();
            if (configuration != null) {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("status", status);
                // values below are mirrored in BaseResource
                values.put("authService", authService);
                values.put("activeUser", AuthService.getUser());
                values.put("activeGroup", ThreadBeanHolder.get("group"));
                values.put("activeSite", ThreadBeanHolder.get("site"));
                values.put("activeApp", ThreadBeanHolder.get("app"));
                values.put("activeSiteApp", ThreadBeanHolder.get("siteApp"));
                // find a Template Representation
                return getTemplateRepresentation(status, request, configuration, values);
            } else {
                return super.getRepresentation(status, request, response);
            }
        } else {
            // just return status code for API calls
            return new StringRepresentation("You got an error! Don't worry, please visit: http://my.amee.com/developers\n");
        }
    }

    protected Representation getTemplateRepresentation(
            Status status,
            Request request,
            Configuration configuration,
            Map<String, Object> values) {
        if (request.getResourceRef().getPath().equals("/")) {
            return new TemplateRepresentation("default.ftl", configuration, values, MediaType.TEXT_HTML);
        } else if (status.equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
            values.put("next", getNextUrl(request));
            return new TemplateRepresentation("401.ftl", configuration, values, MediaType.TEXT_HTML);
        } else if (status.equals(Status.CLIENT_ERROR_FORBIDDEN)) {
            values.put("next", getNextUrl(request));
            return new TemplateRepresentation("403.ftl", configuration, values, MediaType.TEXT_HTML);
        } else if (status.equals(Status.CLIENT_ERROR_NOT_FOUND)) {
            return new TemplateRepresentation("404.ftl", configuration, values, MediaType.TEXT_HTML);
        } else if (status.equals(Status.SERVER_ERROR_INTERNAL)) {
            return new TemplateRepresentation("500.ftl", configuration, values, MediaType.TEXT_HTML);
        } else {
            return null;
        }
    }

    public static String getNextUrl(Request request) {

        // first, look for 'next' in parameters
        Form parameters = request.getResourceRef().getQueryAsForm();
        String next = parameters.getFirstValue("next");

        if (StringUtils.isEmpty(next)) {
            // second, determine 'next' from the previousResourceRef, if set (by DataFilter and ProfileFilter perhaps)
            if (request.getAttributes().get("previousResourceRef") != null) {
                next = request.getAttributes().get("previousResourceRef").toString();
            }
        }

        if (StringUtils.isEmpty(next)) {
            // third, determine 'next' from current URL
            next = request.getResourceRef().toString();
            if ((next != null) && ((next.endsWith("/signIn") || next.endsWith("/signOut") || next.endsWith("/protected")))) {
                next = null;
            }
        }

        if (StringUtils.isEmpty(next)) {
            // forth, use a default
            next = "/auth";
        }
        return next;
    }

    @Override
    public Status getStatus(Throwable throwable, Request request, Response response) {
        Application.getCurrent().getLogger()
                .log(Level.SEVERE, "Unhandled exception or error intercepted: " + throwable, throwable);
        return new Status(Status.SERVER_ERROR_INTERNAL.getCode(), throwable);
    }
}
