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
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import com.amee.service.auth.AuthorizationContext;
import com.amee.service.auth.AuthorizationService;
import com.amee.service.environment.GroupService;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AuthorizeResource extends BaseResource {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private GroupService groupService;

    @Override
    public void handleGet() {
        log.debug("handleGet()");
        if (isAvailable()) {
            if (hasPermissions(getGetPermissionEntries())) {
                doGet();
            } else {
                notAuthorized();
            }
        } else {
            super.handleGet();
        }
    }

    /**
     * Get the PermissionEntries for getting a resource.
     *
     * @return PermissionEntries for getting a resource
     */
    public Set<PermissionEntry> getGetPermissionEntries() {
        return Permission.OWN_VIEW;
    }

    public void doGet() {
        super.handleGet();
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        log.debug("acceptRepresentation()");
        if (isAvailable()) {
            if (hasPermissions(getAcceptPermissionEntries())) {
                doAccept(entity);
            } else {
                notAuthorized();
            }
        } else {
            super.acceptRepresentation(entity);
        }
    }

    /**
     * Get the PermissionEntries for accepting a resource.
     *
     * @return PermissionEntries for accepting a resource
     */
    public Set<PermissionEntry> getAcceptPermissionEntries() {
        return Permission.OWN_CREATE;
    }

    public void doAccept(Representation entity) {
        doAcceptOrStore(entity);
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        log.debug("storeRepresentation()");
        if (isAvailable()) {
            if (hasPermissions(getStorePermissionEntries())) {
                doStore(entity);
            } else {
                notAuthorized();
            }
        } else {
            super.storeRepresentation(entity);
        }
    }

    /**
     * Get the PermissionEntries for storing a resource.
     *
     * @return PermissionEntries for storing a resource
     */
    public Set<PermissionEntry> getStorePermissionEntries() {
        return Permission.OWN_MODIFY;
    }

    public void doStore(Representation entity) {
        doAcceptOrStore(entity);
    }

    public void doAcceptOrStore(Representation entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        log.debug("removeRepresentations()");
        if (isAvailable()) {
            if (hasPermissions(getRemovePermissionEntries())) {
                doRemove();
            } else {
                notAuthorized();
            }
        } else {
            super.removeRepresentations();
        }
    }

    /**
     * Get the PermissionEntries for removing a resource.
     *
     * @return PermissionEntries for removing a resource
     */
    public Set<PermissionEntry> getRemovePermissionEntries() {
        return Permission.OWN_DELETE;
    }

    public void doRemove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermissions(Set<PermissionEntry> entrySet) {
        return authorizationService.isAuthorized(
                new AuthorizationContext(getPrinciples(), getEntities(), entrySet));
    }

    public List<AMEEEntity> getPrinciples() {
        List<AMEEEntity> principles = new ArrayList<AMEEEntity>();
        principles.add(getActiveUser());
        principles.addAll(groupService.getGroupsForPrinciple(getActiveUser()));
        return principles;
    }

    public abstract List<AMEEEntity> getEntities();
}
