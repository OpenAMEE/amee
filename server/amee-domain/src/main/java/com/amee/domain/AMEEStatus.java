/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.domain;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public enum AMEEStatus implements Serializable {

    // The order of these values must not be changed!
    // Hibernate has mapped them to ordinal values.
    // Any new values must be appended to the list.
    TRASH("TRASH", "Trash"),
    ACTIVE("ACTIVE", "Active"),
    DEPRECATED("DEPRECATED", "Deprecated");

    AMEEStatus(String name, String label) {
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
        for (AMEEStatus status : AMEEStatus.values()) {
            choices.put(status.name, status.label);
        }
        return choices;
    }

    public static JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Map<String, String> choices = AMEEStatus.getChoices();
        for (Map.Entry<String, String> e : choices.entrySet()) {
            obj.put(e.getKey(), e.getValue());
        }
        return obj;
    }

    public static Element getElement(Document document) {
        Element statesElem = document.createElement("States");
        Map<String, String> choices = AMEEStatus.getChoices();
        for (Map.Entry<String, String> e : choices.entrySet()) {
            Element statusElem = document.createElement("Status");
            statusElem.setAttribute("name", e.getKey());
            statusElem.setAttribute("label", e.getValue());
            statesElem.appendChild(statusElem);
        }
        return statesElem;
    }
}