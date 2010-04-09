package com.amee.domain.auth;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public enum UserType implements Serializable {

    // The order of these values must not be changed!
    // Hibernate has mapped them to ordinal values.
    // Any new values must be appended to the list.
    STANDARD("STANDARD", "Standard"),
    GUEST("GUEST", "Guest"),
    ANONYMOUS("ANONYMOUS", "Anonymous"),
    SUPER("SUPER", "Super");

    UserType(String name, String label) {
        this.name = name;
        this.label = label;
    }

    private final String name;
    private final String label;

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public static Map<String, String> getChoices() {
        Map<String, String> choices = new LinkedHashMap<String, String>();
        for (UserType userType : UserType.values()) {
            choices.put(userType.name, userType.label);
        }
        return choices;
    }

    public static JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Map<String, String> choices = UserType.getChoices();
        for (Map.Entry<String, String> e : choices.entrySet()) {
            obj.put(e.getKey(), e.getValue());
        }
        return obj;
    }

    public static Element getElement(Document document) {
        Element container = document.createElement("UserTypes");
        Map<String, String> choices = UserType.getChoices();
        for (Map.Entry<String, String> e : choices.entrySet()) {
            Element item = document.createElement("UserType");
            item.setAttribute("name", e.getKey());
            item.setAttribute("label", e.getValue());
            container.appendChild(item);
        }
        return container;
    }
}