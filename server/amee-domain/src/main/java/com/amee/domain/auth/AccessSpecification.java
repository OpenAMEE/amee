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

import com.amee.domain.IAMEEEntityReference;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates PermissionEntries with an AMEEEntity to specify desired and actual access rights.
 */
public class AccessSpecification implements Serializable {

    private IAMEEEntityReference entityReference;
    private Set<PermissionEntry> actual;
    private Set<PermissionEntry> desired = new HashSet<PermissionEntry>();

    private AccessSpecification() {
        super();
    }

    public AccessSpecification(IAMEEEntityReference entityReference) {
        this();
        this.entityReference = entityReference;
        entityReference.setAccessSpecification(this);
    }

    public AccessSpecification(IAMEEEntityReference entityReference, PermissionEntry... desired) {
        this(entityReference);
        CollectionUtils.addAll(getDesired(), desired);
    }

    public AccessSpecification(IAMEEEntityReference entityReference, Set<PermissionEntry> actual, PermissionEntry... desired) {
        this(entityReference);
        setActual(actual);
        CollectionUtils.addAll(getDesired(), desired);
    }

    public IAMEEEntityReference getEntityReference() {
        return entityReference;
    }

    public Set<PermissionEntry> getActual() {
        return actual;
    }

    public boolean hasActual() {
        return actual != null;
    }

    public void setActual(Set<PermissionEntry> actual) {
        if (actual != null) {
            this.actual = new HashSet<PermissionEntry>(actual);
        } else {
            this.actual = null;
        }
    }

    public Set<PermissionEntry> getDesired() {
        return desired;
    }
}