package com.amee.admin.restlet.site;

import com.amee.admin.service.AppBrowser;
import com.amee.admin.service.AppConstants;
import com.amee.admin.service.app.AppService;
import com.amee.domain.Pager;
import com.amee.domain.auth.Action;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentService;
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
public class ActionsResource extends BaseResource implements Serializable {

    @Autowired
    private AppService appService;

    @Autowired
    private AppBrowser appBrowser;

    private Action newAction;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        appBrowser.setAppUid(request.getAttributes().get("appUid").toString());
        setPage(request);
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (appBrowser.getApp() != null);
    }

    @Override
    public String getTemplatePath() {
        return AppConstants.VIEW_ACTIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<Action> actions = appService.getActions(appBrowser.getApp(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", appBrowser);
        values.put("app", appBrowser.getApp());
        values.put("actions", actions);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Action> actions = appService.getActions(appBrowser.getApp(), pager);
            pager.setCurrentPage(getPage());
            obj.put("app", appBrowser.getApp().getJSONObject());
            JSONArray actionsArr = new JSONArray();
            for (Action action : actions) {
                actionsArr.put(action.getJSONObject());
            }
            obj.put("actions", actionsArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("action", newAction.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ActionsResource");
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Action> actions = appService.getActions(appBrowser.getApp(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(appBrowser.getApp().getElement(document));
            Element actionsElement = document.createElement("Actions");
            for (Action action : actions) {
                actionsElement.appendChild(action.getElement(document));
            }
            element.appendChild(actionsElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newAction.getElement(document));
        }
        return element;

    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (appBrowser.getAppActions().isAllowView()) {
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
        if (appBrowser.getAppActions().isAllowModify()) {
            Form form = getForm();
            // create new instance if submitted
            if (form.getFirstValue("name") != null) {
                // create new instance
                newAction = new Action(appBrowser.getApp());
                newAction.setName(form.getFirstValue("name"));
                newAction.setKey(form.getFirstValue("key"));
                newAction.setDescription(form.getFirstValue("description"));
                appService.save(newAction);
            }
            if (newAction != null) {
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
