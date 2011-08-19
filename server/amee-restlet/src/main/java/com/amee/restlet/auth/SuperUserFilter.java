package com.amee.restlet.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * A filter to restrict access to the users of type {@link com.amee.domain.auth.UserType#SUPER}.
 * All other user types will receive a 403 Forbidden response.
 */
public class SuperUserFilter extends BaseAuthFilter {

    private final Log log = LogFactory.getLog(getClass());

    public SuperUserFilter(Application application) {
        super(application);
    }

    @Override
    public int doHandle(Request request, Response response) {
        log.debug("doHandle()");

        // Authentication has already been performed upstream
        if (getActiveUser() != null && getActiveUser().isSuperUser()) {
            return super.doHandle(request, response);
        } else {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return Filter.STOP;
        }
    }
}