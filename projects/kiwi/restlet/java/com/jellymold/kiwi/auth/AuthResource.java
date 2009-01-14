package com.jellymold.kiwi.auth;

import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.User;
import com.jellymold.kiwi.auth.AuthService;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.BaseResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class AuthResource extends BaseResource implements Serializable {

    public final static String VIEW_AUTH = "auth/home.ftl";

    @Autowired
    private AuthService authService;

    public AuthResource() {
        super();
    }

    public AuthResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    public String getTemplatePath() {
        return VIEW_AUTH;
    }

    // TODO: Everything below here is kept for backwards compatibility.
    // TODO: This should be phased out in favour of SignInResource.

    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        return values;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        return obj;
    }

    @Override
    public void handleGet() {
        Site site = SiteService.getSite();
        Request request = getRequest();
        Response response = getResponse();
        if (site.isSecureAvailable() && !request.getResourceRef().getSchemeProtocol().equals(Protocol.HTTPS)) {
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
        User user;
        String authToken;
        if (form.getNames().contains("username")) {
            // deal with sign in
            String nextUrl = AuthUtils.getNextUrl(getRequest(), getForm());
            user = new User();
            user.setUsername(form.getFirstValue("username"));
            user.setPassword(form.getFirstValue("password"));
            authToken = authService.authenticateAndGenerateAuthToken(user, getRequest().getClientInfo().getAddress());
            if (authToken != null) {
                // signed in
                AuthUtils.addAuthCookie(getResponse(), authToken);
                AuthUtils.addAuthHeader(getResponse(), authToken);
                // go to 'next' page - which IS a protected resource
                if (!nextUrl.endsWith("/auth/signIn")) {
                    success(nextUrl);
                } else {
                    success("/auth");
                }
            } else {
                // not signed in
                AuthUtils.discardAuthCookie(getResponse());
                // show auth page again
                success("/auth/signIn?next=" + nextUrl);
            }
        } else {
            badRequest();
        }
    }
}