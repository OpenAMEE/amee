package org.restlet.ext.seam;

import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.BeansException;

public class SpringFilter extends Filter implements ApplicationContextAware {

    private TransactionController transactionController;
    private ApplicationContext applicationContext;

    public SpringFilter(Application application, TransactionController transactionController) {
        super(application.getContext(), application);
        this.transactionController = transactionController;
    }

    public SpringFilter(Application application, TransactionController transactionController, ApplicationContext applicationContext) {
        this(application, transactionController);
        this.applicationContext = applicationContext;
    }

    protected int doHandle(Request request, Response response) {
        try {
            request.getAttributes().put("springContext", applicationContext);
            transactionController.beforeHandle();
            return super.doHandle(request, response);
        } finally {
            transactionController.afterHandle(!response.getStatus().isError());
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}