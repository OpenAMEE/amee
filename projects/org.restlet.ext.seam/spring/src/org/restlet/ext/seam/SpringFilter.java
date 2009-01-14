package org.restlet.ext.seam;

import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.ApplicationContext;

public class SpringFilter extends Filter {

    private SpringController springController;
    private ApplicationContext springContext;

    public SpringFilter(Application application, SpringController springController, ApplicationContext springContext) {
        super(application.getContext(), application);
        this.springController = springController;
        this.springContext = springContext;
    }

    protected int doHandle(Request request, Response response) {
        try {
            request.getAttributes().put("springContext", springContext);
            springController.beforeHandle();
            super.doHandle(request, response);
        } finally {
            springController.afterHandle(!response.getStatus().isError());
        }
        return CONTINUE;
    }
}
