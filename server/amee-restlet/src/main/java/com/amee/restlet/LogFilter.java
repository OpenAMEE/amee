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
import com.amee.service.ThreadBeanHolder;
import org.apache.log4j.MDC;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Series;

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
            // Use the orignating IP address if available in the X-Cluster-Client-Ip header, otherwise use the client address
            Series<Parameter> headers = (Series) request.getAttributes().get("org.restlet.http.headers");
            String ipAddress = headers.getFirstValue("X-Cluster-Client-Ip", true, request.getClientInfo().getAddress());
            MDC.put("ipAddress", ipAddress);
            User user = (User) ThreadBeanHolder.get("user");
            if (user != null) {
                MDC.put("userUid", user.getUid());
            }
            return super.doHandle(request, response);
        } finally {
            MDC.remove("ipAddress");
            MDC.remove("userUid");
        }
    }
}