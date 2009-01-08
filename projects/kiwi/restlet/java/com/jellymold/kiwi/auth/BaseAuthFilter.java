package com.jellymold.kiwi.auth;

import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.HeaderUtils;
import com.jellymold.utils.MediaTypeUtils;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Cookie;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.context.ApplicationContext;

public class BaseAuthFilter extends Filter {

    public BaseAuthFilter(Application application) {
        super(application.getContext(), application);
    }

    public BaseAuthFilter(Context context, Application application) {
        super(context, application);
    }

    protected String authenticated(Request request) {
        ApplicationContext springContext = (ApplicationContext) request.getAttributes().get("springContext");
        AuthService authService = (AuthService) springContext.getBean("authService");
        return authService.isAuthenticated(
                getAuthToken(request),
                request.getClientInfo().getAddress());
    }

    protected String getAuthToken(Request request) {
        String authToken = null;
        Cookie cookie = null;
        try {
            cookie = request.getCookies().getFirst(AuthService.AUTH_TOKEN);
        } catch (Exception e) {
            // swallow
        }
        if (cookie != null) {
            // first look in cookie
            authToken = cookie.getValue();
        }
        if (authToken == null) {
            // next, look in header as token not found in cookie
            authToken = HeaderUtils.getHeaderFirstValue(AuthService.AUTH_TOKEN, request);
        }
        if (authToken == null) {
            //next, look in query string
            authToken = request.getResourceRef().getQueryAsForm().getFirstValue(AuthService.AUTH_TOKEN);
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
            Site site = SiteService.getSite();
            if (site.isSecureAvailable()) {
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
