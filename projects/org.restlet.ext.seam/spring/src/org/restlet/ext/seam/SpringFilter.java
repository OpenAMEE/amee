package org.restlet.ext.seam;

import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.context.ApplicationContext;

public class SpringFilter extends Filter {

    private TransactionController transactionController;
    private ApplicationContext springContext;

    public SpringFilter(Application application, TransactionController transactionController, ApplicationContext springContext) {
        super(application.getContext(), application);
        this.transactionController = transactionController;
        this.springContext = springContext;
    }

    protected int doHandle(Request request, Response response) {
        try {
            request.getAttributes().put("springContext", springContext);
            transactionController.beforeHandle();
            return super.doHandle(request, response);
        } finally {
            transactionController.afterHandle(!response.getStatus().isError());
        }
    }
}