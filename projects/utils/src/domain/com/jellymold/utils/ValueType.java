package com.jellymold.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public enum ValueType implements Serializable {

    UNSPECIFIED, TEXT, DATE, BOOLEAN, INTEGER, DECIMAL;

    private String[] names = {
            "UNSPECIFIED",
            "TEXT",
            "DATE",
            "BOOLEAN",
            "INTEGER",
            "DECIMAL"};

    private String[] labels = {
            "Unspecified",
            "Text",
            "Date",
            "Boolean",
            "Integer",
            "Decimal"};

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
        for (ValueType valueType : ValueType.values()) {
            choices.put(valueType.getName(), valueType.getLabel());
        }
        return choices;
    }

    public static JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Map<String, String> choices = ValueType.getChoices();
        for (String key : choices.keySet()) {
            obj.put(key, choices.get(key));
        }
        return obj;
    }

    public static Element getElement(Document document) {
        Element element = document.createElement("ValueTypes");
        Map<String, String> choices = ValueType.getChoices();
        for (String name : choices.keySet()) {
            Element valueTypeElem = document.createElement("ValueType");
            valueTypeElem.setAttribute("name", name);
            valueTypeElem.setAttribute("label", choices.get(name));
        }
        return element;
    }

    public static ValueType getValueType(Object object) {
        if (object instanceof String) {
            return ValueType.TEXT;
        } else if (object instanceof BigDecimal) {
            return ValueType.DECIMAL;
        } else if ((object instanceof Integer)) {
            return ValueType.INTEGER;
        } else if (object instanceof Boolean) {
            return ValueType.BOOLEAN;
        } else if (object instanceof Date) {
            return ValueType.DATE;
        } else {
            return ValueType.UNSPECIFIED;
        }
    }
}