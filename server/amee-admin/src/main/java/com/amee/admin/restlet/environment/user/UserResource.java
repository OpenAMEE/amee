package com.amee.admin.restlet.environment.user;


import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.AMEEEntity;
import com.amee.domain.auth.User;
import com.amee.domain.data.LocaleName;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.service.environment.EnvironmentService;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class UserResource extends AuthorizeResource {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private SiteService siteService;

    @Autowired
    protected EnvironmentService environmentService;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserIdentifier(request.getAttributes().get("userUid").toString());
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getActiveEnvironment());
        entities.add(environmentBrowser.getEnvironment());
        entities.add(environmentBrowser.getUser());
        return entities;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null) && (environmentBrowser.getUser() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("user", environmentBrowser.getUser());
        values.put("apiVersions", environmentBrowser.getApiVersions());
        values.put("availableLocales", LocaleName.AVAILABLE_LOCALES.keySet());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("user", environmentBrowser.getUser().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getUser().getElement(document));
        return element;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicate username/password and enforce unique username per environment?
    // TODO: password validation
    @Override
    public void doStore(Representation entity) {
        log.debug("doStore");
        Form form = getForm();
        User user = environmentBrowser.getUser();
        boolean ok = true;
        // update values
        if (form.getNames().contains("name")) {
            user.setName(form.getFirstValue("name"));
        }
        if (form.getNames().contains("username")) {
            if (form.getFirstValue("username").equalsIgnoreCase(user.getUsername())
                    || siteService.getUserByUsername(environmentBrowser.getEnvironment(), form.getFirstValue("username")) == null) {
                user.setUsername(form.getFirstValue("username"));
            } else {
                ok = false;
            }
        }

        if (ok) {
            if (form.getNames().contains("type")) {
                user.setType(form.getFirstValue("type"));
            }
            if (form.getNames().contains("password")) {
                String password = form.getFirstValue("password");
                if ((password != null) && password.length() > 0) {
                    user.setPasswordInClear(password);
                }
            }
            if (form.getNames().contains("email")) {
                user.setEmail(form.getFirstValue("email"));
            }
            if (form.getNames().contains("APIVersion")) {
                user.setAPIVersion(environmentBrowser.getApiVersion(form.getFirstValue("APIVersion")));
                if (user.getAPIVersion() == null) {
                    log.warn("Unable to find api version '" + form.getFirstValue("APIVersion") + "'");
                    badRequest();
                    return;
                }
            }
            if (form.getNames().contains("locale")) {
                String locale = form.getFirstValue("locale");
                if (LocaleName.AVAILABLE_LOCALES.containsKey(locale)) {
                    user.setLocale(locale);
                }
            }
            success();
        } else {
            badRequest();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void doRemove() {
        siteService.remove(environmentBrowser.getUser());
        success();
    }
}