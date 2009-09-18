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
package com.amee.domain.auth;

import com.amee.domain.AMEEEntity;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates PermissionEntries with an AMEEEntity to specify desired and actual access rights.
 */
public class AccessSpecification implements Serializable {

    private AMEEEntity entity;
    private Set<PermissionEntry> desired = new HashSet<PermissionEntry>();
    private Set<PermissionEntry> actual = new HashSet<PermissionEntry>();

    private AccessSpecification() {
        super();
    }

    public AccessSpecification(AMEEEntity entity) {
        this();
        this.entity = entity;
        entity.setAccessSpecification(this);
    }

    public AccessSpecification(AMEEEntity entity, PermissionEntry... desired) {
        this(entity);
        CollectionUtils.addAll(this.desired, desired);
    }

    public AccessSpecification(AMEEEntity entity, Set<PermissionEntry> desired) {
        this(entity);
        this.desired.addAll(desired);
    }

    public AMEEEntity getEntity() {
        return entity;
    }

    public Set<PermissionEntry> getDesired() {
        return desired;
    }

    public Set<PermissionEntry> getActual() {
        return actual;
    }

    public void setActual(Set<PermissionEntry> actual) {
        this.actual.clear();
        this.actual.addAll(actual);
    }
}