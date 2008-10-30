package com.jellymold.engine;

import com.jellymold.kiwi.auth.AuthUtils;
import com.jellymold.utils.MediaTypeUtils;
import com.noelios.restlet.StatusFilter;
import freemarker.template.Configuration;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

import java.util.HashMap;
import java.util.Map;

public class EngineStatusFilter extends StatusFilter {

    public EngineStatusFilter(Context context, boolean overwrite, String email, String homeUri) {
        super(context, overwrite, email, homeUri);
    }

    public Representation getRepresentation(Status status, Request request, Response response) {
        if (MediaTypeUtils.isStandardWebBrowser(request)) {
            Configuration configuration = (Configuration)
                    request.getAttributes().get("freeMarkerConfiguration");
            Map<String, Object> values = new HashMap<String, Object>();
            // values below are mirrored in BaseResource and SkinRenderResource
            // TODO: SPRINGIFY
            // values.put("authService", Contexts.lookupInStatefulContexts("authService"));
            // values.put("activeUser", Contexts.lookupInStatefulContexts("user"));
            // values.put("activeGroup", Contexts.lookupInStatefulContexts("group"));
            // values.put("activeSite", Contexts.lookupInStatefulContexts("site"));
            // values.put("activeApp", Contexts.lookupInStatefulContexts("app"));
            // values.put("activeSiteApp", Contexts.lookupInStatefulContexts("siteApp"));
            // find a Template Representation
            return getTemplateRepresentation(status, request, response, configuration, values);
        } else {
            // just return status code for API calls
            return new StringRepresentation("");
        }
    }

    protected Representation getTemplateRepresentation(Status status, Request request, Response response, Configuration configuration, Map<String, Object> values) {
        if (status.equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
            values.put("next", AuthUtils.getNextUrl(request));
            return new TemplateRepresentation("401.ftl", configuration, values, MediaType.TEXT_HTML);
        } else if (status.equals(Status.CLIENT_ERROR_FORBIDDEN)) {
            values.put("next", AuthUtils.getNextUrl(request));
            return new TemplateRepresentation("403.ftl", configuration, values, MediaType.TEXT_HTML);
        } else if (status.equals(Status.CLIENT_ERROR_NOT_FOUND)) {
            return new TemplateRepresentation("404.ftl", configuration, values, MediaType.TEXT_HTML);
        } else if (status.equals(Status.SERVER_ERROR_INTERNAL)) {
            return new TemplateRepresentation("500.ftl", configuration, values, MediaType.TEXT_HTML);
        } else {
            return null;
        }
    }
}
