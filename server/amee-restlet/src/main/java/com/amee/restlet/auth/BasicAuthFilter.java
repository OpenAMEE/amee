package com.amee.restlet.auth;

import com.amee.core.ThreadBeanHolder;
import com.amee.domain.LocaleConstants;
import com.amee.domain.LocaleHolder;
import com.amee.domain.auth.User;
import com.amee.domain.environment.Environment;
import com.amee.service.auth.AuthenticationService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic Authentication Filter.
 * <p/>
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
public class BasicAuthFilter extends Guard {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private AuthenticationService authenticationService;

    public BasicAuthFilter(Application application) {
        super(application.getContext(), ChallengeScheme.HTTP_BASIC, "AMEE");
    }

    @Override
    public int doHandle(Request request, Response response) {
        try {
            if (request.getChallengeResponse() != null) {
                return super.doHandle(request, response);
            } else {
                getNext().handle(request, response);
                return CONTINUE;
            }
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException: " + e.getMessage(), e);
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return STOP;
        }
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        User sampleUser = new User();
        sampleUser.setEnvironment((Environment) request.getAttributes().get("activeEnvironment"));
        sampleUser.setUsername(identifier);
        sampleUser.setPasswordInClear(new String(secret));
        User activeUser = authenticationService.authenticate(sampleUser);
        if (activeUser != null) {
            request.getAttributes().put("activeUser", activeUser);
            ThreadBeanHolder.set("activeUser", activeUser);

            // Set user or request locale information into the thread
            String locale = request.getResourceRef().getQueryAsForm().getFirstValue("locale");
            if (StringUtils.isBlank(locale) || !LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                locale = activeUser.getLocale();
            }
            LocaleHolder.set(LocaleHolder.KEY, locale);
        }
        return activeUser != null;
    }
}
