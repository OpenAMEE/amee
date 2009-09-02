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

import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.profile.Profile;
import com.amee.restlet.RequestContext;
import com.amee.restlet.RewriteFilter;
import com.amee.core.ThreadBeanHolder;
import com.amee.service.path.PathItemService;
import com.amee.service.profile.ProfileService;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProfileFilter extends RewriteFilter {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PathItemService pathItemService;

    public ProfileFilter(Application application) {
        super(application);
        handleAccept = true;
    }

    @Override
    protected int rewrite(Request request, Response response) {
        log.debug("rewrite() - start profile path rewrite");
        String path = null;
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        removeEmptySegmentAtEnd(segments);
        RequestContext ctx = (RequestContext) ThreadBeanHolder.get("ctx");
        if (!skipRewrite(segments) && segments.get(0).equals("profiles")) {
            // remove '/profiles'
            segments.remove(0);
            // only continue rewrite if we're looking for a Profile UID
            if (segments.size() > 0) {
                // extract Profile UID
                String profileUid = segments.remove(0);
                // handle suffixes
                String suffix = handleSuffix(segments);
                // look for Profile matching path
                Environment environment = (Environment) request.getAttributes().get("activeEnvironment");
                Profile profile = profileService.getProfile(environment, profileUid);
                if (profile != null && !profile.isTrash()) {
                    // we found a Profile. Make available to request scope.
                    request.getAttributes().put("profile", profile);
                    ctx.setProfile(profile);
                    // look for path match
                    PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environment);
                    PathItem pathItem = pathItemGroup.findBySegments(segments, true);
                    if (pathItem != null) {
                        // rewrite paths
                        path = pathItem.getInternalPath();
                        if (path != null) {
                            path = "/profiles" + path;
                            // rewrite paths
                            request.getAttributes().put("pathItem", pathItem);
                            request.getAttributes().put("previousResourceRef", reference.toString());
                            request.getAttributes().put("previousHierachicalPart", reference.getScheme() + ":" + reference.getHierarchicalPart().toString());
                            reference.setPath(path + suffix);
                        }
                    }
                }

                if (path == null) {
                    response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return STOP;
                }
            }
        }

        log.debug("rewrite() - end profile path rewrite");
        return CONTINUE;
    }

    @Override
    protected boolean matchesReservedPrefixes(String segment) {
        return segment.equalsIgnoreCase("actions");
    }

    @Override
    protected String handleSuffix(List<String> segments) {
        if (segments.size() > 0) {
            String segment = segments.get(segments.size() - 1);
            if ("service".equalsIgnoreCase(segment)) {
                return "/" + segments.remove(segments.size() - 1);
            }
        }
        return "";
    }
}