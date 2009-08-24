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
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Service
@Scope("prototype")
public class PermissionService {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.permissionService";

    @PersistenceContext
    private EntityManager entityManager;

    public List<Permission> getPermissionsForEntity(AMEEEntity entity) {
        if ((entity == null) || !Permission.isValidEntity(entity)) {
            throw new IllegalArgumentException();
        }
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("entityReference.entityId", entity.getId()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    public List<Permission> getPermissionsForPrinciple(AMEEEntity principle) {
        if ((principle == null) || !Permission.isValidPrinciple(principle)) {
            throw new IllegalArgumentException();
        }
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("principleReference.entityId", principle.getId()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    public List<Permission> getPermissionsForPrincipleAndEntity(AMEEEntity principle, AMEEEntity entity) {
        if ((principle == null) || (entity == null) || !Permission.isValidPrincipleToEntity(principle, entity)) {
            throw new IllegalArgumentException();
        }
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("principleReference.entityId", principle.getId()));
        criteria.add(Restrictions.eq("entityReference.entityId", entity.getId()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    public boolean hasPermissions(AMEEEntity principle, AMEEEntity entity, String entries) {
        Set<PermissionEntry> entrySet = new HashSet<PermissionEntry>();
        for (String entry : entries.split(",")) {
            entrySet.add(new PermissionEntry(entry));
        }
        return hasPermissions(principle, entity, entrySet);
    }

    public boolean hasPermissions(AMEEEntity principle, AMEEEntity entity, Set<PermissionEntry> entrySet) {
        List<AMEEEntity> principles = new ArrayList<AMEEEntity>();
        principles.add(principle);
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(entity);
        return hasPermissions(principles, entities, entrySet);
    }

    public boolean hasPermissions(List<AMEEEntity> principles, List<AMEEEntity> entities, Set<PermissionEntry> entrySet) {
        // Permissions for earlier entities can be superceeded by those from later entities.
        for (AMEEEntity entity : entities) {
            // Permissions for earlier principles can be superceeded by those from later principles.
            for (AMEEEntity principle : principles) {
                // Check permissions for this principle and entity combination.
                Collection<Permission> permissions = getPermissionsForPrincipleAndEntity(principle, entity);
                for (Permission permission : permissions) {
                    // Does this Permission contain all of the entries supplied?
                    if (permission.getEntries().containsAll(entrySet)) {
                        log.debug("hasPermissions() - yeh");
                        return true;
                    }
                }
            }
        }
        log.debug("hasPermissions() - nah");
        return false;
    }
}
