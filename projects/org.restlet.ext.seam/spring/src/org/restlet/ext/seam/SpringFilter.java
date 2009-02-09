package org.restlet.ext.seam;

import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringFilter extends Filter {

    @Autowired
    private TransactionController transactionController;

    public SpringFilter(Application application) {
        super(application.getContext());
    }

    protected int doHandle(Request request, Response response) {
        try {
            transactionController.beforeHandle();
            return super.doHandle(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            transactionController.afterHandle(!response.getStatus().isError());
        }
    }
}