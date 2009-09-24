package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.AMEEEntity;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.SiteService;
import com.amee.service.environment.GroupService;
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

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class UserGroupResource extends AuthorizeResource {

    @Autowired
    private GroupService GroupService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserIdentifier(request.getAttributes().get("userUid").toString());
        environmentBrowser.setGroupUid(request.getAttributes().get("groupUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (environmentBrowser.getEnvironment() != null) &&
                (environmentBrowser.getUser() != null) &&
                (environmentBrowser.getGroupPrincipal() != null);
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
        throw new UnsupportedOperationException("UserGroupResource does not have or need a template.");
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("user", environmentBrowser.getUser().getJSONObject());
        obj.put("groupPrincipal", environmentBrowser.getGroupPrincipal().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserGroupResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getUser().getIdentityElement(document));
        element.appendChild(environmentBrowser.getGroupPrincipal().getElement(document));
        return element;
    }


    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void removeRepresentations() {
        groupService.remove(environmentBrowser.getGroupPrincipal());
        success();
    }
}