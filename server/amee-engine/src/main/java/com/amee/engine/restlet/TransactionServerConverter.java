package com.amee.engine.restlet;

import com.amee.core.ThreadBeanHolder;
import com.amee.service.transaction.TransactionController;
import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;
import org.restlet.Context;

public class TransactionServerConverter extends HttpServerConverter {

    private TransactionController transactionController;

    public TransactionServerConverter(Context context) {
        super(context);
        transactionController = (TransactionController) context.getAttributes().get("transactionController");
    }

    public HttpRequest toRequest(HttpServerCall httpCall) {
        // Clear the ThreadBeanHolder at the start of each request.
        ThreadBeanHolder.clear();
        // Pass request through.
        return super.toRequest(httpCall);
    }

    public void commit(HttpResponse response) {
        // Commit the response.
        super.commit(response);
        // End transaction / entity manager.
        transactionController.afterCommit();
        // Clear the ThreadBeanHolder at the end of each request.
        ThreadBeanHolder.clear();
    }
}