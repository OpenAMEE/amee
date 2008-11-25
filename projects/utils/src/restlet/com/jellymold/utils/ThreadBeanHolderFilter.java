package com.jellymold.utils;

import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class ThreadBeanHolderFilter extends Filter {
    protected int doHandle(Request request, Response response) {
        try {
            ThreadBeanHolder.clear();
            return super.doHandle(request, response);
        } finally {
            // ThreadBeanHolder.clear();
        }
    }
}