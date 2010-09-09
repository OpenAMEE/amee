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

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.profile.Profile;
import com.amee.restlet.RequestContext;
import com.amee.restlet.data.DataFilter;
import com.amee.service.profile.ProfileService;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProfileFilter extends DataFilter {

    @Autowired
    private ProfileService profileService;

    public ProfileFilter(Application application) {
        super(application);
        handleAccept = true;
    }

    @Override
    protected int rewrite(Request request, Response response) {
        log.debug("rewrite() Start profile path rewrite.");
        String path = null;
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        removeEmptySegmentAtEnd(segments);
        RequestContext ctx = (RequestContext) ThreadBeanHolder.get("ctx");
        if (!skipRewrite(segments) && segments.get(0).equals("profiles")) {
            // Remove '/profiles'.
            segments.remove(0);
            // Only continue rewrite if we're looking for a Profile UID.
            if (segments.size() > 0) {
                // Extract Profile UID.
                String profileUid = segments.remove(0);
                // Handle suffixes.
                String suffix = handleSuffix(segments);
                // Look for Profile matching path.
                Profile profile = profileService.getProfile(profileUid);
                if (profile != null && !profile.isTrash()) {
                    // We found a Profile. Make available to request scope.
                    request.getAttributes().put("profile", profile);
                    ctx.setProfile(profile);
                    // Rewrite paths.
                    path = getInternalPath(segments);
                    if (path != null) {
                        // Always starts with /profiles.
                        path = "/profiles" + path;
                        // Rewrite paths.
                        request.getAttributes().put("previousResourceRef", reference.toString());
                        request.getAttributes().put("previousHierachicalPart", reference.getScheme() + ":" + reference.getHierarchicalPart());
                        reference.setPath(path + suffix);
                    }
                }
                // If a path wasn't found then we have a 404.
                if (path == null) {
                    response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return STOP;
                }
            }
        }
        log.debug("rewrite() End profile path rewrite.");
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