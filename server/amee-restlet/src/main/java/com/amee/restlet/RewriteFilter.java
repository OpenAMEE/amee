package com.amee.restlet;

import com.amee.restlet.utils.MediaTypeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.util.List;

public abstract class RewriteFilter extends Filter {

    protected final Log log = LogFactory.getLog(getClass());

    protected boolean handleAccept = false;

    public RewriteFilter(Application application) {
        super(application.getContext());
    }

    protected int beforeHandle(Request request, Response response) {
        log.debug("beforeHandle()");
        if (handleAccept) {
            setAccept(request);
        }
        return rewrite(request, response);
    }

    protected void afterHandle(Request request, Response response) {
        log.debug("afterHandle()");
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
            MediaType mediaType = MediaType.valueOf(accept);
            if (mediaType != null) {
                MediaTypeUtils.forceMediaType(mediaType, request);
            }
        }
    }

    protected boolean skipRewrite(List<String> segments) {
        return (segments.size() > 1) && matchesReservedPrefixes(segments.get(1));
    }

    protected abstract int rewrite(Request request, Response response);

    protected abstract boolean matchesReservedPrefixes(String segment);

    protected abstract String handleSuffix(List<String> segments);
}
