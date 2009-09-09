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

import com.amee.domain.*;
import com.amee.domain.auth.Permission;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class PermissionServiceDAO {

    private static final String CACHE_REGION = "query.permissionService";

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings(value = "unchecked")
    public List<Permission> getPermissionsForEntity(IAMEEEntityReference entity) {
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("entityReference.entityId", entity.getEntityId()));
        criteria.add(Restrictions.eq("entityReference.entityType", entity.getObjectType().getName()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    @SuppressWarnings(value = "unchecked")
    public List<Permission> getPermissionsForPrinciple(IAMEEEntityReference principle, Class entityClass) {
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("principleReference.entityId", principle.getEntityId()));
        criteria.add(Restrictions.eq("principleReference.entityType", principle.getObjectType().getName()));
        if (entityClass != null) {
            criteria.add(Restrictions.eq("entityReference.entityType", ObjectType.getType(entityClass).getName()));
        }
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    @SuppressWarnings(value = "unchecked")
    public List<Permission> getPermissionsForPrincipleAndEntity(IAMEEEntityReference principle, IAMEEEntityReference entity) {
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("principleReference.entityId", principle.getEntityId()));
        criteria.add(Restrictions.eq("principleReference.entityType", principle.getObjectType().getName()));
        criteria.add(Restrictions.eq("entityReference.entityId", entity.getEntityId()));
        criteria.add(Restrictions.eq("entityReference.entityType", entity.getObjectType().getName()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    @SuppressWarnings(value = "unchecked")
    public AMEEEntity getEntity(IAMEEEntityReference entityReference) {
        if (entityReference == null) {
            throw new IllegalArgumentException();
        }
        return (AMEEEntity) entityManager.find(
                entityReference.getObjectType().getClazz(), entityReference.getEntityId());
    }

    public void trashPermissionsForEntity(IAMEEEntityReference entity) {
        entityManager.createQuery(
                "UPDATE Permission " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE entityReference.entityId = :entityId " +
                        "AND entityReference.entityType = :entityType " +
                        "AND status != :trash")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("entityId", entity.getEntityId())
                .setParameter("entityType", entity.getObjectType().getName())
                .executeUpdate();
    }
}
