package com.amee.admin.restlet.auth;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.base.utils.XMLUtils;
import com.amee.domain.auth.User;
import com.amee.restlet.BaseResource;
import com.amee.restlet.auth.AuthUtils;
import com.amee.service.auth.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class SignInResource extends BaseResource implements Serializable {

    public final static String VIEW_SIGN_IN = "auth/signIn.ftl";

    @Autowired
    protected AuthenticationService authenticationService;

    @Override
    public String getTemplatePath() {
        return VIEW_SIGN_IN;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        User activeUser = getActiveUser();
        JSONObject obj = new JSONObject();
        obj.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        if (activeUser != null) {
            obj.put("auth", activeUser.getJSONObject(false));
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        User activeUser = getActiveUser();
        Element element = document.createElement("SignInResource");
        element.appendChild(XMLUtils.getElement(document, "Next", AuthUtils.getNextUrl(getRequest(), getForm())));
        if (activeUser != null) {
            element.appendChild(activeUser.getElement(document, false));
        }
        return element;
    }

    @Override
    public void handleGet() {
        Request request = getRequest();
        Response response = getResponse();
        if (getActiveSite().isSecureAvailable() && !request.getResourceRef().getSchemeProtocol().equals(Protocol.HTTPS)) {
            // bounce to HTTPS
            response.setLocationRef("https://" +
                    request.getResourceRef().getHostDomain() +
                    "/auth/signIn?next=" + AuthUtils.getNextUrl(request));
            response.setStatus(Status.REDIRECTION_FOUND);
        } else {
            super.handleGet();
        }
    }

    public boolean allowPost() {
        return true;
    }

    public void acceptRepresentation(Representation entity) {
        storeRepresentation(entity);
    }

    public boolean allowPut() {
        return true;
    }

    public void storeRepresentation(Representation entity) {
        Form form = getForm();
        String nextUrl;
        User sampleUser;
        User activeUser;
        String authToken;
        if (!StringUtils.isBlank(form.getFirstValue("username")) && !StringUtils.isBlank(form.getFirstValue("password"))) {
            // deal with sign in
            nextUrl = AuthUtils.getNextUrl(getRequest(), getForm());
            sampleUser = new User();
            sampleUser.setUsername(form.getFirstValue("username"));
            sampleUser.setPasswordInClear(form.getFirstValue("password"));
            activeUser = authenticationService.authenticate(sampleUser);
            if (activeUser != null) {
                // put active user in context
                getRequest().getAttributes().put("activeUser", activeUser);
                ThreadBeanHolder.set(User.class, activeUser);
                // create AuthToken and add to response
                authToken = authenticationService.generateAuthToken(activeUser, getRequest().getClientInfo().getAddress());
                AuthUtils.addAuthCookie(getResponse(), authToken);
                AuthUtils.addAuthHeader(getResponse(), authToken);
                // different response for API calls
                if (isStandardWebBrowser()) {
                    // go to 'next' page - which IS a protected resource
                    if (!nextUrl.endsWith("/auth/signIn")) {
                        success(nextUrl);
                    } else {
                        success("/auth");
                    }
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                // clear contexts
                getRequest().getAttributes().put("activeUser", null);
                ThreadBeanHolder.set(User.class, null);
                // not signed in
                AuthUtils.discardAuthCookie(getResponse());
                // show auth page again
                success("/auth/signIn?next=" + nextUrl);
            }
        } else {
            if (isStandardWebBrowser()) {
                // For standard browsers, return to signin page.
                super.handleGet();
            } else {
                // Otherwise, send back a 400 status.
                badRequest();
            }
        }
    }
}