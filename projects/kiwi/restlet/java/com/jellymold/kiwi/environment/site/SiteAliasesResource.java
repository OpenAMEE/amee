package com.jellymold.kiwi.environment.site;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.SiteAlias;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class SiteAliasesResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private Environment environment;

    private SiteAlias newSiteAlias;

    public SiteAliasesResource() {
        super();
    }

    public SiteAliasesResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setSiteUid(request.getAttributes().get("siteUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getSite() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_SITE_ALIASES;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(environment.getItemsPerPage());
        List<SiteAlias> siteAliases = siteService.getSiteAliases(environmentBrowser.getSite(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("site", environmentBrowser.getSite());
        values.put("siteAliases", siteAliases);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<SiteAlias> siteAliases = siteService.getSiteAliases(environmentBrowser.getSite(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getIdentityJSONObject());
            obj.put("site", environmentBrowser.getSite().getIdentityJSONObject());
            JSONArray siteAliasesArr = new JSONArray();
            for (SiteAlias siteAlias : siteAliases) {
                siteAliasesArr.put(siteAlias.getJSONObject());
            }
            obj.put("siteAliases", siteAliasesArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("siteAlias", newSiteAlias.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SiteAliasesResource");
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<SiteAlias> siteAliases = siteService.getSiteAliases(environmentBrowser.getSite(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            element.appendChild(environmentBrowser.getSite().getIdentityElement(document));
            Element sitesElement = document.createElement("SiteAliases");
            for (SiteAlias siteAlias : siteAliases) {
                sitesElement.appendChild(siteAlias.getElement(document));
            }
            element.appendChild(sitesElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newSiteAlias.getElement(document));
        }
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getSiteAliasActions().isAllowView()) {
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
    public void post(Representation entity) {
        log.debug("post");
        if (environmentBrowser.getSiteAliasActions().isAllowModify()) {
            Form form = getForm();
            // create new instance if submitted
            if (form.getFirstValue("name") != null) {
                // create new instance
                Site site = environmentBrowser.getSite();
                newSiteAlias = new SiteAlias(site);
                newSiteAlias.setName(form.getFirstValue("name"));
                newSiteAlias.setServerAlias(form.getFirstValue("serverAlias"));
                site.add(newSiteAlias);
            }
            if (newSiteAlias != null) {
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
