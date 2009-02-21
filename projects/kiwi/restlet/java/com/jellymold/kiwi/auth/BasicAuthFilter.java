package com.jellymold.kiwi.auth;

import com.jellymold.kiwi.User;
import gc.carbon.auth.AuthService;
import org.restlet.Application;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Basic Authentication Filter.
 * 
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

    @Autowired
    private AuthService authService;

    public BasicAuthFilter(Application application) {
        super(application.getContext(), ChallengeScheme.HTTP_BASIC, "AMEE");
    }

    @Override
    public int doHandle(Request request, Response response) {
        if (request.getChallengeResponse() != null) {
            return super.doHandle(request, response);
        } else {
            getNext().handle(request, response);
            return CONTINUE;
        }
    }

    @Override
    public boolean checkSecret(Request request, String identifer, char[] secret) {
        User user = new User();
        user.setUsername(identifer);
        user.setPasswordInClear(new String(secret));
        return authService.authenticate(user);
    }
}
