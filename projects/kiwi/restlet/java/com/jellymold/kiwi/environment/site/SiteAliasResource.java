package com.jellymold.kiwi.environment.site;

import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.SiteAlias;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Component
@Scope("prototype")
public class SiteAliasResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    public SiteAliasResource() {
        super();
    }

    public SiteAliasResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setSiteUid(request.getAttributes().get("siteUid").toString());
        environmentBrowser.setSiteAliasUid(request.getAttributes().get("siteAliasUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getSiteAlias() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_SITE_ALIAS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("site", environmentBrowser.getSite());
        values.put("siteAlias", environmentBrowser.getSiteAlias());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("site", environmentBrowser.getSite().getJSONObject());
        obj.put("siteAlias", environmentBrowser.getSiteAlias().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SiteAliasResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getSite().getIdentityElement(document));
        element.appendChild(environmentBrowser.getSiteAlias().getElement(document));
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
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicate server aliases
    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation");
        if (environmentBrowser.getSiteAliasActions().isAllowModify()) {
            Form form = getForm();
            SiteAlias siteAlias = environmentBrowser.getSiteAlias();
            // update values
            if (form.getNames().contains("name")) {
                siteAlias.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("description")) {
                siteAlias.setDescription(form.getFirstValue("description"));
            }
            if (form.getNames().contains("serverAlias")) {
                siteAlias.setServerAlias(form.getFirstValue("serverAlias"));
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
    public void removeRepresentations() {
        if (environmentBrowser.getSiteAliasActions().isAllowDelete()) {
            Site site = environmentBrowser.getSite();
            SiteAlias siteAlias = environmentBrowser.getSiteAlias();
            site.remove(siteAlias);
            siteService.remove(siteAlias);
            success();
        } else {
            notAuthorized();
        }
    }
}