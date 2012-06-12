package com.amee.restlet.auth;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.LocaleConstants;
import com.amee.domain.LocaleHolder;
import com.amee.domain.auth.User;
import com.amee.restlet.RequestContext;
import com.amee.service.auth.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

public class BasicAuthFilter extends Guard {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private AuthenticationService authenticationService;

    public BasicAuthFilter(Application application) {
        super(application.getContext(), ChallengeScheme.HTTP_BASIC, "AMEE");
    }

    @Override
    public int doHandle(Request request, Response response) {
        boolean challengePresent;
        try {
            challengePresent = request.getChallengeResponse() != null;
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException: " + e.getMessage(), e);
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return STOP;
        }
        if (challengePresent) {
            return super.doHandle(request, response);
        } else {
            getNext().handle(request, response);
            return CONTINUE;
        }
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        User sampleUser = new User();
        sampleUser.setUsername(identifier);
        sampleUser.setPasswordInClear(new String(secret));
        User activeUser = authenticationService.authenticate(sampleUser);
        if (activeUser != null) {
            request.getAttributes().put("activeUser", activeUser);
            ThreadBeanHolder.set(User.class, activeUser);

            // setup RequestContext
            RequestContext ctx = ThreadBeanHolder.get(RequestContext.class);
            ctx.setUser(activeUser);
            ctx.setRequest(request);

            // Set user or request locale information into the thread
            String locale = request.getResourceRef().getQueryAsForm().getFirstValue("locale");
            if (StringUtils.isBlank(locale) || !LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                locale = activeUser.getLocale();
            }
            LocaleHolder.set(String.class, locale);
        }
        return activeUser != null;
    }
}
