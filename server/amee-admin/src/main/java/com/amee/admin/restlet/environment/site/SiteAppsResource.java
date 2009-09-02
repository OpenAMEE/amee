package com.amee.admin.restlet.environment.site;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.admin.service.app.AppService;
import com.amee.domain.AMEEEntity;
import com.amee.domain.Pager;
import com.amee.domain.site.App;
import com.amee.domain.site.Site;
import com.amee.domain.site.SiteApp;
import com.amee.restlet.AuthorizeResource;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class SiteAppsResource extends AuthorizeResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private AppService appService;

    private SiteApp newSiteApp;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setSiteUid(request.getAttributes().get("siteUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getSite() != null);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(environmentBrowser.getEnvironment());
        entities.add(environmentBrowser.getSite());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_SITE_APPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(getItemsPerPage());
        List<SiteApp> siteApps = siteService.getSiteApps(environmentBrowser.getSite(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("site", environmentBrowser.getSite());
        values.put("siteApps", siteApps);
        values.put("apps", siteService.getApps());
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(getItemsPerPage());
            List<SiteApp> siteApps = siteService.getSiteApps(environmentBrowser.getSite(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getIdentityJSONObject());
            obj.put("site", environmentBrowser.getSite().getIdentityJSONObject());
            JSONArray siteAppsArr = new JSONArray();
            for (SiteApp siteApp : siteApps) {
                siteAppsArr.put(siteApp.getJSONObject());
            }
            obj.put("siteApps", siteAppsArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("siteApp", newSiteApp.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SiteAppsResource");
        if (isGet()) {
            Pager pager = getPager(getItemsPerPage());
            List<SiteApp> siteApps = siteService.getSiteApps(environmentBrowser.getSite(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            element.appendChild(environmentBrowser.getSite().getIdentityElement(document));
            Element sitesElement = document.createElement("SiteApps");
            for (SiteApp siteApp : siteApps) {
                sitesElement.appendChild(siteApp.getElement(document));
            }
            element.appendChild(sitesElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newSiteApp.getElement(document));
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
        String appUid = form.getFirstValue("appUid");
        if (appUid != null) {
            App app = appService.getAppByUid(appUid);
            if (app != null) {
                Site site = environmentBrowser.getSite();
                newSiteApp = new SiteApp(app, site);
                newSiteApp.setSkinPath(form.getFirstValue("skinPath"));
                site.add(newSiteApp);
            }
        }
        if (newSiteApp != null) {
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
