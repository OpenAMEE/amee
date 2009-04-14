package com.amee.admin.restlet.environment.group;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.auth.Group;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.service.environment.SiteService;
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
public class GroupResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setGroupUid(request.getAttributes().get("groupUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getGroup() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_GROUP;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("group", environmentBrowser.getGroup());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("group", environmentBrowser.getGroup().getJSONObject());
        return obj;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getGroupActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("GroupResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getGroup().getElement(document));
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
        if (environmentBrowser.getGroupActions().isAllowModify()) {
            Form form = getForm();
            Group group = environmentBrowser.getGroup();
            // update values
            if (form.getNames().contains("name")) {
                group.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("description")) {
                group.setDescription(form.getFirstValue("description"));
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

    // TODO: do not allow delete affecting logged-in auth...
    @Override
    public void removeRepresentations() {
        if (environmentBrowser.getGroupActions().isAllowDelete()) {
            siteService.remove(environmentBrowser.getGroup());
            success();
        } else {
            notAuthorized();
        }
    }
}