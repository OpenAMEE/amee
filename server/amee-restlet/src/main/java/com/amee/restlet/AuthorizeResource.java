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
package com.amee.restlet;

import com.amee.domain.AMEEEntity;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.auth.AccessSpecification;
import com.amee.domain.auth.AuthorizationContext;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.data.DataCategory;
import com.amee.service.auth.AuthorizationService;
import com.amee.service.auth.GroupService;
import com.amee.service.data.DataService;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An abstract base Resource class providing authentication functionality. Sub-classes
 * must, at least, implement getEntities. Sub-classes are expected to implement doGet, doAccept, doStore,
 * doRemove as required. Other methods can be overriden for further custom behaviour.
 */
public abstract class AuthorizeResource extends BaseResource {

    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    protected GroupService groupService;

    @Autowired
    protected DataService dataService;

    protected AuthorizationContext authorizationContext;

    /**
     * Flag indicating if a super-user is required?
     */
    private boolean requireSuperUser = false;

    /**
     * Ensure AuthorizationContext instance is always available to templates.
     *
     * @return the template map
     */
    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("authorizationContext", authorizationContext);
        values.put("authorizationService", authorizationService);
        return values;
    }

    /**
     * Overrides Resource.handleGet to enforce authorization for GET requests.
     * Calls hasPermissions with the AccessSpecifications from getGetAccessSpecifications. If
     * hasPermissions returns true (authorized) then doGet is called, otherwise notAuthorized is
     * called.
     */
    @Override
    public void handleGet() {
        log.debug("handleGet()");
        if (isAvailable()) {
            if (isAuthorized(getGetAccessSpecifications())) {
                doGet();
            } else {
                notAuthorized();
            }
        } else {
            super.handleGet();
        }
    }

    /**
     * Get the AccessSpecifications for GET requests. Creates an AccessSpecification for each entity
     * from getEntities with VIEW as the PermissionEntry. This specifies that principals must have VIEW permissions
     * for all the entities.
     *
     * @return AccessSpecifications for GET requests
     */
    public List<AccessSpecification> getGetAccessSpecifications() {
        List<AccessSpecification> accessSpecifications = new ArrayList<AccessSpecification>();
        for (IAMEEEntityReference entity : getDistinctEntities()) {
            accessSpecifications.add(new AccessSpecification(entity, PermissionEntry.VIEW));
        }
        return accessSpecifications;
    }

    /**
     * Handles a GET request. This is only called if the request is authorized. The default
     * implementation is to call handleGet in the super-class. The intention is for sub-classes
     * to override and provide their own GET handling implementation, if required.
     */
    public void doGet() {
        super.handleGet();
    }

    /**
     * Overrides Resource.acceptRepresentation to enforce authorization for POST requests.
     * Calls hasPermissions with the AccessSpecifications from getAcceptAccessSpecifications. If
     * hasPermissions returns true (authorized) then doAccept is called, otherwise notAuthorized is
     * called.
     */
    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        log.debug("acceptRepresentation()");
        if (isAvailable()) {
            if (isAuthorized(getAcceptAccessSpecifications())) {
                doAccept(entity);
            } else {
                notAuthorized();
            }
        } else {
            super.acceptRepresentation(entity);
        }
    }

    /**
     * Get the AccessSpecifications for POST requests. Updates the last entry from getGetAccessSpecifications with
     * the CREATE PermissionEntry. This specifies that principals must have VIEW permissions
     * for all the entities and VIEW & CREATE for the last entity.
     *
     * @return AccessSpecifications for POST requests
     */
    public List<AccessSpecification> getAcceptAccessSpecifications() {
        return updateLastAccessSpecificationWithPermissionEntry(getGetAccessSpecifications(), PermissionEntry.CREATE);
    }

    /**
     * Handles a POST request. This is only called if the request is authorized. The default
     * implementation is to call doAcceptOrStore. The intention is for sub-classes
     * to override and provide their own POST handling implementation, if required.
     *
     * @param entity representation
     */
    public void doAccept(Representation entity) {
        doAcceptOrStore(entity);
    }

    /**
     * Overrides Resource.storeRepresentation to enforce authorization for PUT requests.
     * Calls hasPermissions with the AccessSpecifications from getStoreAccessSpecifications. If
     * hasPermissions returns true (authorized) then doStore is called, otherwise notAuthorized is
     * called.
     */
    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        log.debug("storeRepresentation()");
        if (isAvailable()) {
            if (isAuthorized(getStoreAccessSpecifications())) {
                doStore(entity);
            } else {
                notAuthorized();
            }
        } else {
            super.storeRepresentation(entity);
        }
    }

    /**
     * Get the AccessSpecifications for PUT requests. Updates the last entry from getGetAccessSpecifications with
     * the MODIFY PermissionEntry. This specifies that principals must have VIEW permissions
     * for all the entities and VIEW & MODIFY for the last entity.
     *
     * @return AccessSpecifications for PUT requests
     */
    public List<AccessSpecification> getStoreAccessSpecifications() {
        return updateLastAccessSpecificationWithPermissionEntry(getGetAccessSpecifications(), PermissionEntry.MODIFY);
    }

    /**
     * Handles a PUT request. This is only called if the request is authorized. The default
     * implementation calls doAcceptOrStore. The intention is for sub-classes
     * to override and provide their own PUT handling implementation, if required.
     *
     * @param entity representation
     */
    public void doStore(Representation entity) {
        doAcceptOrStore(entity);
    }

    /**
     * Handles a POST or PUT request. This is only called if the request is authorized. The default
     * implementation throws UnsupportedOperationException. The intention is for sub-classes
     * to override and provide their own GET handling implementation, if required.
     *
     * @param entity representation
     */
    public void doAcceptOrStore(Representation entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * Overrides Resource.removeRepresentations to enforce authorization for DELETE requests.
     * Calls hasPermissions with the AccessSpecifications from getRemoveAccessSpecifications. If
     * hasPermissions returns true (authorized) then doRemove is called, otherwise notAuthorized is
     * called.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        log.debug("removeRepresentations()");
        if (isAvailable()) {
            if (isAuthorized(getRemoveAccessSpecifications())) {
                doRemove();
            } else {
                notAuthorized();
            }
        } else {
            super.removeRepresentations();
        }
    }

    /**
     * Get the AccessSpecifications for DELETE requests. Updates the last entry from getGetAccessSpecifications with
     * the DELETE PermissionEntry. This specifies that principals must have VIEW permissions
     * for all the entities and VIEW & DELETE for the last entity.
     *
     * @return AccessSpecifications for DELETE requests
     */
    public List<AccessSpecification> getRemoveAccessSpecifications() {
        return updateLastAccessSpecificationWithPermissionEntry(getGetAccessSpecifications(), PermissionEntry.DELETE);
    }

    /**
     * Handles a DELETE request. This is only called if the request is authorized. The default
     * implementation throws UnsupportedOperationException. The intention is for sub-classes
     * to override and provide their own DELETE handling implementation, if required.
     */
    public void doRemove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the request is authorized, otherwise false. AuthorizationService is used to
     * do the authorization based on the supplied AccessSpecifications and the principals from GetPrincipals.
     *
     * @param accessSpecifications to use for authorization
     * @return true if the request is authorized, otherwise false
     */
    public boolean isAuthorized(List<AccessSpecification> accessSpecifications) {
        authorizationContext = new AuthorizationContext();
        authorizationContext.addPrincipals(getPrincipals());
        authorizationContext.addAccessSpecifications(accessSpecifications);
        boolean authorized = authorizationService.isAuthorized(authorizationContext);
        if (authorized && isRequireSuperUser() && !authorizationContext.isSuperUser()) {
            authorized = false;
        }
        return authorized;
    }

    /**
     * Returns a list of principals involved in authorization. Permissions from these principals will
     * be compared against AccessSpecifications to determine if a request is authorized.
     *
     * @return a list of principals
     */
    public List<AMEEEntity> getPrincipals() {
        List<AMEEEntity> principals = new ArrayList<AMEEEntity>();
        principals.addAll(groupService.getGroupsForPrincipal(getActiveUser()));
        principals.add(getActiveUser());
        return principals;
    }

    /**
     * Returns a list of entities required for authorization for the current resource. The list is
     * in hierarchical order, from general to more specific (e.g., category -> sub-category -> item).
     *
     * @return list of entities required for authorization
     */
    public abstract List<IAMEEEntityReference> getEntities();

    /**
     * Returns a de-duped version of the list from getEntities().
     *
     * @return list of entities required for authorization
     */
    public List<IAMEEEntityReference> getDistinctEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        for (IAMEEEntityReference entity : getEntities()) {
            if (!entities.contains(entity)) {
                entities.add(entity);
            }
        }
        return entities;
    }

    /**
     * Updates the last AccessSpecification in the supplied list of AccessSpecifications with the PermissionEntry.
     *
     * @param specifications list of AccessSpecifications, of which the last will be updated.
     * @param entry          to add to last AccessSpecification
     * @return the list of AccessSpecifications
     */
    public List<AccessSpecification> updateLastAccessSpecificationWithPermissionEntry(List<AccessSpecification> specifications, PermissionEntry entry) {
        if (!specifications.isEmpty()) {
            specifications.get(specifications.size() - 1).getDesired().add(entry);
        }
        return specifications;
    }

    // TODO: What is this used by? Templates?

    public AuthorizationContext getAuthorizationContext() {
        return authorizationContext;
    }

    public boolean isRequireSuperUser() {
        return requireSuperUser;
    }

    public void setRequireSuperUser(boolean requireSuperUser) {
        this.requireSuperUser = requireSuperUser;
    }

    public DataCategory getRootDataCategory() {
        return dataService.getRootDataCategory();
    }
}