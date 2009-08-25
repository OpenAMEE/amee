package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentConstants;
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
public class UserGroupRoleResource extends BaseResource {

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserIdentifier(request.getAttributes().get("userUid").toString());
        environmentBrowser.setGroupUid(request.getAttributes().get("groupUid").toString());
        environmentBrowser.setRoleUid(request.getAttributes().get("roleUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null) && (environmentBrowser.getUser() != null) && (environmentBrowser.getGroupUser() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER_GROUP_ROLE;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("user", environmentBrowser.getUser());
        values.put("group", environmentBrowser.getGroup());
        values.put("groupUser", environmentBrowser.getGroupUser());
        values.put("role", environmentBrowser.getRole());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("user", environmentBrowser.getUser().getJSONObject());
        obj.put("groupUser", environmentBrowser.getGroupUser().getJSONObject());
        obj.put("role", environmentBrowser.getRole().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserGroupResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getUser().getIdentityElement(document));
        element.appendChild(environmentBrowser.getGroupUser().getElement(document));
        element.appendChild(environmentBrowser.getRole().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getUserActions().isAllowView()) {
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
        if (environmentBrowser.getUserActions().isAllowModify()) {
            environmentBrowser.getGroupUser().remove(environmentBrowser.getRole());
            success();
        } else {
            notAuthorized();
        }
    }
}

