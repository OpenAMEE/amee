package com.jellymold.kiwi.app;

import com.jellymold.kiwi.App;
import com.jellymold.utils.BaseResource;
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
public class AppResource extends BaseResource {

    @Autowired
    private AppService appService;

    @Autowired
    private AppBrowser appBrowser;

    public AppResource() {
        super();
    }

    public AppResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        appBrowser.setAppUid(request.getAttributes().get("appUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (appBrowser.getApp() != null);
    }

    @Override
    public String getTemplatePath() {
        return KiwiAppConstants.VIEW_APP;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", appBrowser);
        values.put("app", appBrowser.getApp());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("app", appBrowser.getApp().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("AppResource");
        element.appendChild(appBrowser.getApp().getElement(document));
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
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicate names
    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (appBrowser.getAppActions().isAllowModify()) {
            Form form = getForm();
            App app = appBrowser.getApp();
            // update values
            if (form.getNames().contains("name")) {
                app.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("description")) {
                app.setDescription(form.getFirstValue("description"));
            }
            if (form.getNames().contains("authenticationRequired")) {
                app.setAuthenticationRequired(Boolean.valueOf(form.getFirstValue("authenticationRequired")));
            }
            if (form.getNames().contains("filterNames")) {
                app.setFilterNames(form.getFirstValue("filterNames"));
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

    // TODO: Prevent deletion of the current app?!
    @Override
    public void delete() {
        if (appBrowser.getAppActions().isAllowDelete()) {
            appService.remove(appBrowser.getApp());
            success();
        } else {
            notAuthorized();
        }
    }
}
