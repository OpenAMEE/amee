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

import com.amee.domain.auth.Permission;
import com.amee.domain.auth.AccessSpecification;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * An abstract base class for all persistent entities in AMEE, providing common base properties and
 * methods. The properties cover identity (id, uid), state (status), auditing (created, modified)
 * and permissions.
 */
@Entity
@MappedSuperclass
public abstract class AMEEEntity implements IAMEEEntityReference, DatedObject, Serializable {

    public final static int UID_SIZE = 12;

    /**
     * The unique ID, within the table, of the entity.
     */
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    /**
     * The unique UID of the entity.
     */
    @NaturalId
    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    /**
     * Represents the state of the entity.
     */
    @Column(name = "STATUS")
    protected AMEEStatus status = AMEEStatus.ACTIVE;

    /**
     * Timestamp of when the entity was created. Set by onCreate().
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED", nullable = false)
    private Date created = null;

    /**
     * Timestamp of when the entity was modified. Set by onModify().
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED", nullable = false)
    private Date modified = null;

    /**
     * A List of transient Permissions. These Permissions can be bound into the model by resources and
     * services to be used by the authorization decision logic in PermissionService.
     */
    @Transient
    private List<Permission> permissions;

    /**
     * A transient AccessSpecification 
     */
    @Transient
    private AccessSpecification accessSpecification;

    /**
     * Default constructor. Set the uid property with a newly generated value.
     */
    public AMEEEntity() {
        super();
        setUid(UidGen.getUid());
    }

    /**
     * Two AMEEEntity instances are considered equal if their UID matches, along with standard
     * object identity matching.
     *
     * @param o object to compare
     * @return true if the supplied object matches this object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || !IAMEEEntityReference.class.isAssignableFrom(o.getClass())) return false;
        IAMEEEntityReference entity = (IAMEEEntityReference) o;
        return getEntityId().equals(entity.getEntityId()) && getObjectType().equals(entity.getObjectType());
    }

    /**
     * Returns a hash code for an AMEEEntity. Internally uses the hash code of the uid property.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return getUid().hashCode();
    }

    /**
     * Called by the JPA persistence provider when a persistent entity is created.
     */
    @PrePersist
    public void onCreate() {
        Date now = Calendar.getInstance().getTime();
        setCreated(now);
        setModified(now);
    }

    /**
     * Called by the JPA persistence provider when a persistent entity is updated.
     */
    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
    }

    /**
     * Fetch the entity ID.
     *
     * @return the entity ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the entity ID. Implements method declared in IAMEEEntityReference.
     *
     * @return the entity ID
     */
    public Long getEntityId() {
        return id;
    }

    /**
     * Sets the entity ID.
     *
     * @param id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the entity UID.
     *
     * @return the entity UID
     */
    public String getUid() {
        return uid;
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
     * Set the entity UID.
     *
     * @param uid to set
     */
    public void setUid(String uid) {
        if (uid == null) {
            uid = "";
        }
        this.uid = uid;
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
     * Fetch the created timestamp.
     *
     * @return the created timestamp
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Set the created timestamp.
     *
     * @param created timestamp to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Fetch the modified timestamp.
     *
     * @return modified timestamp to set
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Set the modified timestamp.
     *
     * @param modified timestamp to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Fetches the list of Permissions associated with an AMEEEntity instance. If the permissions
     * property has not yet been set then a new list is assigned and then populated with
     * Permissions from addBuiltInPermissions (if any).
     *
     * @return the list of Permissions
     */
    public List<Permission> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<Permission>();
            addBuiltInPermissions(permissions);
        }
        return permissions;
    }

    /**
     * Add 'built-in' Permissions to this Entity. Allows specific entities to express
     * permissions that are implicit in the model.
     *
     * @param permissions the Permissions List to modify
     */
    protected void addBuiltInPermissions(List<Permission> permissions) {
        // do nothing here
    }

    /**
     * Returns a list of Permissions associated with the supplied principal.
     *
     * @param principal to match against
     * @return list of matching Permissions
     */
    // TODO: Doesn't seem to be required?
    private List<Permission> getPermissionsForPrincipal(IAMEEEntityReference principal) {
        List<Permission> permissions = new ArrayList<Permission>();
        for (Permission permission : getPermissions()) {
            if (permission.getPrincipalReference().equals(principal)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * Returns a list of Permissions associated with the supplied entity.
     *
     * @param entity to match against
     * @return list of matching Permissions
     */
    // TODO: Doesn't seem to be required?
    private List<Permission> getPermissionsForEntity(IAMEEEntityReference entity) {
        List<Permission> permissions = new ArrayList<Permission>();
        for (Permission permission : getPermissions()) {
            if (permission.getEntityReference().equals(entity)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * Fetch all Permissions that match the supplied principal and entity.
     *
     * @param principal to match on
     * @param entity    to match on
     * @return list of matching permissions
     */
    public List<Permission> getPermissionsForPrincipalAndEntity(IAMEEEntityReference principal, IAMEEEntityReference entity) {
        List<Permission> permissions = new ArrayList<Permission>();
        for (Permission permission : getPermissions()) {
            if (permission.getPrincipalReference().equals(principal) && permission.getEntityReference().equals(entity)) {
                permissions.add(permission);
            }
        }
        return permissions;
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
}