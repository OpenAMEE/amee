package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.AdminBrowser;
import com.amee.admin.restlet.environment.AdminResource;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.LocaleConstants;
import com.amee.domain.auth.User;
import com.amee.service.environment.AdminConstants;
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
import java.util.TimeZone;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class UserResource extends AdminResource {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AdminBrowser adminBrowser;

    @Autowired
    private SiteService siteService;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        adminBrowser.setUserIdentifier(request.getAttributes().get("userUid").toString());
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        entities.add(adminBrowser.getUser());
        return entities;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (adminBrowser.getUser() != null);
    }

    @Override
    public String getTemplatePath() {
        return AdminConstants.VIEW_USER;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", adminBrowser);
        values.put("user", adminBrowser.getUser());
        values.put("apiVersions", adminBrowser.getApiVersions());
        values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("user", adminBrowser.getUser().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserResource");
        element.appendChild(adminBrowser.getUser().getElement(document));
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
        User user = adminBrowser.getUser();
        boolean ok = true;
        // update values
        if (form.getNames().contains("name")) {
            user.setName(form.getFirstValue("name"));
        }
        if (form.getNames().contains("username")) {
            String username = form.getFirstValue("username");
            if ((username.equalsIgnoreCase(user.getUsername()) || siteService.getUserByUsername(username) == null)
                && Pattern.matches("[A-Za-z][A-Za-z0-9_]+", username)) {
                    user.setUsername(username);
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
                user.setAPIVersion(adminBrowser.getApiVersion(form.getFirstValue("APIVersion")));
                if (user.getAPIVersion() == null) {
                    log.warn("Unable to find api version '" + form.getFirstValue("APIVersion") + "'");
                    badRequest();
                    return;
                }
            }
            if (form.getNames().contains("locale")) {
                String locale = form.getFirstValue("locale");
                if (LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                    user.setLocale(locale);
                }
            }
            if (form.getNames().contains("timeZone")) {
                TimeZone timeZone = TimeZone.getTimeZone(form.getFirstValue("timeZone"));
                user.setTimeZone(timeZone);
            }
            siteService.invalidate(user);
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
        siteService.remove(adminBrowser.getUser());
        success();
    }
}