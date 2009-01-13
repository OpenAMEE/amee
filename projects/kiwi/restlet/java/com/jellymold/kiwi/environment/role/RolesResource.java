package com.jellymold.kiwi.environment.role;

import com.jellymold.kiwi.Role;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.kiwi.environment.EnvironmentService;
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class RolesResource extends BaseResource implements Serializable {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    private Role newRole;

    public RolesResource() {
        super();
    }

    public RolesResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_ROLES;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<Role> roles = siteService.getRoles(environmentBrowser.getEnvironment(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("roles", roles);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Role> roles = siteService.getRoles(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
            JSONArray rolesArr = new JSONArray();
            for (Role role : roles) {
                rolesArr.put(role.getJSONObject());
            }
            obj.put("roles", roles);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("group", newRole.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("RolesResource");
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Role> roles = siteService.getRoles(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            Element rolesElement = document.createElement("Roles");
            for (Role role : roles) {
                rolesElement.appendChild(role.getElement(document));
            }
            element.appendChild(rolesElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newRole.getElement(document));
        }
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getRoleActions().isAllowList()) {
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
    public void acceptRepresentation(Representation entity) {
        log.debug("post");
        if (environmentBrowser.getRoleActions().isAllowCreate()) {
            Form form = getForm();
            // create new instance if submitted
            if (form.getFirstValue("name") != null) {
                // create new instance
                newRole = new Role(environmentBrowser.getEnvironment());
                newRole.setName(form.getFirstValue("name"));
                newRole.setDescription(form.getFirstValue("description"));
                siteService.save(newRole);
            }
            if (newRole != null) {
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
