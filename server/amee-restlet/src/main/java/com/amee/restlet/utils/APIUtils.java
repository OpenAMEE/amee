package com.amee.restlet.utils;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.hibernate.util.DTDEntityResolver;

import java.io.InputStream;

public class APIUtils {

    public static org.dom4j.Element getRootElement(InputStream stream) throws DocumentException {
        if (stream != null) {
            SAXReader saxReader = new SAXReader();
            saxReader.setEntityResolver(new DTDEntityResolver());
            saxReader.setMergeAdjacentText(true);
            return saxReader.read(stream).getRootElement();
        } else {
            return null;
        }
    }
}
