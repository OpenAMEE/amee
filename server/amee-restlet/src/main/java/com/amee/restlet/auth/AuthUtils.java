package com.amee.restlet.auth;

import com.amee.domain.site.ISite;
import com.amee.restlet.utils.HeaderUtils;
import com.amee.service.auth.AuthenticationService;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class AuthUtils {

    public static void addAuthCookie(Response response, String authToken) {
        if (authToken != null) {
            CookieSetting authCookie =
                    new CookieSetting(
                            0,
                            AuthenticationService.AUTH_TOKEN, authToken,
                            "/",
                            getActiveSite().getAuthCookieDomain().length() > 0 ? getActiveSite().getAuthCookieDomain() : null);
            CookieSetting oldAuthCookie = response.getCookieSettings().getFirst(AuthenticationService.AUTH_TOKEN);
            if (oldAuthCookie != null) {
                response.getCookieSettings().remove(oldAuthCookie);
            }
            response.getCookieSettings().add(authCookie);
        }
    }

    public static void discardAuthCookie(Response response) {
        CookieSetting authCookie =
                new CookieSetting(
                        0,
                        AuthenticationService.AUTH_TOKEN,
                        "",
                        "/",
                        getActiveSite().getAuthCookieDomain().length() > 0 ? getActiveSite().getAuthCookieDomain() : null);
        authCookie.setMaxAge(0); // discard cookie now
        response.getCookieSettings().add(authCookie);
    }

    public static void addAuthHeader(Response response, String authToken) {
        if (authToken != null) {
            HeaderUtils.addHeader(AuthenticationService.AUTH_TOKEN, authToken, response);
        }
    }

    public static String getNextUrl(Request request) {
        return AuthUtils.getNextUrl(request, null);
    }

    public static String getNextUrl(Request request, Form form) {
        // first, look for 'next' in parameters
        Form parameters = request.getResourceRef().getQueryAsForm();
        String next = parameters.getFirstValue("next");
        if (((next == null) || next.length() == 0) && (form != null)) {
            // second, look for 'next' in form
            next = form.getFirstValue("next");
        }
        if ((next == null) || next.length() == 0) {
            // third, determine 'next' from the previousResourceRef, if set (by DataFilter and ProfileFilter perhaps)
            if (request.getAttributes().get("previousResourceRef") != null) {
                next = request.getAttributes().get("previousResourceRef").toString();
            }
        }
        if ((next == null) || next.length() == 0) {
            // forth, determine 'next' from current URL
            next = request.getResourceRef().toString();
            if ((next != null) && ((next.endsWith("/signIn") || next.endsWith("/signOut") || next.endsWith("/protected")))) {
                next = null;
            }
        }
        if ((next == null) || next.length() == 0) {
            // fifth, use a default
            next = "/auth";
        }
        return next;
    }

    public static ISite getActiveSite() {
        return (ISite) Request.getCurrent().getAttributes().get("activeSite");
    }
}