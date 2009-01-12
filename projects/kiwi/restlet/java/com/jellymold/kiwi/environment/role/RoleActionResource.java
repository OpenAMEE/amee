package com.jellymold.kiwi.environment.role;

import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.utils.BaseResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

@Component
@Scope("prototype")
public class RoleActionResource extends BaseResource {

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    public RoleActionResource() {
        super();
    }

    public RoleActionResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setRoleUid(request.getAttributes().get("roleUid").toString());
        environmentBrowser.setActionUid(request.getAttributes().get("actionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getRole() != null) && (environmentBrowser.getAction() != null);
    }

    @Override
    public String getTemplatePath() {
        return ""; // no view
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("role", environmentBrowser.getRole());
        values.put("action", environmentBrowser.getAction());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("role", environmentBrowser.getRole().getIdentityJSONObject());
        obj.put("action", environmentBrowser.getAction().getIdentityJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("RoleActionResource");
        element.appendChild(environmentBrowser.getRole().getIdentityElement(document));
        element.appendChild(environmentBrowser.getAction().getIdentityElement(document));
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
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void removeRepresentations() {
        if (environmentBrowser.getRoleActions().isAllowModify()) {
            environmentBrowser.getRole().remove(environmentBrowser.getAction());
            success();
        } else {
            notAuthorized();
        }
    }
}