package com.jellymold.kiwi.environment.user;

import com.jellymold.kiwi.GroupUser;
import com.jellymold.kiwi.Role;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import gc.carbon.environment.EnvironmentConstants;
import gc.carbon.environment.SiteService;
import gc.carbon.environment.EnvironmentService;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class UserGroupRolesResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    private GroupUser updatedGroupUser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserUid(request.getAttributes().get("userUid").toString());
        environmentBrowser.setGroupUid(request.getAttributes().get("groupUid").toString());
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null) && (environmentBrowser.getUser() != null) && (environmentBrowser.getGroupUser() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER_GROUP_ROLES;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        Map<String, Role> roleMap = new HashMap<String, Role>();
        Set<Object> pagerSet = new HashSet<Object>();
        for (Role role : environmentBrowser.getGroupUser().getRoles()) {
            roleMap.put(role.getUid(), role);
            pagerSet.add(role);
        }
        pager.setPagerSet(pagerSet);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("user", environmentBrowser.getUser());
        values.put("group", environmentBrowser.getGroup());
        values.put("groupUser", environmentBrowser.getGroupUser());
        values.put("roleMap", roleMap);
        values.put("roles", siteService.getRoles(environmentBrowser.getEnvironment(), pager));
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            pager.setCurrentPage(getPage());
            obj.put("user", environmentBrowser.getUser().getJSONObject());
            obj.put("groupUser", environmentBrowser.getGroupUser().getJSONObject());
            GroupUser gu = siteService.getGroupUser(environmentBrowser.getGroup(), environmentBrowser.getUser());
            obj.put("roles", gu.getRoles());
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("groupUser", updatedGroupUser.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserGroupResource");
        if (isGet()) {
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            element.appendChild(environmentBrowser.getUser().getIdentityElement(document));
            element.appendChild(environmentBrowser.getGroupUser().getElement(document));
        } else if (isPost()) {
            element.appendChild(updatedGroupUser.getElement(document));
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

    // TODO: handle newRole as with other new resources
    // TODO: prevent duplicate instances
    @Override
    public void acceptRepresentation(Representation entity) {
        log.debug("acceptRepresentation");
        if (environmentBrowser.getUserActions().isAllowModify()) {
            Form form = getForm();
            // create new instance if submitted
            String roleUid = form.getFirstValue("roleUid");
            if (null != roleUid) {
                GroupUser groupUser = environmentBrowser.getGroupUser();
                Role role = siteService.getRoleByUid(environmentBrowser.getEnvironment(), roleUid);
                if (role != null) {
                    groupUser.getRoles().add(role);
                    updatedGroupUser = groupUser;
                }
            }
            if (updatedGroupUser != null) {
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
