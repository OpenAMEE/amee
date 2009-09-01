/**
 * This file is part of AMEE.
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.restlet;

import com.amee.domain.auth.User;
import org.apache.log4j.MDC;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

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