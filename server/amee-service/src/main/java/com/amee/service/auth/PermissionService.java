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
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ObjectType;
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PermissionService {

    /**
     * Defines which 'principles' (keys) can relate to which 'entities' (values).
     */
    public final static Map<ObjectType, Set<ObjectType>> PRINCIPLE_ENTITY = new HashMap<ObjectType, Set<ObjectType>>();

    /**
     * Define which principles can relate to which entities.
     */
    {
        // Users <--> Entities
        addPrincipleAndEntity(ObjectType.USR, ObjectType.ENV);
        addPrincipleAndEntity(ObjectType.USR, ObjectType.PR);
        addPrincipleAndEntity(ObjectType.USR, ObjectType.DC);
        addPrincipleAndEntity(ObjectType.USR, ObjectType.PI);
        addPrincipleAndEntity(ObjectType.USR, ObjectType.DI);
        addPrincipleAndEntity(ObjectType.USR, ObjectType.IV);

        // Groups <--> Entities
        addPrincipleAndEntity(ObjectType.GRP, ObjectType.ENV);
        addPrincipleAndEntity(ObjectType.GRP, ObjectType.PR);
        addPrincipleAndEntity(ObjectType.GRP, ObjectType.DC);
        addPrincipleAndEntity(ObjectType.GRP, ObjectType.PI);
        addPrincipleAndEntity(ObjectType.GRP, ObjectType.DI);
        addPrincipleAndEntity(ObjectType.GRP, ObjectType.IV);
    }

    @Autowired
    private PermissionServiceDAO dao;

    // Authorization.

    public List<Permission> getPermissionsForEntity(IAMEEEntityReference entity) {
        if ((entity == null) || !isValidEntity(entity)) {
            throw new IllegalArgumentException();
        }
        return dao.getPermissionsForEntity(entity);
    }

    public Permission getPermissionForEntity(IAMEEEntityReference entity, PermissionEntry entry) {
        List<Permission> permissions = getPermissionsForEntity(entity);
        for (Permission permission : permissions) {
            if (permission.getEntries().contains(entry)) {
                return permission;
            }
        }
        return null;
    }

    public List<Permission> getPermissionsForPrinciples(Collection<IAMEEEntityReference> principles) {
        if ((principles == null) || principles.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<Permission> permissions = new ArrayList<Permission>();
        for (IAMEEEntityReference principle : principles) {
            permissions.addAll(getPermissionsForPrinciple(principle));
        }
        return permissions;
    }

    public List<Permission> getPermissionsForPrinciple(IAMEEEntityReference principle) {
        if ((principle == null) || !isValidPrinciple(principle)) {
            throw new IllegalArgumentException();
        }
        return dao.getPermissionsForPrinciple(principle, null);
    }

    /**
     * Fetch a List of all available Permissions matching the supplied principle and entity.
     *
     * @param principle to match on
     * @param entity    to match on
     * @return list of matching permissions
     */
    public List<Permission> getPermissionsForPrincipleAndEntity(AMEEEntity principle, AMEEEntity entity) {
        if ((principle == null) || (entity == null) || !isValidPrincipleToEntity(principle, entity)) {
            throw new IllegalArgumentException();
        }
        List<Permission> permissions = new ArrayList<Permission>();
        permissions.addAll(entity.getPermissionsForPrincipleAndEntity(principle, entity));
        permissions.addAll(dao.getPermissionsForPrincipleAndEntity(principle, entity));
        return permissions;
    }

    public void trashPermissionsForEntity(IAMEEEntityReference entity) {
        dao.trashPermissionsForEntity(entity);
    }

    private void addPrincipleAndEntity(ObjectType principle, ObjectType entity) {
        Set<ObjectType> entities = PRINCIPLE_ENTITY.get(principle);
        if (entities == null) {
            entities = new HashSet<ObjectType>();
            PRINCIPLE_ENTITY.put(principle, entities);
        }
        entities.add(entity);
    }

    public boolean isValidPrinciple(IAMEEEntityReference principle) {
        if (principle == null) throw new IllegalArgumentException();
        return PRINCIPLE_ENTITY.keySet().contains(principle.getObjectType());
    }

    public boolean isValidEntity(IAMEEEntityReference entity) {
        if (entity == null) throw new IllegalArgumentException();
        for (Set<ObjectType> entities : PRINCIPLE_ENTITY.values()) {
            if (entities.contains(entity.getObjectType())) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidPrincipleToEntity(IAMEEEntityReference principle, IAMEEEntityReference entity) {
        if ((principle == null) || (entity == null)) throw new IllegalArgumentException();
        return isValidPrinciple(principle) &&
                PRINCIPLE_ENTITY.get(principle.getObjectType()).contains(entity.getObjectType());
    }
}
