package org.restlet.ext.seam;

import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionFilter extends Filter {

    @Autowired
    private TransactionController transactionController;

    public TransactionFilter(Application application) {
        super(application.getContext());
    }

    protected int doHandle(Request request, Response response) {
        try {
            return super.doHandle(request, response);
        } finally {
            transactionController.afterHandle(!response.getStatus().isError());
        }
    }
}