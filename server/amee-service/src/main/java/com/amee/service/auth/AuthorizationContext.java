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
import com.amee.domain.auth.AccessSpecification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthorizationContext encapsulates the 'context' for an authorization request. The context
 * contains three main collections; a list of 'principals', a list of 'entities' and a set of
 * PermissionEntries. These collections are taken into consideration when deciding if a
 * request should be authorized.
 */
public class AuthorizationContext implements Serializable {

    /**
     * A list of principals which may or may not be authorized for the entities.
     */
    private List<AMEEEntity> principals = new ArrayList<AMEEEntity>();

    /**
     * A list of entities over which the principals may or may not have permissions, along with
     * the requested permission entries.
     */
    private List<AccessSpecification> accessSpecifications = new ArrayList<AccessSpecification>();

    /**
     * Default constructor.
     */
    public AuthorizationContext() {
        super();
    }

    /**
     * Construct an AuthorizationContext with the supplied principal, entity and entries.
     *
     * @param principal
     * @param accessSpecification
     */
    public AuthorizationContext(AMEEEntity principal, AccessSpecification accessSpecification) {
        this();
        addPrincipal(principal);
        addAccessSpecification(accessSpecification);
    }

    /**
     * Construct an AuthorizationContext with the supplied principals, entities and entries.
     *
     * @param principals
     * @param accessSpecifications
     */
    public AuthorizationContext(List<AMEEEntity> principals, List<AccessSpecification> accessSpecifications) {
        this();
        addPrincipals(principals);
        addAccessSpecifications(accessSpecifications);
    }

    /**
     * Convienience method to add a principal to the principals collection.
     *
     * @param principal to add
     */
    public void addPrincipal(AMEEEntity principal) {
        if (principal == null) throw new IllegalArgumentException("The principal argument must not be null.");
        getPrincipals().add(principal);
    }

    /**
     * Convienience method to add a list of principals to the principals collections.
     *
     * @param principals to add
     */
    public void addPrincipals(List<AMEEEntity> principals) {
        if (principals == null) throw new IllegalArgumentException("The principals argument must not be null.");
        getPrincipals().addAll(principals);
    }

    /**
     * Convienience method to add an access specification to the accessSpecifications collection.
     *
     * @param accessSpecification to add
     */
    public void addAccessSpecification(AccessSpecification accessSpecification) {
        if (accessSpecification == null) throw new IllegalArgumentException("The accessSpecification argument must not be null.");
        getAccessSpecifications().add(accessSpecification);
    }

    /**
     * Convienience method to add a list of access specifications to the accessSpecifications collection.
     *
     * @param accessSpecifications to add
     */
    public void addAccessSpecifications(List<AccessSpecification> accessSpecifications) {
        if (accessSpecifications == null) throw new IllegalArgumentException("The accessSpecifications argument must not be null.");
        getAccessSpecifications().addAll(accessSpecifications);
    }

    /**
     * Returns the principals list.
     *
     * @return principals list.
     */
    public List<AMEEEntity> getPrincipals() {
        return principals;
    }

    /**
     * Returns the accessSpecifications list.
     *
     * @return accessSpecifications list.
     */
    public List<AccessSpecification> getAccessSpecifications() {
        return accessSpecifications;
    }
}
