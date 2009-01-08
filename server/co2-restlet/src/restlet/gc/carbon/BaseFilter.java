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
package gc.carbon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;

import java.util.List;

import com.jellymold.utils.MediaTypeUtils;

public abstract class BaseFilter extends Filter {

    private final Log log = LogFactory.getLog(getClass());

    public BaseFilter() {
        super();
    }

    public BaseFilter(Application application) {
        super(application.getContext(), application);
    }

    protected void removeEmptySegmentAtEnd(List<String> segments) {
        if (!segments.isEmpty()) {
            int last = segments.size() - 1;
            if (segments.get(last).length() == 0) {
                segments.remove(last);
            }
        }
    }

    protected void setAccept(Request request) {
        String accept = request.getResourceRef().getQueryAsForm().getFirstValue("accept");
        if (accept != null) {
            log.debug("setAccept() - accept: " + accept);
            MediaType mediaType = MediaType.valueOf(accept);
            if (mediaType != null) {
                MediaTypeUtils.forceMediaType(mediaType, request);
            }
        }
    }
}
