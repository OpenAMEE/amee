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
package gc.carbon.profile;

import gc.carbon.BaseFilter;
import gc.carbon.CarbonBeans;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemGroup;
import gc.carbon.path.PathItemService;
import org.apache.log4j.Logger;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.Component;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.util.List;

import com.jellymold.kiwi.Environment;

public class ProfileFilter extends BaseFilter {

    private final static Logger log = Logger.getLogger(ProfileFilter.class);

    public ProfileFilter() {
        super();
    }

    public ProfileFilter(Application application) {
        super(application);
    }

    protected void beforeHandle(Request request, Response response) {
        log.debug("before handle");
        rewrite(request);
    }

    protected void afterHandle(Request request, Response response) {
        log.debug("after handle");
    }

    protected void rewrite(Request request) {
        log.info("start profile path rewrite");
        String path = null;
        Environment environment = (Environment) Component.getInstance("environment");
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        removeEmptySegmentAtEnd(segments);
        segments.remove(0); // remove '/profiles'
        if (segments.size() > 0) {
            String segment = segments.remove(0);
            if (!matchesReservedPaths(segment)) {
                // look for Profile matching path
                ProfileService profileService = CarbonBeans.getProfileService();
                Profile profile = profileService.getProfile(segment);
                if (profile != null) {
                    // we found a Profile
                    // make available in Seam contexts
                    Contexts.getEventContext().set("profile", profile);
                    Contexts.getEventContext().set("permission", profile.getPermission());
                    // look for path match
                    PathItemService pathItemService = CarbonBeans.getPathItemService();
                    PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environment, profile);
                    PathItem pathItem = pathItemGroup.findBySegments(segments);
                    if (pathItem != null) {
                        // rewrite paths
                        Contexts.getEventContext().set("pathItem", pathItem);
                        path = pathItem.getInternalPath();
                        if (path != null) {
                            path = "/profiles" + path;
                        }
                    }
                }
            }
        }
        if (path != null) {
            // rewrite paths
            request.getAttributes().put("previousResourceRef", reference.toString());
            reference.setPath(path);
        }
        log.info("end profile path rewrite");
    }

    // TODO: add more reserved paths
    protected boolean matchesReservedPaths(String segment) {
        return (segment.equalsIgnoreCase("stats") ||
                segment.equalsIgnoreCase("js"));
    }
}