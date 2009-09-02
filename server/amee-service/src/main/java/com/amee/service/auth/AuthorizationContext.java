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
package com.amee.service.auth;

import com.amee.domain.AMEEEntity;
import com.amee.domain.auth.PermissionEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthorizationContext implements Serializable {

    private List<AMEEEntity> principles = new ArrayList<AMEEEntity>();
    private List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
    private Set<PermissionEntry> entries = new HashSet<PermissionEntry>();
    private boolean matchAll = false;

    public AuthorizationContext() {
        super();
    }

    public AuthorizationContext(AMEEEntity principle, AMEEEntity entity, String entries) {
        this();
        addPrinciple(principle);
        addEntity(entity);
        addEntries(entries);
    }

    public AuthorizationContext(AMEEEntity principle, AMEEEntity entity, Set<PermissionEntry> entries) {
        this();
        addPrinciple(principle);
        addEntity(entity);
        addEntries(entries);
    }

    public AuthorizationContext(List<AMEEEntity> principles, List<AMEEEntity> entities, Set<PermissionEntry> entries) {
        this();
        addPrinciples(principles);
        addEntities(entities);
        addEntries(entries);
    }

    public AuthorizationContext(List<AMEEEntity> principles, List<AMEEEntity> entities, Set<PermissionEntry> entries, boolean matchAll) {
        this();
        addPrinciples(principles);
        addEntities(entities);
        addEntries(entries);
        setMatchAll(matchAll);
    }

    public void addPrinciple(AMEEEntity principle) {
        if (principle == null) throw new IllegalArgumentException("The principle argument must not be null.");
        getPrinciples().add(principle);
    }

    public void addPrinciples(List<AMEEEntity> principles) {
        if (principles == null) throw new IllegalArgumentException("The principles argument must not be null.");
        getPrinciples().addAll(principles);
    }

    public void addEntity(AMEEEntity entity) {
        if (entity == null) throw new IllegalArgumentException("The entity argument must not be null.");
        getEntities().add(entity);
    }

    public void addEntities(List<AMEEEntity> entities) {
        if (entities == null) throw new IllegalArgumentException("The entities argument must not be null.");
        getEntities().addAll(entities);
    }

    public void addEntries(String entries) {
        if (entries == null) throw new IllegalArgumentException("The entries argument must not be null.");
        for (String entry : entries.split(",")) {
            getEntries().add(new PermissionEntry(entry));
        }
    }

    public void addEntries(Set<PermissionEntry> entries) {
        if (entries == null) throw new IllegalArgumentException("The entries argument must not be null.");
        getEntries().addAll(entries);
    }

    public List<AMEEEntity> getPrinciples() {
        return principles;
    }

    public List<AMEEEntity> getEntities() {
        return entities;
    }

    public Set<PermissionEntry> getEntries() {
        return entries;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }
}
