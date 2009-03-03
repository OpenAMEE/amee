package com.amee.domain.auth;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public enum UserStatus implements Serializable {

    ACTIVE, BLOCK, TRASH;

    private String[] names = {
            "ACTIVE",
            "BLOCK",
            "TRASH"};

    private String[] labels = {
            "Active",
            "Block",
            "Trash"};

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
        for (UserStatus userStatus : UserStatus.values()) {
            choices.put(userStatus.getName(), userStatus.getLabel());
        }
        return choices;
    }

    public static JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Map<String, String> choices = UserStatus.getChoices();
        for (String key : choices.keySet()) {
            obj.put(key, choices.get(key));
        }
        return obj;
    }

    public static Element getElement(Document document) {
        Element element = document.createElement("UserStates");
        Map<String, String> choices = UserStatus.getChoices();
        for (String name : choices.keySet()) {
            Element userStatusElem = document.createElement("UserStatus");
            userStatusElem.setAttribute("name", name);
            userStatusElem.setAttribute("label", choices.get(name));
        }
        return element;
    }
}