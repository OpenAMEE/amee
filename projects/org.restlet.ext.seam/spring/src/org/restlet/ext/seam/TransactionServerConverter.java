package org.restlet.ext.seam;

import com.jellymold.utils.ThreadBeanHolder;
import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;
import org.apache.log4j.NDC;
import org.restlet.Context;

public class TransactionServerConverter extends HttpServerConverter {

    private TransactionController transactionController;

    public TransactionServerConverter(Context context) {
        super(context);
        transactionController = (TransactionController) context.getAttributes().get("transactionController");
    }

    public HttpRequest toRequest(HttpServerCall httpCall) {
        // clear the ThreadBeanHolder at the start of each request
        ThreadBeanHolder.clear();
        // start transaction / entity manager
        transactionController.beforeToRequest(!"GET".equalsIgnoreCase(httpCall.getMethod()));
        // pass request through
        HttpRequest req = super.toRequest(httpCall);
        // set the NDC (IP address) at the start of each request
        NDC.remove();
        NDC.push(req.getClientInfo().getAddress());
        return req;
    }

    public void commit(HttpResponse response) {
        // commit the response
        super.commit(response);
        // end transaction / entity manager
        transactionController.afterCommit();
        // clear the ThreadBeanHolder at the end of each request
        ThreadBeanHolder.clear();
        // Clear the NDC when the request has be completed.
        NDC.remove();
    }
}