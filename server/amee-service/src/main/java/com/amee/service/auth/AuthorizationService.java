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
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.auth.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.io.Serializable;

@Service
public class AuthorizationService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private PermissionService permissionService;

    /**
     * Returns true if the supplied AuthorizationContext is considered to be authorized.
     * <p/>
     * The supplied AuthorizationContext encapsulates a list of principals and a list of AccessSpecifications. The
     * aim is to discover if the principals have the requested access rights to the entities within the AccessSpecification.
     * <p/>
     * The authorization rules are:
     * <p/>
     * - Super-users are always authorized (return true).
     * - Always deny access if there are no AccessSpecifications.
     * - Each AccessSpecification is evaluated in entity hierarchical order (e.g., category -> sub-category -> item).
     * - Principals are evaluated from broader to narrower scope (e.g., organisation -> department -> individual).
     * - PermissionEntries are consolidated from all Permissions for each principal & entity combination.
     * - The PermissionEntries are inherited down the entity hierarchy.
     * - PermissionEntries for later principal & entity combinations override those that are inherited.
     * - Always authorize if an OWN PermissionEntry is present for an entity (return true).
     * - Apply isAuthorized(AccessSpecification, Collection<PermissionEntry>) to each AccessSpecification, return false
     * if not authorized.
     * - Return authorized (true) if isAuthorized is passed for each entity.
     *
     * @param authorizationContext to consider for authorization
     * @return true if authorize result is allow, otherwise false if result is deny
     */
    public boolean isAuthorized(AuthorizationContext authorizationContext) {

        List<Permission> permissions;
        List<PermissionEntry> entityEntries;

        // Super-users can do anything. Stop here.
        // TODO: Jumping out here means accessSpecification.actual will not be populated.
        if (isSuperUser(authorizationContext.getPrincipals())) {
            log.debug("isAuthorized() - ALLOW (super-user)");
            return true;
        }

        // Deny if there are no AccessSpecifications. Pretty odd if this happens...
        if (authorizationContext.getAccessSpecifications().isEmpty()) {
            log.debug("isAuthorized() - DENY (not permitted)");
            return false;
        }

        // Iterate over AccessSpecifications (entities) in hierarchical order.
        for (AccessSpecification accessSpecification : authorizationContext.getAccessSpecifications()) {

            // Gather all Permissions for principals for current entity.
            permissions = new ArrayList<Permission>();
            for (AMEEEntity principal : authorizationContext.getPrincipals()) {
                permissions.addAll(permissionService.getPermissionsForPrincipalAndEntity(principal, accessSpecification.getEntity()));
            }

            // Get list of PermissionEntries for current entity from Permissions.
            entityEntries = getPermissionEntries(permissions);

            // Merge PermissionEntries for current entity with inherited PermissionEntries.
            mergePermissionEntries(authorizationContext.getEntries(), entityEntries);

            // Update the AccessSpecification with the actual PermissionEntries for the
            // current principals related to the current entity.
            accessSpecification.setActual(authorizationContext.getEntries());

            // Owner can do anything.
            if (authorizationContext.getEntries().contains(PermissionEntry.OWN)) {
                log.debug("isAuthorized() - ALLOW (owner)");
                return true;
            }

            // Principals must be able to do everything specified.
            if (!isAuthorized(accessSpecification, authorizationContext.getEntries())) {
                log.debug("isAuthorized() - DENY (not permitted)");
                return false;
            }
        }

        // Got to the bottom of the hierarchy, ALLOW.
        log.debug("isAuthorized() - ALLOW");
        return true;
    }

    /**
     * Gets a List of PermissionEntries from a Collection of Permissions. The PermissionEntries list contains all
     * the PermissionEntries from within all the Permissions.
     *
     * @param permissions tp get PermissionEntries from
     * @return PermissionEntries list
     */
    protected List<PermissionEntry> getPermissionEntries(Collection<Permission> permissions) {
        List<PermissionEntry> entries = new ArrayList<PermissionEntry>();
        for (Permission permission : permissions) {
            entries.addAll(permission.getEntries());
        }
        return entries;
    }

    /**
     * Merges two PermissionEntries Collections. The merge consists of two stages. Firstly, remove PermissionEntries
     * from the target collection where those from the source collection have a different allow flag. Secondly, add
     * all PermissionEntries from the source collection to the target collection.
     *
     * @param targetEntries target collection
     * @param sourceEntries source collection
     */
    protected void mergePermissionEntries(Collection<PermissionEntry> targetEntries, Collection<PermissionEntry> sourceEntries) {
        PermissionEntry pe1;
        Iterator<PermissionEntry> iterator = targetEntries.iterator();
        while (iterator.hasNext()) {
            pe1 = iterator.next();
            for (PermissionEntry pe2 : sourceEntries) {
                if (pe1.getValue().equals(pe2.getValue()) && pe1.getStatus().equals(pe2.getStatus())) {
                    iterator.remove();
                    break;
                }
            }
        }
        targetEntries.addAll(sourceEntries);
    }

    /**
     * Returns true if access is authorized to an entity. The AccessSpecification declares the entity and what
     * kind of access is desired. The PermissionEntry collection declares what kind of access principals are
     * allowed for the entity.
     *
     * @param accessSpecification specification of access requested to an entity
     * @param principalEntries    PermissionEntries from the principals
     * @return true if access is authorized
     */
    protected boolean isAuthorized(AccessSpecification accessSpecification, Collection<PermissionEntry> principalEntries) {
        // Default to not authorized.
        Boolean authorized = false;
        // Iterate over the desired PermissionEntries specified for the entity.
        for (PermissionEntry pe1 : accessSpecification.getDesired()) {
            // Default to not authorized.
            authorized = false;
            // Iterate over PermissionEntries associated with current principals.
            for (PermissionEntry pe2 : principalEntries) {
                // Authorized if:
                // - Both PermissionEntries match by value.
                // - Principals PermissionEntry is allowed.
                // - Principals PermissionEntry status matches the entity status.
                if (pe1.getValue().equals(pe2.getValue()) &&
                        pe2.isAllow() &&
                        (pe2.getStatus().equals(accessSpecification.getEntity().getStatus()))) {
                    // Authorized, no need to continue so break. Most permissive principal PermissionEntry 'wins'.
                    authorized = true;
                    break;
                }
            }
            // Stop now if not authorized.
            if (!authorized) {
                break;
            }
        }
        return authorized;
    }

    /**
     * Return true if there is a super-user in the supplied Collection of principals.
     *
     * @param principals to check for presence of super-user
     * @return true if super-user is found
     */
    public boolean isSuperUser(Collection<AMEEEntity> principals) {
        for (AMEEEntity principal : principals) {
            if (isSuperUser(principal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the principal supplied is a User and is a super-user.
     *
     * @param principal to examine
     * @return true if principal is a super-user
     */
    public boolean isSuperUser(AMEEEntity principal) {
        if (User.class.isAssignableFrom(principal.getClass())) {
            if (((User) principal).isSuperUser()) {
                log.debug("isAuthorized() - true");
                return true;
            }
        }
        return false;
    }
}