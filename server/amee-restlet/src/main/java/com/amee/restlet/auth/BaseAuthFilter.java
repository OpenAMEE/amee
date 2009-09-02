package com.amee.restlet.auth;

import com.amee.restlet.BaseFilter;
import com.amee.restlet.utils.HeaderUtils;
import com.amee.restlet.utils.MediaTypeUtils;
import com.amee.service.auth.AuthenticationService;
import org.restlet.Application;
import org.restlet.data.Cookie;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseAuthFilter extends BaseFilter {

    @Autowired
    protected AuthenticationService authenticationService;

    public BaseAuthFilter(Application application) {
        super(application.getContext());
    }

    protected String authenticated(Request request) {
        return authenticationService.isAuthenticated(
                getActiveEnvironment(),
                getActiveSite(),
                getAuthToken(request),
                request.getClientInfo().getAddress());
    }

    protected String getAuthToken(Request request) {
        String authToken = null;
        Cookie cookie = null;
        try {
            cookie = request.getCookies().getFirst(AuthenticationService.AUTH_TOKEN);
        } catch (Exception e) {
            // swallow
        }
        if (cookie != null) {
            // first look in cookie
            authToken = cookie.getValue();
        }
        if (authToken == null) {
            // next, look in header as token not found in cookie
            authToken = HeaderUtils.getHeaderFirstValue(AuthenticationService.AUTH_TOKEN, request);
        }
        if (authToken == null) {
            //next, look in query string
            authToken = request.getResourceRef().getQueryAsForm().getFirstValue(AuthenticationService.AUTH_TOKEN);
        }
        return authToken;
    }

    protected int accept(Request request, Response response, String authToken) {
        AuthUtils.addAuthCookie(response, authToken);
        AuthUtils.addAuthHeader(response, authToken);
        return super.doHandle(request, response);
    }

    protected void reject(Request request, Response response) {
        if (MediaTypeUtils.isStandardWebBrowser(request)) {
            if (getActiveSite().isSecureAvailable()) {
                // bounce to HTTPS
                response.setLocationRef("https://" +
                        request.getResourceRef().getHostDomain() +
                        "/auth/protected?next=" + AuthUtils.getNextUrl(request));
            } else {
                // bounce to HTTP
                response.setLocationRef("http://" +
                        request.getResourceRef().getHostDomain() +
                        "/auth/protected?next=" + AuthUtils.getNextUrl(request));
            }
            response.setStatus(Status.REDIRECTION_FOUND);
        } else {
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
    }
}
