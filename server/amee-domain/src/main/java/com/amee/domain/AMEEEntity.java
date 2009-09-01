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
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@MappedSuperclass
public abstract class AMEEEntity implements IAMEEEntityReference, DatedObject, Serializable {

    public final static int UID_SIZE = 12;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @NaturalId
    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    @Column(name = "STATUS")
    protected AMEEStatus status = AMEEStatus.ACTIVE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED", nullable = false)
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED", nullable = false)
    private Date modified = null;

    /**
     * A List of transient Permissions. These Permissions can be bound into the model by resources and
     * services to be used by the authorization decision logic in PermissionService.
     */
    @Transient
    private List<Permission> permissions;

    public AMEEEntity() {
        super();
        setUid(UidGen.getUid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || !AMEEEntity.class.isAssignableFrom(o.getClass())) return false;
        AMEEEntity ameeEntity = (AMEEEntity) o;
        return getUid().equals(ameeEntity.getUid());
    }

    @Override
    public int hashCode() {
        return getUid().hashCode();
    }

    @PrePersist
    public void onCreate() {
        Date now = Calendar.getInstance().getTime();
        setCreated(now);
        setModified(now);
    }

    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
    }

    public Long getId() {
        return id;
    }

    public Long getEntityId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public String getEntityUid() {
        return getUid();
    }

    public void setUid(String uid) {
        if (uid == null) {
            uid = "";
        }
        this.uid = uid;
    }

    public AMEEStatus getStatus() {
        return status;
    }

    public int getStatusCode() {
        return status.ordinal();
    }

    public boolean isTrash() {
        return status.equals(AMEEStatus.TRASH);
    }

    public boolean isActive() {
        return status.equals(AMEEStatus.ACTIVE);
    }

    public boolean isDeprecated() {
        return status.equals((AMEEStatus.DEPRECATED));
    }

    public void setStatus(AMEEStatus status) {
        this.status = status;
    }

    public void setStatus(String name) {
        if (name != null) {
            try {
                setStatus(AMEEStatus.valueOf(name));
            } catch (IllegalArgumentException e) {
                // swallow
            }
        }
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public List<Permission> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<Permission>();
            addBuiltInPermissions(permissions);
        }
        return permissions;
    }

    /**
     * Add 'built-in' Permissions to this Entity. Allows specific entities to express permissions that
     * are implicit in the model.
     *
     * @param permissions the Permissions List to modify
     */
    protected void addBuiltInPermissions(List<Permission> permissions) {
        // do nothing here
    }

    public List<Permission> getPermissionsForPrinciple(IAMEEEntityReference principle) {
        List<Permission> permissions = new ArrayList<Permission>();
        for (Permission permission : getPermissions()) {
            if (permission.getPrincipleReference().equals(principle)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public List<Permission> getPermissionsForEntity(IAMEEEntityReference entity) {
        List<Permission> permissions = new ArrayList<Permission>();
        for (Permission permission : getPermissions()) {
            if (permission.getEntityReference().equals(entity)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * Fetch all Permissions that match the supplied principle and entity.
     *
     * @param principle to match on
     * @param entity    to match on
     * @return list of matching permissions
     */
    public List<Permission> getPermissionsForPrincipleAndEntity(IAMEEEntityReference principle, IAMEEEntityReference entity) {
        List<Permission> permissions = new ArrayList<Permission>();
        for (Permission permission : getPermissions()) {
            if (permission.getPrincipleReference().equals(principle) && permission.getEntityReference().equals(entity)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }
}