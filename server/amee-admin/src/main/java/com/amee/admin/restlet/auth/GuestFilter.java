package com.amee.admin.restlet.auth;

import com.amee.restlet.auth.AuthUtils;
import com.amee.restlet.auth.BaseAuthFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * GuestFilter will ensure that that at least one User is signed-in. Firstly a previously authenticated 'real' User
 * will be looked for and validated. If no real user is available then the 'Guest' user for the current Site
 * is signed-in instead.
 */
public class GuestFilter extends BaseAuthFilter {

    private final Log log = LogFactory.getLog(getClass());

    public GuestFilter(Application application) {
        super(application);
    }

    public int doHandle(Request request, Response response) {
        log.debug("do handle");
        int result = CONTINUE;
        String authToken = authenticated(request);
        if (authToken != null) {
            accept(request, response, authToken);
        } else {
            AuthUtils.discardAuthCookie(response);
            if (authService.doGuestSignIn() != null) {
                // a guest auth has been found, authenticated and signed in
                result = super.doHandle(request, response);
            } else {
                // no auth available
                response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                result = STOP;
            }
        }
        return result;
    }
}