package com.amee.admin.restlet.site;

import com.amee.admin.service.AppBrowser;
import com.amee.admin.service.AppConstants;
import com.amee.domain.AMEEEntity;
import com.amee.domain.site.App;
import com.amee.restlet.AuthorizeResource;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class AppResource extends AuthorizeResource {

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
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(appBrowser.getApp());
        return entities;
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
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicate names
    @Override
    public void doStore(Representation entity) {
        log.debug("doStore");
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
    }

    @Override
    public boolean allowDelete() {
        return false;
    }
}
