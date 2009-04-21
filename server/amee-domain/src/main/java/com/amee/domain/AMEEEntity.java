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

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.amee.core.PersistentObject;

@MappedSuperclass
public abstract class AMEEEntity implements DatedObject, Serializable {

    public final static int UID_SIZE = 12;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @NaturalId
    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED", nullable = false)
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED", nullable = false)
    private Date modified = null;

    public AMEEEntity() {
        super();
        setUid(UidGen.getUid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!AMEEEntity.class.isAssignableFrom(o.getClass())) return false;
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid == null) {
            uid = "";
        }
        this.uid = uid;
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
}
