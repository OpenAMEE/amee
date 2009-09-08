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

/**
 * AuthorizationContext encapsulates the 'context' for an authorization request. The context
 * contains three main collections; a list of 'principles', a list of 'entities' and a set of
 * PermissionEntries. These collections are taken into consideration when deciding if a
 * request should be authorized.
 */
public class AuthorizationContext implements Serializable {

    /**
     * A list of principles which may or may not be authorized for the entities.
     */
    private List<AMEEEntity> principles = new ArrayList<AMEEEntity>();

    /**
     * A list of entities over which the principles may or may not have permissions.
     */
    private List<AMEEEntity> entities = new ArrayList<AMEEEntity>();

    /**
     * The specification of the permissions that are desired for the authorization request.
     */
    private Set<PermissionEntry> entries = new HashSet<PermissionEntry>();

    /**
     * Flag to indicate if all the PermissionEntries in entries should be matched or
     * if just one should match. The default is false, to match any single PermissionEntry.
     */
    private boolean matchAll = false;

    /**
     * Default constructor.
     */
    public AuthorizationContext() {
        super();
    }

    /**
     * Construct an AuthorizationContext with the supplied principle, entity and entries.
     *
     * @param principle
     * @param entity
     * @param entries
     */
    public AuthorizationContext(AMEEEntity principle, AMEEEntity entity, String entries) {
        this();
        addPrinciple(principle);
        addEntity(entity);
        addEntries(entries);
    }

    /**
     * Construct an AuthorizationContext with the supplied principle, entity and entries.
     *
     * @param principle
     * @param entity
     * @param entries
     */
    public AuthorizationContext(AMEEEntity principle, AMEEEntity entity, Set<PermissionEntry> entries) {
        this();
        addPrinciple(principle);
        addEntity(entity);
        addEntries(entries);
    }

    /**
     * Construct an AuthorizationContext with the supplied principles, entities and entries.
     *
     * @param principles
     * @param entities
     * @param entries
     */
    public AuthorizationContext(List<AMEEEntity> principles, List<AMEEEntity> entities, Set<PermissionEntry> entries) {
        this();
        addPrinciples(principles);
        addEntities(entities);
        addEntries(entries);
    }

    /**
     * Construct an AuthorizationContext with the supplied principles, entities and entries.
     *
     * @param principles
     * @param entities
     * @param entries
     */
    public AuthorizationContext(List<AMEEEntity> principles, List<AMEEEntity> entities, Set<PermissionEntry> entries, boolean matchAll) {
        this();
        addPrinciples(principles);
        addEntities(entities);
        addEntries(entries);
        setMatchAll(matchAll);
    }

    /**
     * Convienience method to add a principle to the principles collection.
     *
     * @param principle to add
     */
    public void addPrinciple(AMEEEntity principle) {
        if (principle == null) throw new IllegalArgumentException("The principle argument must not be null.");
        getPrinciples().add(principle);
    }

    /**
     * Convienience method to add a list of principles to the principles collections.
     *
     * @param principles to add
     */
    public void addPrinciples(List<AMEEEntity> principles) {
        if (principles == null) throw new IllegalArgumentException("The principles argument must not be null.");
        getPrinciples().addAll(principles);
    }

    /**
     * Convienience method to add an entity to the entities collection.
     *
     * @param entity to add
     */
    public void addEntity(AMEEEntity entity) {
        if (entity == null) throw new IllegalArgumentException("The entity argument must not be null.");
        getEntities().add(entity);
    }

    /**
     * Convienience method to add a list of entities to the entities collection.
     *
     * @param entities to add
     */
    public void addEntities(List<AMEEEntity> entities) {
        if (entities == null) throw new IllegalArgumentException("The entities argument must not be null.");
        getEntities().addAll(entities);
    }

    /**
     * Convienience method to add permission entries. PermissionEntry instances will be constructed
     * for each value in the supplied comma delimited String.
     *
     * @param entries to add
     */
    public void addEntries(String entries) {
        if (entries == null) throw new IllegalArgumentException("The entries argument must not be null.");
        for (String entry : entries.split(",")) {
            getEntries().add(new PermissionEntry(entry));
        }
    }

    /**
     * Convienience method to add permission entries.
     *
     * @param entries to add
     */
    public void addEntries(Set<PermissionEntry> entries) {
        if (entries == null) throw new IllegalArgumentException("The entries argument must not be null.");
        getEntries().addAll(entries);
    }

    /**
     * Returns the principles list.
     *
     * @return principles list.
     */
    public List<AMEEEntity> getPrinciples() {
        return principles;
    }

    /**
     * Returns the entities list.
     *
     * @return entities list.
     */
    public List<AMEEEntity> getEntities() {
        return entities;
    }

    /**
     * Returns the set of permission entries.
     *
     * @return set of permission entries.
     */
    public Set<PermissionEntry> getEntries() {
        return entries;
    }

    /**
     * Ruturns true if AuthorizationContext is set to match all permission entries.
     *
     * @return true if all permission entries should be matched
     */
    public boolean isMatchAll() {
        return matchAll;
    }

    /**
     * Set true if all permission entries should be match.
     *
     * @param matchAll true if all permission entries should be match
     */
    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }
}
