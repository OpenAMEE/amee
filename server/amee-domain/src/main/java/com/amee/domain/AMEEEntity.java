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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract base class for all persistent entities in AMEE, providing common base properties and
 * methods. The properties cover identity (id, uid), state (status), auditing (created, modified)
 * and permissions.
 */
@Entity
@MappedSuperclass
public abstract class AMEEEntity implements IAMEEEntityReference, DatedObject, Serializable {

    @Transient
    protected final Log log = LogFactory.getLog(getClass());

    // The exact size of all UID fields.
    public final static int UID_SIZE = 12;

    // A thread bound Set of UIDs seen by the onCreate method. Used to detect UID duplicates.
    private final static ThreadLocal<Set<String>> UIDS = new ThreadLocal<Set<String>>() {
        protected Set<String> initialValue() {
            return new HashSet<String>();
        }
    };

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
     * A transient AccessSpecification.
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
        return getEntityUid().equals(entity.getEntityUid()) && getObjectType().equals(entity.getObjectType());
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
     * Called by the JPA persistence provider when a persistent entity is created. Updates created and
     * modified with the current time.
     */
    @PrePersist
    public void onCreate() {
        // Keep track of UIDs used in the current Thread.
        if (!addUid()) {
            // UID duplicate detected.
            throw new RuntimeException("Duplicate UID '" + getUid() + "' detected in class '" + getClass().getSimpleName() + "'.");
        }
        // Update created and modified.
        Date now = Calendar.getInstance().getTime();
        setCreated(now);
        setModified(now);
    }

    /**
     * Adds the current uid value to the thread bound Set of UIDs.
     *
     * @return true of the UID is not already stored, otherwise false
     */
    protected boolean addUid() {
        return UIDS.get().add(getUid());
    }

    /**
     * Called by the JPA persistence provider when a persistent entity is updated. Updates modified with
     * the current time.
     */
    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
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