package com.amee.admin.restlet.site;

import com.amee.admin.service.AppBrowser;
import com.amee.admin.service.AppConstants;
import com.amee.admin.service.app.AppService;
import com.amee.domain.site.App;
import com.amee.restlet.BaseResource;
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

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        appBrowser.setAppUid(request.getAttributes().get("appUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (appBrowser.getApp() != null);
    }

    @Override
    public String getTemplatePath() {
        return AppConstants.VIEW_APP;
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
    public void storeRepresentation(Representation entity) {
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
            success();
        } else {
            notAuthorized();
        }
    }

    // TODO: See http://my.amee.com/developers/ticket/243 & http://my.amee.com/developers/ticket/242
    @Override
    public boolean allowDelete() {
        return false;
    }

    // TODO: See http://my.amee.com/developers/ticket/243 & http://my.amee.com/developers/ticket/242
    // TODO: Prevent deletion of the current app?!
    @Override
    public void removeRepresentations() {
        throw new UnsupportedOperationException();
//        if (appBrowser.getAppActions().isAllowDelete()) {
//            appService.remove(appBrowser.getApp());
//            success();
//        } else {
//            notAuthorized();
//        }
    }
}
