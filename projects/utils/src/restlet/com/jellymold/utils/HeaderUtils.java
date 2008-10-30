package com.jellymold.utils;

import com.noelios.restlet.http.HttpConstants;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Series;

import java.io.Serializable;

public class HeaderUtils implements Serializable {

    public static String getHeaderFirstValue(String name, Request request) {
        Series headers = (Series) request.getAttributes().get(HttpConstants.ATTRIBUTE_HEADERS);
        return headers.getFirstValue(name, true);
    }

    public static void addHeader(String name, String value, Response response) {
        HeaderUtils.addHeader(new Parameter(name, value), response);
    }

    public static void addHeader(Parameter header, Response response) {
        Series<Parameter> additionalHeaders =
                (Series<Parameter>) response.getAttributes().get(HttpConstants.ATTRIBUTE_HEADERS);
        if (additionalHeaders == null) {
            additionalHeaders = new Form();
            response.getAttributes().put(HttpConstants.ATTRIBUTE_HEADERS, additionalHeaders);
        }
        Parameter oldHeader = additionalHeaders.getFirst(header.getName());
        if (oldHeader != null) {
            additionalHeaders.remove(oldHeader);
        }
        additionalHeaders.add(header);
    }
}
