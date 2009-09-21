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
import com.amee.domain.auth.PermissionEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AuthorizationContext encapsulates the 'context' for an authorization request. The context
 * contains three main collections; a list of 'principals', a list of 'entities' and a set of
 * PermissionEntries. These collections are taken into consideration when deciding if a
 * request should be authorized.
 * <p/>
 * AuthorizationContext is a Spring bean in the prototype scope and is not considered thread safe.
 */
@Component
@Scope("prototype")
public class AuthorizationContext implements Serializable {

    @Autowired
    protected AuthorizationService authorizationService;

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
     * A set of PermissionEntries representing the consolidated & inherited access rights following
     * execution of AuthorizationService.isAuthorized(). The PermissionEntries will reflect the state-of-play at
     * the point of AuthorizationService.isAuthorized() returning ALLOW or DENY for the authorization check.
     */
    private Set<PermissionEntry> entries = new HashSet<PermissionEntry>();

    /**
     * Local cache for the result of AuthorizationService.isAuthorized().
     */
    private Boolean authorized = null;

    /**
     * Default constructor.
     */
    public AuthorizationContext() {
        super();
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
        if (accessSpecification == null)
            throw new IllegalArgumentException("The accessSpecification argument must not be null.");
        getAccessSpecifications().add(accessSpecification);
    }

    /**
     * Convienience method to add a list of access specifications to the accessSpecifications collection.
     *
     * @param accessSpecifications to add
     */
    public void addAccessSpecifications(List<AccessSpecification> accessSpecifications) {
        if (accessSpecifications == null)
            throw new IllegalArgumentException("The accessSpecifications argument must not be null.");
        getAccessSpecifications().addAll(accessSpecifications);
    }

    /**
     * Reset the AuthorizationContext. All collections are cleared.
     */
    public void reset() {
        authorized = null;
        principals.clear();
        accessSpecifications.clear();
        entries.clear();
    }

    /**
     * Returns true of the state of the AuthorizationContext should be considered authorized. Internally uses
     * AuthorizationService.isAuthorized and caches the result.
     *
     * @return true if authorize result is allow, otherwise false if result is deny
     */
    public boolean isAuthorized() {
        if (authorized == null) {
            authorized = authorizationService.isAuthorized(this);
        }
        return authorized;
    }

    /**
     * Returns true if the currently active principles have the requested access, via entry, to the
     * supplied entity. If the entity has already been considered for authorization then the cached
     * AccessSpecification is re-used. If the entity has not been considered yet then a fresh authorization
     * check is made, with the assumption that the entity is a direct child of the last entity declared in
     * accessSpecifications.
     *
     * @param entity to authorize access for
     * @param entry  specifying access requested
     * @return true if authorize result is allow, otherwise false if result is deny
     */
    public boolean isAuthorized(AMEEEntity entity, PermissionEntry entry) {
        if (entity.getAccessSpecification() != null) {
            return entity.getAccessSpecification().getActual().contains(entry);
        } else {
            return isAuthorized(new AccessSpecification(entity, entry));
        }
    }

    /**
     * Returns true if the currently active principles have access to the entity with the PermissionEntry in
     * the supplied AccessSpecification, with the assumption that the entity is a direct child of the last
     * entity declared in accessSpecifications.  
     *
     * @param accessSpecification desired AccessSpecification
     * @return true if authorize result is allow, otherwise false if result is deny
     */
    public boolean isAuthorized(AccessSpecification accessSpecification) {
        return authorizationService.isAuthorized(this, accessSpecification);
    }

    /**
     * Returns the principals list.
     *
     * @return principals list
     */
    public List<AMEEEntity> getPrincipals() {
        return principals;
    }

    /**
     * Returns the accessSpecifications list.
     *
     * @return accessSpecifications list
     */
    public List<AccessSpecification> getAccessSpecifications() {
        return accessSpecifications;
    }

    /**
     * Returns the entries set;
     *
     * @return entries set
     */
    public Set<PermissionEntry> getEntries() {
        return entries;
    }
}
