package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.Pager;
import com.amee.domain.AMEEEntity;
import com.amee.domain.data.DataCategory;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrinciple;
import com.amee.restlet.BaseResource;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.environment.SiteService;
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
import java.util.*;

@Component
@Scope("prototype")
public class UserGroupsResource extends AuthorizeResource implements Serializable {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    private GroupPrinciple newGroupUser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserUid(request.getAttributes().get("userUid").toString());
        setPage(request);
        setPagerSetType(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null) && (environmentBrowser.getUser() != null);
    }

    @Override
    protected List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(environmentBrowser.getEnvironment());
        entities.add(environmentBrowser.getUser());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER_GROUPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<GroupPrinciple> groupUsers = siteService.getGroupUsers(environmentBrowser.getUser());
        Map<String, GroupPrinciple> groupUserMap = new HashMap<String, GroupPrinciple>();
        Set<Object> pagerSet = new HashSet<Object>();
        for (GroupPrinciple groupUser : groupUsers) {
            groupUserMap.put(groupUser.getGroup().getUid(), groupUser);
            pagerSet.add(groupUser.getGroup());
        }
        pager.setPagerSet(pagerSet);
        List<Group> groups = siteService.getGroups(environmentBrowser.getEnvironment(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("user", environmentBrowser.getUser());
        values.put("groups", groups);
        values.put("groupUserMap", groupUserMap);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<GroupPrinciple> groupUsers = siteService.getGroupUsers(environmentBrowser.getUser(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
            obj.put("user", environmentBrowser.getUser().getJSONObject());
            JSONArray groupUsersArr = new JSONArray();
            for (GroupPrinciple groupUser : groupUsers) {
                groupUsersArr.put(groupUser.getJSONObject());
            }
            obj.put("groupUsers", groupUsers);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("groupUser", newGroupUser.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserGroupsResource");
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<GroupPrinciple> groupUsers = siteService.getGroupUsers(environmentBrowser.getUser(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            element.appendChild(environmentBrowser.getUser().getIdentityElement(document));
            Element groupUsersElement = document.createElement("GroupUsers");
            for (GroupPrinciple groupUser : groupUsers) {
                groupUsersElement.appendChild(groupUser.getElement(document));
            }
            element.appendChild(groupUsersElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newGroupUser.getElement(document));
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
        String groupUid = form.getFirstValue("groupUid");
        if (groupUid != null) {
            // find the Group
            Group group = siteService.getGroupByUid(environmentBrowser.getEnvironment(), groupUid);
            if (group != null) {
                // create new instance
                newGroupUser = new GroupPrinciple(group, environmentBrowser.getUser());
                siteService.save(newGroupUser);
            }
        }
        if (newGroupUser != null) {
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
