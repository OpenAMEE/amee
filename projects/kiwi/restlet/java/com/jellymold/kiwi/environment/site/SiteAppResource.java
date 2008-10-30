package com.jellymold.kiwi.environment.site;

import com.jellymold.kiwi.App;
import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.SiteApp;
import com.jellymold.kiwi.app.AppService;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.SiteService;
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
public class SiteAppResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private AppService appService;

    @Autowired
    private Environment environment;

    public SiteAppResource() {
        super();
    }

    public SiteAppResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setSiteUid(request.getAttributes().get("siteUid").toString());
        environmentBrowser.setSiteAppUid(request.getAttributes().get("siteAppUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getSiteApp() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_SITE_APP;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("site", environmentBrowser.getSite());
        values.put("siteApp", environmentBrowser.getSiteApp());
        values.put("apps", siteService.getApps());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("site", environmentBrowser.getSite().getJSONObject());
        obj.put("siteApp", environmentBrowser.getSiteApp().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SiteAppResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getSite().getIdentityElement(document));
        element.appendChild(environmentBrowser.getSiteApp().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getSiteAppActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (environmentBrowser.getSiteAppActions().isAllowModify()) {
            Form form = getForm();
            SiteApp siteApp = environmentBrowser.getSiteApp();
            // update values
            String appUid = form.getFirstValue("appUid");
            if (appUid != null) {
                App app = appService.getAppByUid(appUid);
                if (app != null) {
                    siteApp.setApp(app);
                }
            }
            if (form.getNames().contains("uriPattern")) {
                siteApp.setUriPattern(form.getFirstValue("uriPattern"));
            }
            if (form.getNames().contains("skinPath")) {
                siteApp.setSkinPath(form.getFirstValue("skinPath"));
            }
            if (form.getNames().contains("defaultApp")) {
                siteApp.setDefaultApp(Boolean.valueOf(form.getFirstValue("defaultApp")));
            }
            if (form.getNames().contains("enabled")) {
                siteApp.setEnabled(Boolean.valueOf(form.getFirstValue("enabled")));
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

    @Override
    public void delete() {
        if (environmentBrowser.getSiteAppActions().isAllowDelete()) {
            Site site = environmentBrowser.getSite();
            SiteApp siteApp = environmentBrowser.getSiteApp();
            site.remove(siteApp);
            siteService.remove(siteApp);
            success();
        } else {
            notAuthorized();
        }
    }
}
