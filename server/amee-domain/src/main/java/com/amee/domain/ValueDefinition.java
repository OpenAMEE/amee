/**
 * This file is part of AMEE.
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

import com.amee.base.utils.XMLUtils;
import com.amee.domain.ValueType;
import com.amee.domain.environment.Environment;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VALUE_DEFINITION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ValueDefinition extends AMEEEnvironmentEntity {

    @Column(name = "NAME")
    private String name = "";

    @Column(name = "DESCRIPTION")
    private String description = "";

    @Column(name = "VALUE_TYPE")
    private ValueType valueType = ValueType.TEXT;

    public ValueDefinition() {
        super();
    }

    public ValueDefinition(Environment environment, String name, ValueType valueType) {
        this();
        setEnvironment(environment);
        setName(name);
        setValueType(valueType);
    }

    public String toString() {
        return "ValueDefinition_" + getUid();
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("valueType", getValueType());
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            obj.put("description", getDescription());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
        }
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return XMLUtils.getIdentityJSONObject(this);
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ValueDefinition");
        element.setAttribute("uid", getUid());
        element.appendChild(XMLUtils.getElement(document, "Name", getName()));
        element.appendChild(XMLUtils.getElement(document, "ValueType", getValueType().toString()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(XMLUtils.getElement(document, "Description", getDescription()));
            element.appendChild(getEnvironment().getIdentityElement(document));
        }
        return element;
    }

    public Element getIdentityElement(Document document) {
        return XMLUtils.getIdentityElement(document, this);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        if (valueType == null) {
            valueType = ValueType.TEXT;
        }
        this.valueType = valueType;
    }

    public ObjectType getObjectType() {
        return ObjectType.VD;
    }
}