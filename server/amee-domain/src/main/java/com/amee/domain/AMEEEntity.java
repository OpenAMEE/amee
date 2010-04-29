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
import com.amee.domain.auth.AuthorizationContext;
import com.amee.domain.auth.Permission;
import com.amee.persist.BaseEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends BaseEntity to add state (status) and permissions.
 */
@MappedSuperclass
public abstract class AMEEEntity extends BaseEntity implements IAMEEEntityReference {

    /**
     * Represents the state of the entity.
     */
    @Column(name = "STATUS")
    protected AMEEStatus status = AMEEStatus.ACTIVE;

    /**
     * A transient AccessSpecification.
     */
    @Transient
    private AccessSpecification accessSpecification;

    /**
     * Default constructor.
     */
    public AMEEEntity() {
        super();
    }

    /**
     * Two AMEEEntity instances are considered equal if their UID matches, along with standard
     * object identity matching. The IAMEEEntityReference interface is used as the base identity
     * for all AMEEEntity instances.
     *
     * @param o object to compare
     * @return true if the supplied object matches this object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || !IAMEEEntityReference.class.isAssignableFrom(o.getClass())) return false;
        IAMEEEntityReference entity = (IAMEEEntityReference) o;
        return getEntityUid().equals(entity.getEntityUid()) && getObjectType().equals(entity.getObjectType());
    }

    /**
     * Copy values from this instance to the supplied instance.
     *
     * @param o Object to copy values to
     */
    protected void copyTo(AMEEEntity o) {
        super.copyTo(o);
        o.status = status;
    }

    /**
     * Allows specific entities to interact with an AuthorizationContext and express
     * permissions that are implicit in the model.
     * <p/>
     * This default implementation does nothing and returns an empty list.
     *
     * @param authorizationContext to consider
     * @return permissions list
     */
    public List<Permission> handleAuthorizationContext(AuthorizationContext authorizationContext) {
        return new ArrayList<Permission>();
    }

    /**
     * Sets the entity ID. Implements method declared in IAMEEEntityReference.
     *
     * @return the entity ID
     */
    public Long getEntityId() {
        return getId();
    }

    /**
     * Get the entity UID. Implements method declared in IAMEEEntityReference.
     *
     * @return the entity UID
     */
    public String getEntityUid() {
        return getUid();
    }

    /**
     * Fetch the entity status.
     *
     * @return entity status
     */
    public AMEEStatus getStatus() {
        return status;
    }

    /**
     * Fetch the entity status as the ordinal of the AMEEStatus.
     *
     * @return ordinal of AMEEStatus
     */
    public int getStatusCode() {
        return status.ordinal();
    }

    /**
     * Convienience method to determine if the entity state is TRASH.
     *
     * @return true if the entity state is TRASH
     */
    public boolean isTrash() {
        return status.equals(AMEEStatus.TRASH);
    }

    /**
     * Convienience method to determine if the entity state is ACTIVE.
     *
     * @return true if the entity state is ACTIVE
     */
    public boolean isActive() {
        return status.equals(AMEEStatus.ACTIVE);
    }

    /**
     * Convienience method to determine if the entity state is DEPRECATED.
     *
     * @return true if the entity state is DEPRECATED
     */
    public boolean isDeprecated() {
        return status.equals((AMEEStatus.DEPRECATED));
    }

    /**
     * Set the status of the entity.
     *
     * @param status to set
     */
    public void setStatus(AMEEStatus status) {
        this.status = status;
    }

    /**
     * Set the status of the entity. The name is used to find the correct AMEEStatus.
     *
     * @param name represeting status
     */
    public void setStatus(String name) {
        if (name != null) {
            try {
                setStatus(AMEEStatus.valueOf(name));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The supplied status name is invalid.");
            }
        }
    }

    /**
     * Returns the transient AccessSpecification for this entity. This will only be present if
     * an AccessSpecification for this entity has been created in the current thread.
     *
     * @return the AccessSpecification for this entity in the current thread
     */
    public AccessSpecification getAccessSpecification() {
        return accessSpecification;
    }

    /**
     * Sets the AccessSpecification for this entity.
     *
     * @param accessSpecification for this entity
     */
    public void setAccessSpecification(AccessSpecification accessSpecification) {
        this.accessSpecification = accessSpecification;
    }

    public AMEEEntity getEntity() {
        return this;
    }

    public void setEntity(AMEEEntity entity) {
        // do nothing
    }
}