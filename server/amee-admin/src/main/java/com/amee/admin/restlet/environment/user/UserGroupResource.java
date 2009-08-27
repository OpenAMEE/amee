package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.restlet.BaseResource;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.domain.AMEEEntity;
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
import java.util.List;
import java.util.ArrayList;

@Component
@Scope("prototype")
public class UserGroupResource extends AuthorizeResource {

    @Autowired
    private EnvironmentBrowser environmentBrowser;
    
    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserUid(request.getAttributes().get("userUid").toString());
        environmentBrowser.setGroupUid(request.getAttributes().get("groupUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null) && (environmentBrowser.getUser() != null) && (environmentBrowser.getGroupPrinciple() != null);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(environmentBrowser.getEnvironment());
        entities.add(environmentBrowser.getUser());
        entities.add(environmentBrowser.getGroup());
        return entities;
    }
    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER_GROUP;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("user", environmentBrowser.getUser());
        values.put("group", environmentBrowser.getGroup());
        values.put("groupPrinciple", environmentBrowser.getGroupPrinciple());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("user", environmentBrowser.getUser().getJSONObject());
        obj.put("groupPrinciple", environmentBrowser.getGroupPrinciple().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserGroupResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getUser().getIdentityElement(document));
        element.appendChild(environmentBrowser.getGroupPrinciple().getElement(document));
        return element;
    }

    @Override
    public boolean allowDelete() {
        return false;
    }
}