package com.amee.admin.restlet.environment.group;

import com.amee.admin.restlet.environment.AdminBrowser;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.Pager;
import com.amee.domain.auth.Group;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.AdminConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class GroupsResource extends AuthorizeResource implements Serializable {

    @Autowired
    private AdminBrowser adminBrowser;

    private Group newGroup;

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return AdminConstants.VIEW_GROUPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager();
        List<Group> groups = groupService.getGroups(pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", adminBrowser);
        values.put("groups", groups);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager();
            List<Group> groups = groupService.getGroups(pager);
            pager.setCurrentPage(getPage());
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
            Pager pager = getPager();
            List<Group> groups = groupService.getGroups(pager);
            pager.setCurrentPage(getPage());
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
    public boolean allowPost() {
        return true;
    }

    // TODO: prevent duplicate instances
    @Override
    public void doAccept(Representation entity) {
        log.debug("doAccept");
        Form form = getForm();
        // create new instance if submitted
        if (form.getFirstValue("name") != null) {
            // create new instance
            newGroup = new Group();
            newGroup.setName(form.getFirstValue("name"));
            newGroup.setDescription(form.getFirstValue("description"));
            groupService.save(newGroup);
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
    }
}