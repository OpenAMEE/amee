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

import com.jellymold.kiwi.Environment;
import gc.carbon.BaseFilter;
import gc.carbon.CarbonBeans;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.path.PathItemGroup;
import gc.carbon.domain.profile.Profile;
import gc.carbon.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.util.List;

public class ProfileFilter extends BaseFilter {

    private final Log log = LogFactory.getLog(getClass());

    public ProfileFilter() {
        super();
    }

    public ProfileFilter(Application application) {
        super(application);
    }

    protected int beforeHandle(Request request, Response response) {
        log.debug("before handle");
        return rewrite(request);
    }

    protected void afterHandle(Request request, Response response) {
        log.debug("after handle");
    }

    protected int rewrite(Request request) {
        log.info("start profile path rewrite");
        String path = null;
        // TODO: Springify
        Environment environment = null; // (Environment) Component.getInstance("environment");
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        // TODO: Springify
        // removeEmptySegmentAtEnd(segments);
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
                    // TODO: Springify
                    // Contexts.getEventContext().set("profile", profile);
                    // Contexts.getEventContext().set("permission", profile.getPermission());
                    // look for path match
                    PathItemService pathItemService = CarbonBeans.getPathItemService();
                    PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environment, profile);
                    PathItem pathItem = pathItemGroup.findBySegments(segments);
                    if (pathItem != null) {
                        // rewrite paths
                        // TODO: Springify
                        // Contexts.getEventContext().set("pathItem", pathItem);
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
        return CONTINUE;
    }

    // TODO: add more reserved paths
    protected boolean matchesReservedPaths(String segment) {
        return (segment.equalsIgnoreCase("stats") ||
                segment.equalsIgnoreCase("js"));
    }
}