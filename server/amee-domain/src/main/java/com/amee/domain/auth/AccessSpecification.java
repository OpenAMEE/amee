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
 * Associates permission entries with an entity to specify requested access rights.
 */
public class AccessSpecification implements Serializable {

    private AMEEEntity entity;
    private Set<PermissionEntry> entries = new HashSet<PermissionEntry>();

    private AccessSpecification() {
        super();
    }

    public AccessSpecification(AMEEEntity entity) {
        this();
        this.entity = entity;
    }

    public AccessSpecification(AMEEEntity entity, PermissionEntry... entries) {
        this(entity);
        CollectionUtils.addAll(this.entries, entries);
    }

    public AccessSpecification(AMEEEntity entity, Set<PermissionEntry> entries) {
        this(entity);
        this.entries.addAll(entries);
    }

    public AMEEEntity getEntity() {
        return entity;
    }

    public Set<PermissionEntry> getEntries() {
        return entries;
    }
}