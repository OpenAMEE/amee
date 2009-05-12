package com.amee.admin.restlet.environment.role;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.restlet.BaseResource;
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

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
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

    // TODO: See http://my.amee.com/developers/ticket/243 & http://my.amee.com/developers/ticket/242
    @Override
    public boolean allowDelete() {
        return false;
    }

    // TODO: See http://my.amee.com/developers/ticket/243 & http://my.amee.com/developers/ticket/242
    @Override
    public void removeRepresentations() {
        throw new UnsupportedOperationException();
//        if (environmentBrowser.getRoleActions().isAllowModify()) {
//            environmentBrowser.getRole().remove(environmentBrowser.getAction());
//            success();
//        } else {
//            notAuthorized();
//        }
    }
}