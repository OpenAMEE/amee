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
package gc.engine;

import gc.seam.JndiList;
import org.jboss.seam.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

public class JndiHandler extends Restlet {

    public JndiHandler() {
        super(null);
    }

    public JndiHandler(Context context) {
        super(context);
    }

    public void handle(Request request, Response response) {
        JndiList jndiList = (JndiList) Component.getInstance("jndiList", true);
        response.setEntity(new StringRepresentation(jndiList.getList(), MediaType.TEXT_HTML));
    }
}
