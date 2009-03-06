package com.amee.engine.restlet;

import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.transaction.TransactionController;
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
        return super.toRequest(httpCall);
    }

    public void commit(HttpResponse response) {
        // commit the response
        super.commit(response);
        // end transaction / entity manager
        transactionController.afterCommit();
        // clear the ThreadBeanHolder at the end of each request
        ThreadBeanHolder.clear();
    }
}