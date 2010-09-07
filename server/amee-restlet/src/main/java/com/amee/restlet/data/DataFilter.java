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

import com.amee.domain.data.DataCategory;
import com.amee.restlet.RewriteFilter;
import com.amee.service.data.DataService;
import org.apache.commons.lang.StringUtils;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DataFilter extends RewriteFilter {

    @Autowired
    private DataService dataService;

    public DataFilter(Application application) {
        super(application);
    }

    @Override
    protected int rewrite(Request request, Response response) {
        log.debug("rewrite() - start data path rewrite ");
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        removeEmptySegmentAtEnd(segments);
        if (!skipRewrite(segments) && segments.get(0).equals("data")) {
            // Remove '/data'.
            segments.remove(0);
            // Handle suffixes.
            String suffix = handleSuffix(segments);
            // Check for flat or hierarchical path.
            if (!segments.isEmpty() && StringUtils.equals(segments.get(0), "categories")) {
                // This is a 'flat' path request which matches the internal URI mappings - No rewrite is required.
                request.getAttributes().put("previousResourceRef", reference.toString());
            } else {
                // This is a hierarchical path request - We need to rewrite the request URI.
                // look for path match.
                String path = getInternalPath(segments);
                if (path != null) {
                    // Found matching path, rewrite.
                    path += suffix;
                    request.getAttributes().put("previousResourceRef", reference.toString());
                    reference.setPath("/data" + path);
                } else {
                    // Nothing found, 404.
                    response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return STOP;
                }
            }
        }
        log.debug("rewrite() - end data path rewrite");
        return CONTINUE;
    }

    protected String getInternalPath(List<String> segments) {
        String path;
        boolean inDataItem = false;
        boolean inItemValue = false;
        // We always need to start with the root Data Category.
        DataCategory dataCategory = dataService.getRootDataCategory();
        path = dataCategory.getUid();
        // Loop over all path segments and handle each.
        for (String segment : segments) {
            // Are we looking for Data Categories?
            if (inItemValue) {
                // If we have segments beyond the Data Item Value then 404.
                path = null;
                break;
            } else if (inDataItem) {
                // We're within a Data Item - We can assume this is an ItemValue.
                inItemValue = true;
                path += "/values/" + segment;
            } else {
                // We're looking for Data Categories.
                dataCategory = dataService.getDataCategoryByPath(dataCategory, segment);
                if (dataCategory != null) {
                    // We only want the UID of the last Data Category found.
                    path = dataCategory.getUid();
                } else {
                    // This is not a DataCategory - We can assume this is a DataItem.
                    inDataItem = true;
                    path += "/items/" + segment;
                }
            }
        }
        // Complete the internal path.
        if (path != null) {
            path = "/categories/" + path;
        }
        // Path should now be complete or be null for a 404.
        return path;
    }

    @Override
    protected boolean matchesReservedPrefixes(String segment) {
        return segment.equalsIgnoreCase("actions") ||
                segment.equalsIgnoreCase("dataItemLookup");
    }

    @Override
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