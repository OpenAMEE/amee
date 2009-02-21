package com.jellymold.kiwi.app;

import com.jellymold.kiwi.Action;
import com.jellymold.utils.BaseResource;
import gc.carbon.app.AppServiceDAO;
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

import java.util.Map;

@Component
@Scope("prototype")
public class ActionResource extends BaseResource {

    @Autowired
    private AppServiceDAO appService;

    @Autowired
    private AppBrowser appBrowser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        appBrowser.setAppUid(request.getAttributes().get("appUid").toString());
        appBrowser.setActionUid(request.getAttributes().get("actionUid").toString());
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (appBrowser.getApp() != null) && (appBrowser.getAction() != null);
    }

    @Override
    public String getTemplatePath() {
        return AppConstants.VIEW_ACTION;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", appBrowser);
        values.put("app", appBrowser.getApp());
        values.put("action", appBrowser.getAction());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("app", appBrowser.getApp().getJSONObject());
        obj.put("action", appBrowser.getAction().getJSONObject());
        return obj;
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
    public Element getElement(Document document) {
        Element element = document.createElement("ActionResource");
        element.appendChild(appBrowser.getApp().getElement(document));
        element.appendChild(appBrowser.getAction().getElement(document));
        return element;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicates
    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("put");
        if (appBrowser.getAppActions().isAllowModify()) {
            Form form = getForm();
            Action action = appBrowser.getAction();
            // update values
            if (form.getNames().contains("name")) {
                action.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("key")) {
                action.setKey(form.getFirstValue("key"));
            }
            if (form.getNames().contains("description")) {
                action.setDescription(form.getFirstValue("description"));
            }
            success();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    // TODO: do not allow delete affecting logged-in user...
    @Override
    public void removeRepresentations() {
        if (appBrowser.getAppActions().isAllowModify()) {
            appBrowser.getApp().remove(appBrowser.getAction());
            appService.remove(appBrowser.getAction());
            success();
        } else {
            notAuthorized();
        }
    }
}
