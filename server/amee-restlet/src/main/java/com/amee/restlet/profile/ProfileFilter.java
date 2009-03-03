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
package com.amee.restlet.profile;

import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.profile.Profile;
import com.amee.restlet.BaseFilter;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.path.PathItemService;
import com.amee.service.profile.ProfileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProfileFilter extends BaseFilter {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PathItemService pathItemService;

    public ProfileFilter() {
        super();
    }

    public ProfileFilter(Application application) {
        super(application);
    }

    protected int beforeHandle(Request request, Response response) {
        log.debug("beforeHandle()");
        setAccept(request);
        return rewrite(request, response);
    }

    protected void afterHandle(Request request, Response response) {
        log.debug("afterHandle()");
    }

    protected int rewrite(Request request, Response response) {
        log.info("rewrite() - start profile path rewrite");
        String path = null;
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        removeEmptySegmentAtEnd(segments);
        segments.remove(0); // remove '/profiles'
        if (segments.size() > 0) {
            String segment = segments.remove(0);
            if (!matchesReservedPaths(segment)) {
                // look for Profile matching path
                Profile profile = profileService.getProfile(segment);
                if (profile != null) {
                    // we found a Profile. Make available to request scope.
                    // TODO - remove from Threadlocal scope - only used in ProfilePIGFactory and these will be refactored-out in short order.
                    ThreadBeanHolder.set("profile", profile);
                    request.getAttributes().put("profile", profile);

                    ThreadBeanHolder.set("permission", profile.getPermission());
                    // look for path match
                    PathItemGroup pathItemGroup = pathItemService.getProfilePathItemGroup();
                    PathItem pathItem = pathItemGroup.findBySegments(segments);
                    if (pathItem != null) {

                        // rewrite paths
                        // TODO - remove from Threadlocal scope - only used in ProfilePIGFactory and these will be refactored-out in short order.
                        request.getAttributes().put("pathItem", pathItem);
                        ThreadBeanHolder.set("pathItem", pathItem);
                        path = pathItem.getInternalPath();
                        if (path != null) {
                            path = "/profiles" + path;
                        }
                    }
                }
            }

            //TODO - Quick fix to allow /service paths
            if (!segments.isEmpty() && segments.get(0).equals("service"))
                return CONTINUE;

            if (path != null) {
                // rewrite paths
                request.getAttributes().put("previousResourceRef", reference.toString());
                //TODO - There must be a better way of doing this...
                request.getAttributes().put("previousHierachicalPart", reference.getScheme() + ":" + reference.getHierarchicalPart().toString());
                reference.setPath(path);
            } else {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return STOP;
            }

        }
        log.info("rewrite() - end profile path rewrite");
        return CONTINUE;
    }

    protected boolean matchesReservedPaths(String segment) {
        return (segment.equalsIgnoreCase("stats") ||
                segment.equalsIgnoreCase("js"));
    }
}
