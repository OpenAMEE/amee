package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.AdminBrowser;
import com.amee.domain.AMEEEntity;
import com.amee.domain.Pager;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrincipal;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.AdminConstants;
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
    private AdminBrowser adminBrowser;

    private GroupPrincipal newGroupPrincipal;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        adminBrowser.setUserIdentifier(request.getAttributes().get("userUid").toString());
        setPagerSetType(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (adminBrowser.getUser() != null);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getRootDataCategory());
        entities.add(adminBrowser.getUser());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return AdminConstants.VIEW_USER_GROUPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager();
        List<GroupPrincipal> groupPrincipals = groupService.getGroupPrincipalsForPrincipal(adminBrowser.getUser());
        Map<String, GroupPrincipal> groupPrincipalMap = new HashMap<String, GroupPrincipal>();
        Set<Object> pagerSet = new HashSet<Object>();
        for (GroupPrincipal groupPrincipal : groupPrincipals) {
            groupPrincipalMap.put(groupPrincipal.getGroup().getUid(), groupPrincipal);
            pagerSet.add(groupPrincipal.getGroup());
        }
        pager.setPagerSet(pagerSet);
        List<Group> groups = groupService.getGroups(pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", adminBrowser);
        values.put("user", adminBrowser.getUser());
        values.put("groups", groups);
        values.put("groupPrincipalMap", groupPrincipalMap);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager();
            List<GroupPrincipal> groupPrincipals = groupService.getGroupPrincipalsForPrincipal(adminBrowser.getUser(), pager);
            pager.setCurrentPage(getPage());
            obj.put("user", adminBrowser.getUser().getJSONObject());
            JSONArray groupPrincipalsArr = new JSONArray();
            for (GroupPrincipal groupPrincipal : groupPrincipals) {
                groupPrincipalsArr.put(groupPrincipal.getJSONObject());
            }
            obj.put("groupPrincipals", groupPrincipals);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("groupPrincipal", newGroupPrincipal.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserGroupsResource");
        if (isGet()) {
            Pager pager = getPager();
            List<GroupPrincipal> groupPrincipals = groupService.getGroupPrincipalsForPrincipal(adminBrowser.getUser(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(adminBrowser.getUser().getIdentityElement(document));
            Element groupPrincipalsElement = document.createElement("GroupPrincipals");
            for (GroupPrincipal groupPrincipal : groupPrincipals) {
                groupPrincipalsElement.appendChild(groupPrincipal.getElement(document));
            }
            element.appendChild(groupPrincipalsElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newGroupPrincipal.getElement(document));
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
            Group group = groupService.getGroupByUid(groupUid);
            if (group != null) {
                // create new instance
                newGroupPrincipal = new GroupPrincipal(group, adminBrowser.getUser());
                groupService.save(newGroupPrincipal);
            }
        }
        if (newGroupPrincipal != null) {
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
