package com.jellymold.kiwi.environment.group;

import com.jellymold.kiwi.Group;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import gc.carbon.environment.EnvironmentConstants;
import gc.carbon.environment.EnvironmentService;
import gc.carbon.environment.SiteService;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import org.json.JSONArray;
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class GroupsResource extends BaseResource implements Serializable {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    private Group newGroup;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_GROUPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<Group> groups = siteService.getGroups(environmentBrowser.getEnvironment(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("groups", groups);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Group> groups = siteService.getGroups(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
            JSONArray groupsArr = new JSONArray();
            for (Group group : groups) {
                groupsArr.put(group.getJSONObject());
            }
            obj.put("groups", groupsArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("group", newGroup.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("GroupsResource");
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Group> groups = siteService.getGroups(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            Element groupsElement = document.createElement("Groups");
            for (Group group : groups) {
                groupsElement.appendChild(group.getElement(document));
            }
            element.appendChild(groupsElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newGroup.getElement(document));
        }
        return element;

    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getGroupActions().isAllowList()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    // TODO: prevent duplicate instances
    @Override
    public void acceptRepresentation(Representation entity) {
        log.debug("post");
        if (environmentBrowser.getGroupActions().isAllowCreate()) {
            Form form = getForm();
            // create new instance if submitted
            if (form.getFirstValue("name") != null) {
                // create new instance
                newGroup = new Group(environmentBrowser.getEnvironment());
                newGroup.setName(form.getFirstValue("name"));
                newGroup.setDescription(form.getFirstValue("description"));
                siteService.save(newGroup);
            }
            if (newGroup != null) {
                if (isStandardWebBrowser()) {
                    success();
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }
}