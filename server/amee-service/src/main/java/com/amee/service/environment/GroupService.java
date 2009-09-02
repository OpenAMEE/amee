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
package com.amee.service.environment;

import com.amee.domain.*;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrinciple;
import com.amee.domain.environment.Environment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class GroupService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.groupService";

    @PersistenceContext
    private EntityManager entityManager;

    // Events

    public void beforeGroupDelete(Group group) {
        log.debug("beforeGroupDelete");
        // TODO: More cascade dependencies?
    }

    public void beforeGroupPrincipleDelete(GroupPrinciple groupPrinciple) {
        log.debug("beforeGroupPrincipleDelete");
        // TODO: More cascade dependencies?
    }

    // Groups

    public Group getGroupByUid(Environment environment, String uid) {
        Group group = null;
        List<Group> groups = entityManager.createQuery(
                "SELECT g FROM Group g " +
                        "WHERE g.environment.id = :environmentId " +
                        "AND g.uid = :uid " +
                        "AND g.status != :trash")
                .setParameter("environmentId", environment.getId())
                .setParameter("uid", uid)
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        if (groups.size() > 0) {
            group = groups.get(0);
        }
        return group;
    }

    public Group getGroupByName(Environment environment, String name) {
        Group group = null;
        List<Group> groups = entityManager.createQuery(
                "SELECT g FROM Group g " +
                        "WHERE g.environment.id = :environmentId " +
                        "AND g.name = :name " +
                        "AND g.status != :trash")
                .setParameter("environmentId", environment.getId())
                .setParameter("name", name.trim())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        if (groups.size() > 0) {
            group = groups.get(0);
        }
        return group;
    }

    public List<Group> getGroups(Environment environment) {
        List<Group> groups = entityManager.createQuery(
                "SELECT g " +
                        "FROM Group g " +
                        "WHERE g.environment.id = :environmentId " +
                        "AND g.status != :trash " +
                        "ORDER BY g.name")
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return groups;
    }

    public List<Group> getGroups(Environment environment, Pager pager) {

        Query query;

        String pagerSetClause = getPagerSetClause("g", pager);

        // first count all objects
        query = entityManager.createQuery(
                "SELECT count(g) " +
                        "FROM Group g " +
                        "WHERE g.environment.id = :environmentId " + pagerSetClause + " " +
                        "AND g.status != :trash ")
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION);
        if (!"".equals(pagerSetClause)) {
            query.setParameter("pagerSet", pager.getPagerSet());
        }
        // tell pager how many objects there are and give it a chance to select the requested page again
        long count = (Long) query.getSingleResult();
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        query = entityManager.createQuery(
                "SELECT g " +
                        "FROM Group g " +
                        "WHERE g.environment.id = :environmentId " + pagerSetClause + " " +
                        "AND g.status != :trash " +
                        "ORDER BY g.name")
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart());
        if (!"".equals(pagerSetClause)) {
            query.setParameter("pagerSet", pager.getPagerSet());
        }
        List<Group> groups = query.getResultList();
        // update the pager
        pager.setItemsFound(groups.size());
        // all done, return results
        return groups;
    }

    private String getPagerSetClause(String alias, Pager pager) {

        StringBuilder ret = new StringBuilder();
        if (pager.isPagerSetApplicable()) {
            if (pager.getPagerSetType().equals(PagerSetType.INCLUDE)) {
                ret.append(" AND ");
                ret.append(alias);
                ret.append(" IN (:pagerSet) ");
            } else {
                ret.append(" AND ");
                ret.append(alias);
                ret.append(" NOT IN (:pagerSet) ");
            }
        }
        return ret.toString();
    }

    public void save(Group group) {
        entityManager.persist(group);
    }

    public void remove(Group group) {
        beforeGroupDelete(group);
        group.setStatus(AMEEStatus.TRASH);
    }

    // GroupPrinciples

    public GroupPrinciple getGroupPrincipleByUid(Environment environment, String uid) {
        GroupPrinciple groupPrinciple = null;
        if ((environment != null) && (uid != null)) {
            List<GroupPrinciple> groupPrinciples = entityManager.createQuery(
                    "SELECT gp FROM GroupPrinciple gp " +
                            "WHERE gp.environment.id = :environmentId " +
                            "AND gp.uid = :uid " +
                            "AND gp.status != :trash")
                    .setParameter("environmentId", environment.getId())
                    .setParameter("uid", uid)
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                    .getResultList();
            if (groupPrinciples.size() > 0) {
                groupPrinciple = groupPrinciples.get(0);
            }
        }
        return groupPrinciple;
    }

    public GroupPrinciple getGroupPrinciple(Group group, IAMEEEntityReference entity) {
        GroupPrinciple groupPrinciple = null;
        if ((group != null) && (entity != null)) {
            List<GroupPrinciple> groupPrinciples = entityManager.createQuery(
                    "SELECT gp FROM GroupPrinciple gp " +
                            "WHERE gp.environment.id = :environmentId " +
                            "AND gp.group.id = :groupId " +
                            "AND gp.principleReference.entityId = :entityId " +
                            "AND gp.principleReference.entityType = :entityType " +
                            "AND gp.status != :trash")
                    .setParameter("environmentId", group.getEnvironment().getId())
                    .setParameter("groupId", group.getId())
                    .setParameter("principleReference.entityId", entity.getEntityId())
                    .setParameter("principleReference.entityType", entity.getObjectType().getName())
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                    .getResultList();
            if (groupPrinciples.size() > 0) {
                groupPrinciple = groupPrinciples.get(0);
            }
        }
        return groupPrinciple;
    }

    public List<GroupPrinciple> getGroupPrinciples(Group group, Pager pager) {
        // first count all objects
        long count = (Long) entityManager.createQuery(
                "SELECT count(gp) " +
                        "FROM GroupPrinciple gp " +
                        "WHERE gp.group.id = :groupId " +
                        "AND gp.status != :trash")
                .setParameter("groupId", group.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<GroupPrinciple> groupPrinciples = entityManager.createQuery(
                "SELECT gp " +
                        "FROM GroupPrinciple gp " +
                        "WHERE gp.group.id = :groupId " +
                        "AND gp.status != :trash")
                .setParameter("groupId", group.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(groupPrinciples.size());
        // all done, return results
        return groupPrinciples;
    }

    @SuppressWarnings(value = "unchecked")
    public List<GroupPrinciple> getGroupPrinciplesForPrinciple(IAMEEEntityReference principle, Pager pager) {
        // first count all objects
        long count = (Long) entityManager.createQuery(
                "SELECT count(gp) " +
                        "FROM GroupPrinciple gp " +
                        "WHERE gp.principleReference.entityId = :entityId " +
                        "AND gp.principleReference.entityType = :entityType " +
                        "AND gp.status != :trash")
                .setParameter("entityId", principle.getEntityId())
                .setParameter("entityType", principle.getObjectType().getName())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
        // tell pager how many objects there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the objects for the current page
        List<GroupPrinciple> groupPrinciples = entityManager.createQuery(
                "SELECT gp " +
                        "FROM GroupPrinciple gp " +
                        "LEFT JOIN FETCH gp.group g " +
                        "WHERE gp.principleReference.entityId = :entityId " +
                        "AND gp.principleReference.entityType = :entityType " +
                        "AND gp.status != :trash " +
                        "ORDER BY g.name")
                .setParameter("entityId", principle.getEntityId())
                .setParameter("entityType", principle.getObjectType().getName())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(groupPrinciples.size());
        // all done, return results
        return groupPrinciples;
    }

    public Set<Group> getGroupsForPrinciple(AMEEEntity principle) {
        Set<Group> groups = new HashSet<Group>();
        for (GroupPrinciple groupPrinciple : getGroupPrinciplesForPrinciple(principle)) {
            groups.add(groupPrinciple.getGroup());
        }
        return groups;
    }

    @SuppressWarnings(value = "unchecked")
    public List<GroupPrinciple> getGroupPrinciplesForPrinciple(IAMEEEntityReference principle) {
        List<GroupPrinciple> groupPrinciples = entityManager.createQuery(
                "SELECT gp " +
                        "FROM GroupPrinciple gp " +
                        "LEFT JOIN FETCH gp.group g " +
                        "WHERE gp.principleReference.entityId = :entityId " +
                        "AND gp.principleReference.entityType = :entityType " +
                        "AND gp.status != :trash " +
                        "ORDER BY g.name")
                .setParameter("entityId", principle.getEntityId())
                .setParameter("entityType", principle.getObjectType().getName())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        // all done, return results
        return groupPrinciples;
    }

    public List<GroupPrinciple> getGroupPrinciples(Environment environment) {
        List<GroupPrinciple> groupPrinciples = entityManager.createQuery(
                "SELECT gp " +
                        "FROM GroupPrinciple gp " +
                        "WHERE gp.environment.id = :environmentId " +
                        "AND gp.status != :trash")
                .setParameter("environmentId", environment.getId())
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return groupPrinciples;
    }

    public void save(GroupPrinciple groupPrinciple) {
        entityManager.persist(groupPrinciple);
    }

    public void remove(GroupPrinciple groupPrinciple) {
        beforeGroupPrincipleDelete(groupPrinciple);
        groupPrinciple.setStatus(AMEEStatus.TRASH);
    }
}