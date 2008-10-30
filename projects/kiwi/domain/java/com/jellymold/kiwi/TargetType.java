package com.jellymold.kiwi;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public enum TargetType implements Serializable {

    SEAM_COMPONENT_RESOURCE, DIRECTORY_RESOURCE, RESTLET_CLASS;

    private String[] names = {
            "SEAM_COMPONENT_RESOURCE",
            "DIRECTORY_RESOURCE",
            "RESTLET_CLASS"};

    // TODO: i18n - clues here: http://www.jroller.com/page/RickHigh?entry=jdk_1_5_enums_jsf
    private String[] labels = {
            "SEAM_COMPONENT_RESOURCE",
            "DIRECTORY_RESOURCE",
            "RESTLET_CLASS"};

    public String toString() {
        return getName();
    }

    public String getName() {
        return names[this.ordinal()];
    }

    public String getLabel() {
        return labels[this.ordinal()];
    }

    public static Map<String, String> getChoices() {
        Map<String, String> choices = new LinkedHashMap<String, String>();
        for (TargetType userStatus : values()) {
            choices.put(userStatus.getName(), userStatus.getLabel());
        }
        return choices;
    }

    public static JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Map<String, String> choices = getChoices();
        for (String key : choices.keySet()) {
            obj.put(key, choices.get(key));
        }
        return obj;
    }

    public static Element getElement(Document document) {
        Element element = document.createElement("TargetTypes");
        Map<String, String> choices = getChoices();
        for (String name : choices.keySet()) {
            Element userStatusElem = document.createElement("TargetType");
            userStatusElem.setAttribute("name", name);
            userStatusElem.setAttribute("label", choices.get(name));
        }
        return element;
    }
}