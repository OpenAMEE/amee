package com.amee.engine;

import com.amee.domain.APIUtils;
import com.amee.restlet.site.FreeMarkerConfigurationService;
import com.amee.restlet.utils.MediaTypeUtils;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.auth.AuthService;
import freemarker.template.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.dom.DocumentImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Application;
import org.restlet.data.*;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    public Status getStatus(Throwable throwable, Request request, Response response) {
        Application.getCurrent().getLogger()
                .log(Level.SEVERE, "Unhandled exception or error intercepted: " + throwable, throwable);
        return new Status(Status.SERVER_ERROR_INTERNAL.getCode(), throwable);
    }

    @Override
    public Representation getRepresentation(Status status, Request request, Response response) {
        if (MediaTypeUtils.isStandardWebBrowser(request)) {
            return getWebBrowserRepresentation(status, request, response);
        } else {
            return getApiRepresentation(status, request);
        }
    }

    private Representation getWebBrowserRepresentation(Status status, Request request, Response response) {
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
    }

    private Representation getTemplateRepresentation(
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

    private Representation getApiRepresentation(Status status, Request request) {
        Representation representation = null;
        if (MediaTypeUtils.doesClientAccept(MediaType.APPLICATION_JSON, request)) {
            representation = getJsonRepresentation(status);
        } else if (MediaTypeUtils.doesClientAccept(MediaType.APPLICATION_XML, request)) {
            representation = getDomRepresentation(status);
        }
        if (representation == null) {
            representation = getStringRepresentation(status);
        }
        return representation;
    }

    private Representation getJsonRepresentation(Status status) {
        Representation representation = null;
        try {
            JSONObject obj = new JSONObject();
            JSONObject statusObj = new JSONObject();
            statusObj.put("code", status.getCode());
            statusObj.put("name", status.getName());
            statusObj.put("description", status.getDescription());
            statusObj.put("uri", status.getUri());
            obj.put("status", statusObj);
            representation = new JsonRepresentation(obj);
        } catch (JSONException e) {
            // swallow
        }
        return representation;
    }

    private Representation getDomRepresentation(Status status) {
        Representation representation;
        Document document = new DocumentImpl();
        Element elem = document.createElement("Resources");
        Element statusElem = document.createElement("Status");
        statusElem.appendChild(APIUtils.getElement(document, "Code", "" + status.getCode()));
        statusElem.appendChild(APIUtils.getElement(document, "Name", status.getName()));
        statusElem.appendChild(APIUtils.getElement(document, "Description", status.getDescription()));
        statusElem.appendChild(APIUtils.getElement(document, "URI", status.getUri()));
        elem.appendChild(statusElem);
        document.appendChild(elem);
        representation = new DomRepresentation(MediaType.APPLICATION_XML, document);
        return representation;
    }

    private Representation getStringRepresentation(Status status) {
        return new StringRepresentation(
                "Code: " + status.getCode() + "\n" +
                        "Name: " + status.getName() + "\n" +
                        "Description: " + status.getDescription() + "\n");
    }

    private String getNextUrl(Request request) {

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
}
