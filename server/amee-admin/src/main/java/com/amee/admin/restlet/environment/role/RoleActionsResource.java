package com.amee.admin.restlet.environment.role;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.admin.service.app.AppService;
import com.amee.domain.Pager;
import com.amee.domain.auth.Action;
import com.amee.domain.auth.Role;
import com.amee.domain.site.App;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentConstants;
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
public class RoleActionsResource extends BaseResource implements Serializable {

    @Autowired
    private AppService appService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setRoleUid(request.getAttributes().get("roleUid").toString());
        setPage(request);
        setPagerSetType(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getRole() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_ROLE_ACTIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Role role = environmentBrowser.getRole();
        Pager pager = getPager(getItemsPerPage());
        Map<String, Action> actionMap = new HashMap<String, Action>();
        Set<Object> pagerSet = new HashSet<Object>();
        for (Action action : role.getActions()) {
            actionMap.put(action.getUid(), action);
            pagerSet.add(action);
        }
        pager.setPagerSet(pagerSet);
        List<Action> actions = appService.getActions(pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("role", environmentBrowser.getRole());
        values.put("actions", actions);
        values.put("actionMap", actionMap);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("role", environmentBrowser.getRole().getJSONObject());
        JSONArray actionsArr = new JSONArray();
        for (Action action : environmentBrowser.getRole().getActions()) {
            actionsArr.put(action.getJSONObject());
        }
        obj.put("actions", actionsArr);
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("RoleActionsResource");
        element.appendChild(environmentBrowser.getRole().getIdentityElement(document));
        Element actionsElement = document.createElement("Actions");
        for (Action action : environmentBrowser.getRole().getActions()) {
            actionsElement.appendChild(action.getElement(document));
        }
        element.appendChild(actionsElement);
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

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void acceptRepresentation(Representation entity) {
        log.debug("post");
        if (environmentBrowser.getRoleActions().isAllowModify()) {
            Form form = getForm();
            // create new instance if submitted
            String appUid = form.getFirstValue("appUid");
            String actionUid = form.getFirstValue("actionUid");
            if ((appUid != null) && (actionUid != null)) {
                // find the App
                App app = appService.getAppByUid(appUid);
                if (app != null) {
                    // find the Action
                    Action action = appService.getActionByUid(app, actionUid);
                    if (action != null) {
                        // addItemValue the Action
                        environmentBrowser.getRole().add(action);
                        success();
                    } else {
                        badRequest();
                    }
                } else {
                    badRequest();
                }
            } else {
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }
}
