package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.Pager;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupUser;
import com.amee.restlet.BaseResource;
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
public class UserGroupsResource extends BaseResource implements Serializable {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    private GroupUser newGroupUser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserIdentifier(request.getAttributes().get("userUid").toString());
        setPage(request);
        setPagerSetType(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null) && (environmentBrowser.getUser() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER_GROUPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<GroupUser> groupUsers = siteService.getGroupUsers(environmentBrowser.getUser());
        Map<String, GroupUser> groupUserMap = new HashMap<String, GroupUser>();
        Set<Object> pagerSet = new HashSet<Object>();
        for (GroupUser groupUser : groupUsers) {
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
            List<GroupUser> groupUsers = siteService.getGroupUsers(environmentBrowser.getUser(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
            obj.put("user", environmentBrowser.getUser().getJSONObject());
            JSONArray groupUsersArr = new JSONArray();
            for (GroupUser groupUser : groupUsers) {
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
            List<GroupUser> groupUsers = siteService.getGroupUsers(environmentBrowser.getUser(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            element.appendChild(environmentBrowser.getUser().getIdentityElement(document));
            Element groupUsersElement = document.createElement("GroupUsers");
            for (GroupUser groupUser : groupUsers) {
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
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getUserActions().isAllowView()) {
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
        log.debug("acceptRepresentation");
        if (environmentBrowser.getUserActions().isAllowModify()) {
            Form form = getForm();
            // create new instance if submitted
            String groupUid = form.getFirstValue("groupUid");
            if (groupUid != null) {
                // find the Group
                Group group = siteService.getGroupByUid(environmentBrowser.getEnvironment(), groupUid);
                if (group != null) {
                    // create new instance
                    newGroupUser = new GroupUser(group, environmentBrowser.getUser());
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
        } else {
            notAuthorized();
        }
    }
}
