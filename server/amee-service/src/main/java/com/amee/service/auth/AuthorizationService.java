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
import com.amee.domain.AMEEStatus;
import com.amee.domain.auth.AccessSpecification;
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.auth.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthorizationService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private PermissionService permissionService;

    /**
     * Determines if the supplied AuthorizationContext is considered to be authorized or not. Will return true
     * if the authorize result is ALLOW, otherwise false if the result is DENY.
     * <p/>
     * The supplied AuthorizationContext encapsulates a list of principles and a list of AccessSpecification. The
     * aim is to discover if the principles have appropriate access to the entities within the AccessSpecification.
     * <p/>
     * The authorization rules are:
     * <p/>
     * - Super-users, always ALLOW and return.
     * - DENY if there are no AccessSpecifications.
     * - Each AccessSpecification is evaluated in hierarchical order (e.g., category -> sub-category -> item).
     * - Principles are evaluated from broader to narrower scope (e.g., organisation -> department -> individual).
     * - PermissionEntries are consolidated from all Permissions for principle & entity combinations.
     * - The PermissionEntries are merged for in each iteration allowing inheritence of permissions.
     * - Permissions from later principles override earlier ones.
     * - If an OWN PermissionEntry is present then ALLOW and return.
     * - TODO
     * - Stop on DENY, don't evaluate entities further down the hierarchy.
     * - Inherited Permissions for earlier entities can be superceeded by those from later entities.
     * - Multiple Permissions for related principles have an 'election' with the most permissive Permission winning.
     * <p/>
     *
     * @param authorizationContext to consider for authorization
     * @return true if authorize result is allow, otherwise false if result is deny
     */
    public boolean isAuthorized(AuthorizationContext authorizationContext) {

        List<Permission> permissions;
        List<PermissionEntry> entityEntries;
        Set<PermissionEntry> allEntries = new HashSet<PermissionEntry>();

        // Super-users can do anything. Stop here.
        if (isSuperUser(authorizationContext.getPrinciples())) {
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

            // Gather all Permissions for principles for current entity.
            permissions = new ArrayList<Permission>();
            for (AMEEEntity principle : authorizationContext.getPrinciples()) {
                permissions.addAll(permissionService.getPermissionsForPrincipleAndEntity(principle, accessSpecification.getEntity()));
            }

            // Get list of PermissionEntries for current entity from Permissions.
            entityEntries = getPermissionEntries(permissions);

            // Merge PermissionEntries for current entity with inherited PermissionEntries.
            mergePermissionEntries(allEntries, entityEntries);

            // Remove PermissionEntries that don't match the entity state.
            removePermissionEntriesWithoutState(allEntries, accessSpecification.getEntity().getStatus());

            // Owner can do anything.
            if (allEntries.contains(PermissionEntry.OWN)) {
                log.debug("isAuthorized() - ALLOW (owner)");
                return true;
            }

            // Principles must be able to do everything specified.
            if (!allEntries.containsAll(accessSpecification.getEntries())) {
                log.debug("isAuthorized() - DENY (not permitted)");
                return false;
            }
        }

        // Got to the bottom of the hierarchy, ALLOW.
        log.debug("isAuthorized() - ALLOW");
        return true;
    }

    protected void mergePermissionEntries(Collection<PermissionEntry> entries, Collection<PermissionEntry> newEntries) {
        PermissionEntry pe1;
        Iterator<PermissionEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            pe1 = iterator.next();
            for (PermissionEntry pe2 : newEntries) {
                if (pe1.getValue().equals(pe2.getValue())) {
                    iterator.remove();
                    break;
                }
            }
        }
        entries.addAll(newEntries);
    }

    protected List<PermissionEntry> getPermissionEntries(Collection<Permission> permissions) {
        List<PermissionEntry> entries = new ArrayList<PermissionEntry>();
        for (Permission permission : permissions) {
            entries.addAll(permission.getEntries());
        }
        return entries;
    }

    protected void removePermissionEntriesWithoutState(Collection<PermissionEntry> entries, AMEEStatus status) {
        Iterator<PermissionEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getStatus().equals(status)) {
                iterator.remove();
            }
        }
    }

    public boolean isSuperUser(Collection<AMEEEntity> principles) {
        for (AMEEEntity principle : principles) {
            if (isSuperUser(principle)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuperUser(AMEEEntity principle) {
        if (User.class.isAssignableFrom(principle.getClass())) {
            if (((User) principle).isSuperUser()) {
                log.debug("isAuthorized() - true");
                return true;
            }
        }
        return false;
    }
}