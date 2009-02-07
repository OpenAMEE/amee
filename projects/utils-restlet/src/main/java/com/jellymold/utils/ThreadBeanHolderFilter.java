package com.jellymold.utils;

import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.apache.log4j.NDC;

public class ThreadBeanHolderFilter extends Filter {
    protected int doHandle(Request request, Response response) {

        try {
            //Clear the ThreadBeanHolder at the start of each request
            ThreadBeanHolder.clear();

            //Set the NDC (IP address) at the start of each request
            NDC.push(request.getClientInfo().getAddress());

            return super.doHandle(request, response);

        } finally {
            // Clear the NDC when the request has be completed
            NDC.remove();
        }
    }
}