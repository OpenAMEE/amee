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
package gc.carbon.data;

import com.jellymold.kiwi.Environment;
import gc.carbon.BaseFilter;
import gc.carbon.CarbonBeans;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemGroup;
import gc.carbon.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.util.List;

public class DataFilter extends BaseFilter {

    private final Log log = LogFactory.getLog(getClass());

    public DataFilter() {
        super();
    }

    public DataFilter(Application application) {
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
        log.debug("start data path rewrite");
        String path = null;
        // TODO: Springify
        Environment environment = null; // (Environment) Component.getInstance("environment");
        Reference reference = request.getResourceRef();
        List<String> segments = reference.getSegments();
        // TODO: Springify
        // removeEmptySegmentAtEnd(segments);
        segments.remove(0); // remove '/data'
        if (!skipRewrite(segments)) {
            // handle suffixes
            String suffix = handleSuffix(segments);
            // look for path match
            PathItemService pathItemService = CarbonBeans.getPathItemService();
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environment);
            PathItem pathItem = pathItemGroup.findBySegments(segments);
            if (pathItem != null) {
                // found matching path, rewrite
                // TODO: Springify
                // Contexts.getEventContext().set("pathItem", pathItem);
                path = pathItem.getInternalPath() + suffix;
            }
        }
        if (path != null) {
            // rewrite paths
            request.getAttributes().put("previousResourceRef", reference.toString());
            reference.setPath("/data" + path);
        }
        log.debug("end data path rewrite");
        return CONTINUE;
    }

    protected boolean skipRewrite(List<String> segments) {
        return (segments.size() > 0) && matchesReservedPrefixes(segments.get(0));
    }

    protected boolean matchesReservedPrefixes(String segment) {
        return segment.equalsIgnoreCase("upload") ||
                segment.equalsIgnoreCase("admin") ||
                segment.equalsIgnoreCase("js");
    }

    protected String handleSuffix(List<String> segments) {
        if (segments.size() > 0) {
            String segment = segments.get(segments.size() - 1);
            if ("sheet".equalsIgnoreCase(segment) ||
                    "drill".equalsIgnoreCase(segment)) {
                return "/" + segments.remove(segments.size() - 1);
            }
        }
        return "";
    }
}
