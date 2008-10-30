package org.restlet.ext.seam;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class SeamFilter extends Filter {

    public SeamFilter() {
        super();
    }

    public SeamFilter(Application application) {
        super(application.getContext());
    }

    public SeamFilter(Context context) {
        super(context);
    }

    public SeamFilter(Context context, Restlet next) {
        super(context, next);
    }

    protected int doHandle(Request request, Response response) {
        int result = CONTINUE;
        try {
            SeamController.getInstance().beforeHandle();
            result = super.doHandle(request, response);
        } finally {
            SeamController.getInstance().afterHandle();
        }
        return result;
    }
}
