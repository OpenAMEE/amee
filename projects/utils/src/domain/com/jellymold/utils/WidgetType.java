package com.jellymold.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public enum WidgetType implements Serializable {

    INPUT, TEXTAREA, SELECT, RADIO, CHECK;

    private String[] names = {
            "INPUT",
            "TEXTAREA",
            "SELECT",
            "RADIO",
            "CHECK"};

    private String[] labels = {
            "Input",
            "TextArea",
            "Select",
            "Radio",
            "Check"};

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
        for (WidgetType widgetType : values()) {
            choices.put(widgetType.getName(), widgetType.getLabel());
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
        Element element = document.createElement("WidgetTypes");
        Map<String, String> choices = getChoices();
        for (String name : choices.keySet()) {
            Element widgetTypeElem = document.createElement("WidgetType");
            widgetTypeElem.setAttribute("name", name);
            widgetTypeElem.setAttribute("label", choices.get(name));
        }
        return element;
    }
}