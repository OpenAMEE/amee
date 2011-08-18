package com.amee.restlet.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * A filter to restrict access to the users of type {@link com.amee.domain.auth.UserType#SUPER}
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
            reject(request, response);
            return Filter.STOP;
        }
    }
}