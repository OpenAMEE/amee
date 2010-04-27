package com.amee.domain;

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

    // The order of these values must not be changed!
    // Hibernate has mapped them to ordinal values.
    // Any new values must be appended to the list. 
    UNSPECIFIED("UNSPECIFIED", "Unspecified"),
    TEXT("TEXT", "Text"),
    DATE("DATE", "Date"),
    BOOLEAN("BOOLEAN", "Boolean"),
    INTEGER("INTEGER", "Integer"),
    DECIMAL("DECIMAL", "Decimal");

    ValueType(String name, String label) {
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
        for (ValueType valueType : ValueType.values()) {
            choices.put(valueType.name, valueType.label);
        }
        return choices;
    }

    public static JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Map<String, String> choices = ValueType.getChoices();
        for (Map.Entry<String, String> e : choices.entrySet()) {
            obj.put(e.getKey(), e.getValue());
        }
        return obj;
    }

    public static Element getElement(Document document) {
        Element element = document.createElement("ValueTypes");
        Map<String, String> choices = ValueType.getChoices();
        for (Map.Entry<String, String> e : choices.entrySet()) {
            Element valueTypeElem = document.createElement("ValueType");
            valueTypeElem.setAttribute("name", e.getKey());
            valueTypeElem.setAttribute("label", e.getValue());
            element.appendChild(valueTypeElem);
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