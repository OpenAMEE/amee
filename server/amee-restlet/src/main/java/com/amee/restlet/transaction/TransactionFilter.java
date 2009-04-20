package com.amee.restlet.transaction;

import com.amee.service.transaction.TransactionController;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Filter to allow the TransactionController to commit or rollback a transactino depending on
 * the error state of the response or if a Throwable is caught.
 */
public class TransactionFilter extends Filter {

    @Autowired
    private TransactionController transactionController;

    public TransactionFilter(Application application) {
        super(application.getContext());
    }

    protected int doHandle(Request request, Response response) {
        boolean success = true;
        try {
            transactionController.beforeHandle(!request.getMethod().equals(Method.GET));
            return super.doHandle(request, response);
        } catch (Throwable t) {
            success = false;
            throw new RuntimeException(t);
        } finally {
            transactionController.afterHandle(success && !response.getStatus().isError());
        }
    }
}