/**
 * This file is part of AMEE.
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
package com.amee.service.definition;

import com.amee.domain.Pager;
import com.amee.domain.ValueDefinition;
import com.amee.domain.algorithm.AbstractAlgorithm;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.algorithm.AlgorithmContext;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.environment.Environment;
import com.amee.domain.event.ObserveEventService;
import com.amee.domain.event.ObservedEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Service
public class DefinitionServiceDAO implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.environmentService";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = true)
    private ObserveEventService observeEventService;

    public DefinitionServiceDAO() {
        super();
    }

    // Handle events

    @ServiceActivator(inputChannel = "beforeEnvironmentDelete")
    public void beforeEnvironmentDelete(ObservedEvent oe) {
        log.debug("beforeEnvironmentDelete");
        // TODO: what?
    }

    // Algorithms & AlgorithmContexts

    @SuppressWarnings(value = "unchecked")
    public Algorithm getAlgorithmByUid(String uid) {
        Algorithm algorithm = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(Algorithm.class);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<Algorithm> algorithms = criteria.list();
            if (algorithms.size() == 1) {
                log.debug("getAlgorithmByUid() found: " + uid);
                algorithm = algorithms.get(0);
            } else {
                log.debug("getAlgorithmByUid() NOT found: " + uid);
            }
        }
        return algorithm;
    }

    public List<AlgorithmContext> getAlgorithmContexts(Environment environment) {
        List<AlgorithmContext> algorithmContexts =
                entityManager.createQuery(
                        "FROM AlgorithmContext ac " +
                                "WHERE ac.environment.id = :environmentId")
                        .setParameter("environmentId", environment.getId())
                        .setHint("org.hibernate.cacheable", true)
                        .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                        .getResultList();
        if (algorithmContexts.size() == 1) {
            log.debug("found AlgorithmContexts");
        } else {
            log.debug("AlgorithmContexts NOT found");
        }
        return algorithmContexts;
    }

    @SuppressWarnings(value = "unchecked")
    public AlgorithmContext getAlgorithmContextByUid(String uid) {
        AlgorithmContext algorithmContext = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(AlgorithmContext.class);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<AlgorithmContext> algorithmContexts = criteria.list();
            if (algorithmContexts.size() == 1) {
                log.debug("getAlgorithmContextByUid() found: " + uid);
                algorithmContext = algorithmContexts.get(0);
            } else {
                log.debug("getAlgorithmContextByUid() NOT found: " + uid);
            }
        }
        return algorithmContext;
    }

    public void remove(AbstractAlgorithm algorithm) {
        entityManager.remove(algorithm);
    }

    public void save(AbstractAlgorithm algorithm) {
        entityManager.persist(algorithm);
    }

    // ItemDefinition

    @SuppressWarnings(value = "unchecked")
    public ItemDefinition getItemDefinitionByUid(String uid) {
        ItemDefinition itemDefinition = null;
        if (!StringUtils.isBlank(uid)) {
            // See http://www.hibernate.org/117.html#A12 for notes on DISTINCT_ROOT_ENTITY.
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(ItemDefinition.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.setFetchMode("itemValueDefinitions", FetchMode.JOIN);
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<ItemDefinition> itemDefinitions = criteria.list();
            if (itemDefinitions.size() == 1) {
                log.debug("getItemDefinitionByUid() found: " + uid);
                itemDefinition = itemDefinitions.get(0);
            } else {
                log.debug("getItemDefinitionByUid() NOT found: " + uid);
            }
        }
        return itemDefinition;
    }

    public List<ItemDefinition> getItemDefinitions(Environment environment) {
        List<ItemDefinition> itemDefinitions = entityManager.createQuery(
                "SELECT DISTINCT id " +
                        "FROM ItemDefinition id " +
                        "LEFT JOIN FETCH id.itemValueDefinitions ivd " +
                        "WHERE id.environment.id = :environmentId " +
                        "ORDER BY id.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return itemDefinitions;
    }

    public List<ItemDefinition> getItemDefinitions(Environment environment, Pager pager) {
        // first count all entities
        long count = (Long) entityManager.createQuery(
                "SELECT count(id) " +
                        "FROM ItemDefinition id " +
                        "WHERE id.environment.id = :environmentId")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
        // tell pager how many entities there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the entities for the current page
        List<ItemDefinition> itemDefinitions = entityManager.createQuery(
                "SELECT id " +
                        "FROM ItemDefinition id " +
                        "WHERE id.environment.id = :environmentId " +
                        "ORDER BY id.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(itemDefinitions.size());
        return itemDefinitions;
    }

    public void save(ItemDefinition itemDefinition) {
        entityManager.persist(itemDefinition);
    }

    public void remove(ItemDefinition itemDefinition) {
        observeEventService.raiseEvent("beforeItemDefinitionDelete", itemDefinition);
        entityManager.remove(itemDefinition);
    }

    // ItemValueDefinitions

    @SuppressWarnings(value = "unchecked")
    public ItemValueDefinition getItemValueDefinitionByUid(String uid) {
        ItemValueDefinition itemValueDefinition = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(ItemValueDefinition.class);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<ItemValueDefinition> itemValueDefinitions = criteria.list();
            if (itemValueDefinitions.size() == 1) {
                log.debug("getItemValueDefinitionByUid() found: " + uid);
                itemValueDefinition = itemValueDefinitions.get(0);
            } else {
                log.debug("getItemValueDefinitionByUid() NOT found: " + uid);
            }
        }
        return itemValueDefinition;
    }

    public void remove(ItemValueDefinition itemValueDefinition) {
        observeEventService.raiseEvent("beforeItemValueDefinitionDelete", itemValueDefinition);
        entityManager.remove(itemValueDefinition);
    }

    // ValueDefinitions

    public List<ValueDefinition> getValueDefinitions(Environment environment) {
        return entityManager.createQuery(
                "FROM ValueDefinition vd " +
                        "WHERE vd.environment.id = :environmentId " +
                        "ORDER BY vd.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
    }

    public List<ValueDefinition> getValueDefinitions(Environment environment, Pager pager) {
        // first count all entities
        long count = (Long) entityManager.createQuery(
                "SELECT count(vd) " +
                        "FROM ValueDefinition vd " +
                        "WHERE vd.environment.id = :environmentId")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
        // tell pager how many entities there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the entities for the current page
        List<ValueDefinition> valueDefinitions = entityManager.createQuery(
                "SELECT vd " +
                        "FROM ValueDefinition vd " +
                        "WHERE vd.environment.id = :environmentId " +
                        "ORDER BY id.name")
                .setParameter("environmentId", environment.getId())
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(valueDefinitions.size());
        return valueDefinitions;
    }

    @SuppressWarnings(value = "unchecked")
    public ValueDefinition getValueDefinitionByUid(String uid) {
        ValueDefinition valueDefinition = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(ValueDefinition.class);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<ValueDefinition> valueDefinitions = criteria.list();
            if (valueDefinitions.size() == 1) {
                log.debug("getValueDefinitionByUid() found: " + uid);
                valueDefinition = valueDefinitions.get(0);
            } else {
                log.debug("getValueDefinitionByUid() NOT found: " + uid);
            }
        }
        return valueDefinition;
    }

    public void save(ValueDefinition valueDefinition) {
        entityManager.persist(valueDefinition);
    }

    public void remove(ValueDefinition valueDefinition) {
        observeEventService.raiseEvent("beforeValueDefinitionDelete", valueDefinition);
        // remove ItemValueDefinitions
        List<ItemValueDefinition> itemValueDefinitions = entityManager.createQuery(
                "SELECT DISTINCT ivd " +
                        "FROM ItemValueDefinition ivd " +
                        "WHERE ivd.valueDefinition.id = :valueDefinitionId")
                .setParameter("valueDefinitionId", valueDefinition.getId())
                .getResultList();
        for (ItemValueDefinition itemValueDefinition : itemValueDefinitions) {
            remove(itemValueDefinition);
        }
        // remove ValueDefinition
        entityManager.remove(valueDefinition);
    }
}