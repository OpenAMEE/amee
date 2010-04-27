package com.amee.domain;

import com.amee.base.utils.XMLUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
@Entity
@Table(name = "API_VERSION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class APIVersion extends AMEEEntity {

    public static final APIVersion ONE = new APIVersion("1.0");
    public static final APIVersion TWO = new APIVersion("2.0");
    public final static int API_VERSION_SIZE = 3;

    @Column(name = "VERSION", length = API_VERSION_SIZE, nullable = true)
    private String version;

    public APIVersion() {
        super();
    }

    public APIVersion(String version) {
        this();
        this.version = version;
    }

    public boolean equals(Object o) {
        return this == o || o instanceof APIVersion && o.toString().equals(version);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return version;
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("apiVersion", version);
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = XMLUtils.getIdentityJSONObject(this);
        obj.put("apiVersion", getVersion());
        return obj;
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        return XMLUtils.getElement(document, "APIVersion", getVersion());
    }

    public Element getIdentityElement(Document document) {
        Element element = XMLUtils.getIdentityElement(document, this);
        element.appendChild(XMLUtils.getElement(document, "APIVersion", getVersion()));
        return element;
    }

    public boolean isVersionOne() {
        return this.equals(ONE);
    }

    public boolean isNotVersionOne() {
        return !this.equals(ONE);
    }

    public ObjectType getObjectType() {
        return ObjectType.AV;
    }
}