package org.restlet.ext.seam;

import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;
import org.restlet.Context;

public class SpringServerConverter extends HttpServerConverter {

    private SpringController springController;

    public SpringServerConverter(Context context) {
        super(context);
        springController = (SpringController) context.getAttributes().get("springController");
    }

    public HttpRequest toRequest(HttpServerCall httpCall) {
        springController.beforeToRequest(!"GET".equalsIgnoreCase(httpCall.getMethod()));
        return super.toRequest(httpCall);
    }

    public void commit(HttpResponse response) {
        super.commit(response);
        springController.afterCommit();
    }
}