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
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.auth.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class AuthorizationService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private PermissionService permissionService;

    // TODO: Handle deprecated entities.
    // TODO: Handle hierarchies.
    public boolean isAuthorized(AuthorizationContext authorizationContext) {
        // Permissions for earlier entities can be superceeded by those from later entities.
        for (AMEEEntity entity : authorizationContext.getEntities()) {
            // Permissions for earlier principles can be superceeded by those from later principles.
            for (AMEEEntity principle : authorizationContext.getPrinciples()) {
                // Exception for Users who are super-users.
                if (User.class.isAssignableFrom(principle.getClass())) {
                    if (((User) principle).isSuperUser()) {
                        log.debug("isAuthorized() - true");
                        return true;
                    }
                }
                // Check permissions for this principle and entity combination.
                Collection<Permission> permissions = permissionService.getPermissionsForPrincipleAndEntity(principle, entity);
                for (Permission permission : permissions) {
                    // Do we need to match all of the entries?
                    if (authorizationContext.isMatchAll()) {
                        // Does this Permission contain all of the entries supplied?
                        if (permission.getEntries().containsAll(authorizationContext.getEntries())) {
                            log.debug("isAuthorized() - true");
                            return true;
                        }
                    } else {
                        // Does this Permission contain at least one of the entries supplied?
                        for (PermissionEntry entry : authorizationContext.getEntries()) {
                            if (permission.getEntries().contains(entry)) {
                                log.debug("isAuthorized() - true");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        log.debug("isAuthorized() - false");
        return false;
    }
}