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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class AMEEEntityReference implements IAMEEEntityReference, Serializable {

    public final static int ENTITY_TYPE_MAX_SIZE = 50;

    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @Column(name = "ENTITY_UID", length = AMEEEntity.UID_SIZE, nullable = false)
    private String entityUid = "";

    @Column(name = "ENTITY_TYPE", length = ENTITY_TYPE_MAX_SIZE, nullable = false)
    private String entityType = "";

    public AMEEEntityReference() {
        super();
    }

    public AMEEEntityReference(IAMEEEntityReference entity) {
        this();
        setEntityId(entity.getEntityId());
        setEntityUid(entity.getEntityUid());
        setEntityType(entity.getObjectType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || !IAMEEEntityReference.class.isAssignableFrom(o.getClass())) return false;
        IAMEEEntityReference entity = (IAMEEEntityReference) o;
        return getEntityId().equals(entity.getEntityId()) && getEntityType().equals(entity.getObjectType());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == getEntityId() ? 0 : getEntityId().hashCode());
        hash = 31 * hash + (null == getEntityType() ? 0 : getEntityType().hashCode());
        return hash;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getEntityUid());
        obj.put("type", getEntityType().getName());
        return obj;
    }

    public Element getElement(Document document, String name) {
        Element element = document.createElement(name);
        element.setAttribute("uid", getEntityUid());
        element.setAttribute("type", getEntityType().getName());
        return element;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityUid() {
        return entityUid;
    }

    public void setEntityUid(String entityUid) {
        if (entityUid == null) {
            entityUid = "";
        }
        this.entityUid = entityUid;
    }

    public ObjectType getObjectType() {
        return getEntityType();
    }

    public ObjectType getEntityType() {
        return ObjectType.valueOf(entityType);
    }

    public void setEntityType(ObjectType objectType) {
        this.entityType = objectType.getName();
    }
}
