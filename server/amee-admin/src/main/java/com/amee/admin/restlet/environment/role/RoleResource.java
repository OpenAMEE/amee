package com.amee.admin.restlet.environment.role;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.auth.Role;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.service.environment.SiteService;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

@Component
@Scope("prototype")
public class RoleResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setRoleUid(request.getAttributes().get("roleUid").toString());
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getRole() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_ROLE;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("role", environmentBrowser.getRole());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getIdentityJSONObject());
        obj.put("role", environmentBrowser.getRole().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("RoleResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getRole().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getRoleActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicates
    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation");
        if (environmentBrowser.getRoleActions().isAllowModify()) {
            Form form = getForm();
            Role role = environmentBrowser.getRole();
            // update values
            if (form.getNames().contains("name")) {
                role.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("description")) {
                role.setDescription(form.getFirstValue("description"));
            }
            success();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    // TODO: do not allow delete affecting logged-in auth...
    @Override
    public void removeRepresentations() {
        if (environmentBrowser.getRoleActions().isAllowDelete()) {
            siteService.remove(environmentBrowser.getRole());
            success();
        } else {
            notAuthorized();
        }
    }
}