package com.jellymold.kiwi.auth;

import org.restlet.Application;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.kiwi.User;

/**
 * AuthFilter will ensure that a User is authenticated before allowing the request to continue. If a User
 * is not authenticated then the request will be redirected to the authentication resource.
 */
public class AuthFilter extends BaseAuthFilter {

    private final Log log = LogFactory.getLog(getClass());

    public AuthFilter(Application application) {
        super(application.getContext(), application);
    }

    public int doHandle(Request request, Response response) {

        log.debug("do handle");
        int result;

        String authToken = authenticated(request);
        if (authToken != null) {
            // a user has been found and authenticated (even if this is just the guest user)
            result = accept(request, response, authToken);

            //TODO - Move legacy mapping logic to own filter
            User user = (User) ThreadBeanHolder.get("user");
            if (user.getApiVersion() != null && user.getApiVersion().isVersionOne()) {
                request.getResourceRef().addQueryParameter("returnPerUnit","month");
            }
        } else {
            // this will only be executed if a guest user is not found (really?)
            reject(request, response);
            result = STOP;
        }

        return result;
    }

}