package com.amee.engine.restlet;

import com.amee.core.ThreadBeanHolder;
import com.amee.restlet.RequestContext;
import com.amee.service.invalidation.InvalidationService;
import com.amee.service.transaction.TransactionController;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Filter to allow the TransactionController to commit or rollback a transaction depending on
 * the error state of the response or if a Throwable is caught.
 */
public class TransactionFilter extends Filter {

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private InvalidationService invalidationService;

    public TransactionFilter(Application application) {
        super(application.getContext());
    }

    protected int doHandle(Request request, Response response) {
        boolean success = true;
        try {
            // Setup a RequestContext bean bound to this request thread.
            ThreadBeanHolder.set("ctx", new RequestContext());
            invalidationService.beforeHandle();
            transactionController.beforeHandle(!request.getMethod().equals(Method.GET));
            return super.doHandle(request, response);
        } catch (Throwable t) {
            success = false;
            RequestContext ctx = (RequestContext) ThreadBeanHolder.get("ctx");
            ctx.setError(t.getMessage());
            ctx.error();
            throw new RuntimeException(t);
        } finally {
            // Close off the RequestContext.
            RequestContext ctx = (RequestContext) ThreadBeanHolder.get("ctx");
            ctx.setStatus(response.getStatus());
            ctx.record();
            // Handle the end of transaction.
            transactionController.afterHandle(success && !response.getStatus().isError());
            // Send any invalidation messages.
            invalidationService.afterHandle();
        }
    }
}