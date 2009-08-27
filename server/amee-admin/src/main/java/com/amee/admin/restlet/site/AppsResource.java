package com.amee.admin.restlet.site;

import com.amee.admin.service.AppBrowser;
import com.amee.admin.service.AppConstants;
import com.amee.admin.service.app.AppService;
import com.amee.domain.AMEEEntity;
import com.amee.domain.Pager;
import com.amee.domain.site.App;
import com.amee.restlet.AuthorizeResource;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class AppsResource extends AuthorizeResource implements Serializable {

    @Autowired
    private AppService appService;

    @Autowired
    private AppBrowser appBrowser;

    private App newApp;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        setPage(request);
    }

    @Override
    protected List<AMEEEntity> getEntities() {
        return new ArrayList<AMEEEntity>();
    }

    @Override
    public String getTemplatePath() {
        return AppConstants.VIEW_APPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<App> apps = appService.getApps(pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", appBrowser);
        values.put("apps", apps);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<App> apps = appService.getApps(pager);
            pager.setCurrentPage(getPage());
            JSONArray appsArr = new JSONArray();
            for (App app : apps) {
                appsArr.put(app.getJSONObject());
            }
            obj.put("apps", appsArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("app", newApp.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("AppsResource");
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<App> apps = appService.getApps(pager);
            pager.setCurrentPage(getPage());
            Element appsElement = document.createElement("Apps");
            for (App app : apps) {
                appsElement.appendChild(app.getElement(document));
            }
            element.appendChild(appsElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newApp.getElement(document));
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
        if (form.getFirstValue("name") != null) {
            // create new instance
            newApp = new App();
            newApp.setName(form.getFirstValue("name"));
            appService.save(newApp);
        }
        if (newApp != null) {
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
