package com.amee.restlet;

import com.amee.domain.auth.User;
import org.slf4j.MDC;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Adds the client IP address and user UID for the current request to the MDC.
 * See: http://logback.qos.ch/manual/mdc.html
 */
public class LogFilter extends Filter {

    public LogFilter() {
        super();
    }

    public LogFilter(Application application) {
        super(application.getContext());
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    protected int doHandle(Request request, Response response) {
        try {
            MDC.put("ipAddress", request.getClientInfo().getAddress());
            User activeUser = (User) request.getAttributes().get("activeUser");
            if (activeUser != null) {
                MDC.put("userUid", activeUser.getUid());
            }
            return super.doHandle(request, response);
        } finally {
            MDC.remove("ipAddress");
            MDC.remove("userUid");
        }
    }
}