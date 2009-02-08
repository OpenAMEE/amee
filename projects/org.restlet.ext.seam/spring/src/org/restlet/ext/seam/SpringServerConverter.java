package org.restlet.ext.seam;

import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;
import org.restlet.Context;

public class SpringServerConverter extends HttpServerConverter {

    private TransactionController transactionController;

    public SpringServerConverter(Context context) {
        super(context);
        transactionController = (TransactionController) context.getAttributes().get("transactionController");
    }

    public HttpRequest toRequest(HttpServerCall httpCall) {
        transactionController.beforeToRequest(!"GET".equalsIgnoreCase(httpCall.getMethod()));
        return super.toRequest(httpCall);
    }

    public void commit(HttpResponse response) {
        super.commit(response);
        transactionController.afterCommit();
    }
}