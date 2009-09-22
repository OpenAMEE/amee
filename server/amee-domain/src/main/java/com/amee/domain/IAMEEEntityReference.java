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

import com.amee.domain.auth.AccessSpecification;

/**
 * Defines an interface exposing the properties required to identify (refer-to) an AMEEEntity.
 */
public interface IAMEEEntityReference {

    /**
     * Fetches the ID of the referenced entity.
     *
     * @return the entity id
     */
    public Long getEntityId();

    /**
     * Fetches the UID of the referenced entity.
     *
     * @return entity UID
     */
    public String getEntityUid();

    /**
     * Fetches the ObjectTyoe of the referenced entity.
     *
     * @return the ObjectType
     */
    public ObjectType getObjectType();

    /**
     * Returns the transient AccessSpecification for this entity. This will only be present if
     * an AccessSpecification for this entity has been created in the current thread.
     *
     * @return the AccessSpecification for this entity in the current thread
     */
    public AccessSpecification getAccessSpecification();

    /**
     * Sets the AccessSpecification for this entity.
     *
     * @param accessSpecification for this entity
     */
    public void setAccessSpecification(AccessSpecification accessSpecification);

    public AMEEEntity getEntity();

    public void setEntity(AMEEEntity entity);
}