package org.restlet.ext.seam;

import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;
import org.restlet.Context;

public class SeamServerConverter extends HttpServerConverter {

    public SeamServerConverter(Context context) {
        super(context);
    }

    public HttpRequest toRequest(HttpServerCall httpCall) {
        SeamController.getInstance().beforeToRequest(!"GET".equalsIgnoreCase(httpCall.getMethod()));
        return super.toRequest(httpCall);
    }

    public void commit(HttpResponse response) {
        super.commit(response);
        SeamController.getInstance().afterCommit();
    }
}