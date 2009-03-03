package com.amee.domain.auth;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public enum UserType implements Serializable {

    STANDARD, GUEST, ANONYMOUS, SUPER;

    private String[] names = {
            "STANDARD",
            "GUEST",
            "ANONYMOUS",
            "SUPER"};

    private String[] labels = {
            "Standard",
            "Guest",
            "Anonymous",
            "Super"};

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
        for (UserType userType : UserType.values()) {
            choices.put(userType.getName(), userType.getLabel());
        }
        return choices;
    }

    public static JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Map<String, String> choices = UserType.getChoices();
        for (String key : choices.keySet()) {
            obj.put(key, choices.get(key));
        }
        return obj;
    }

    public static Element getElement(Document document) {
        Element container = document.createElement("UserTypes");
        Map<String, String> choices = UserType.getChoices();
        for (String name : choices.keySet()) {
            Element item = document.createElement("UserType");
            item.setAttribute("name", name);
            item.setAttribute("label", choices.get(name));
        }
        return container;
    }
}