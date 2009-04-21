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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.restlet.data;

import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.restlet.RewriteFilter;
import com.amee.service.path.PathItemService;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DataFilter extends RewriteFilter {

    @Autowired
    private PathItemService pathItemService;

    public DataFilter(Application application) {
        super(application);
    }

    @Override
    protected int rewrite(Request request, Response response) {
        log.debug("rewrite() - start data path rewrite ");
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        removeEmptySegmentAtEnd(segments);
        segments.remove(0); // remove '/data'
        if (!skipRewrite(segments)) {
            // handle suffixes
            String suffix = handleSuffix(segments);
            // look for path match
            Environment environment = (Environment) request.getAttributes().get("environment");
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environment);
            PathItem pathItem = pathItemGroup.findBySegments(segments, false);
            if (pathItem != null) {
                // found matching path, rewrite
                String path = pathItem.getInternalPath() + suffix;
                request.getAttributes().put("pathItem", pathItem);
                request.getAttributes().put("previousResourceRef", reference.toString());
                reference.setPath("/data" + path);
            } else {
                // nothing to be found, 404
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return STOP;
            }
        }
        log.debug("rewrite() - end data path rewrite");
        return CONTINUE;
    }

    protected boolean skipRewrite(List<String> segments) {
        return (segments.size() > 0) && matchesReservedPrefixes(segments.get(0));
    }

    protected boolean matchesReservedPrefixes(String segment) {
        return segment.equalsIgnoreCase("actions");
    }

    protected String handleSuffix(List<String> segments) {
        if (segments.size() > 0) {
            String segment = segments.get(segments.size() - 1);
            if ("drill".equalsIgnoreCase(segment)) {
                return "/" + segments.remove(segments.size() - 1);
            }
        }
        return "";
    }
}