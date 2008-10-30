package com.jellymold.utils.domain;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.hibernate.util.DTDEntityResolver;

import java.io.InputStream;

public class APIUtils {

    public static Element getElement(Document document, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value);
        return element;
    }

    public static Element getIdentityElement(Document document, String name, PersistentObject obj) {
        Element element = document.createElement(name);
        element.setAttribute("uid", obj.getUid());
        return element;
    }

    public static Element getIdentityElement(Document document, PersistentObject obj) {
        return getIdentityElement(document, obj.getClass().getSimpleName(), obj);
    }

    public static JSONObject getIdentityJSONObject(PersistentObject object) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", object.getUid());
        return obj;
    }

    public static org.dom4j.Element getRootElement(InputStream stream) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        saxReader.setEntityResolver(new DTDEntityResolver());
        saxReader.setMergeAdjacentText(true);
        return saxReader.read(stream).getRootElement();
    }
}
