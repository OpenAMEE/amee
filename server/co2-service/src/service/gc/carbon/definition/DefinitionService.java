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
package gc.carbon.definition;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.Pager;
import com.jellymold.utils.event.ObserveEventService;
import com.jellymold.utils.event.ObservedEvent;
import gc.carbon.data.DataService;
import gc.carbon.domain.ValueDefinition;
import gc.carbon.domain.data.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.integration.annotation.ServiceActivator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * TODO: Come up with more efficient way to delete Environment entities.
 */
@Service
public class DefinitionService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = true)
    private ObserveEventService observeEventService;

    public DefinitionService() {
        super();
    }

    // Handle events

    @ServiceActivator(inputChannel="beforeEnvironmentDelete")
    public void beforeEnvironmentDelete(ObservedEvent oe) {
        log.debug("beforeEnvironmentDelete");
        // TODO: what?
    }

    // Algorithms

    // TODO: Scope to something

    public Algorithm getAlgorithm(String uid) {
        Algorithm algorithm = null;
        List<Algorithm> algorithms = entityManager.createQuery(
                "FROM Algorithm a " +
                        "WHERE a.uid = :uid")
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        if (algorithms.size() == 1) {
            log.debug("found Algorithm");
            algorithm = algorithms.get(0);
        } else {
            log.debug("Algorithm NOT found");
        }
        return algorithm;
    }

    public void remove(AbstractAlgorithm algorithm) {
        entityManager.remove(algorithm);
    }


    public void save(AbstractAlgorithm algorithm) {
        entityManager.persist(algorithm);
    }


    // TODO: Scope to something

    public List<AlgorithmContext> getAlgorithmContexts(Environment environment) {
        List<AlgorithmContext> algorithmContexts =
                entityManager.createQuery("FROM AlgorithmContext ac " +
                        "WHERE ac.environment = :environment")
                        .setParameter("environment", environment)
                        .setHint("org.hibernate.cacheable", true)
                        .setHint("org.hibernate.cacheRegion", "query.environmentService")
                        .getResultList();
        if (algorithmContexts.size() == 1) {
            log.debug("found AlgorithmContexts");
        } else {
            log.debug("AlgorithmContexts NOT found");
        }
        return algorithmContexts;
    }

    public AlgorithmContext getAlgorithmContext(Environment environment, String algorithmContextUid) {
        AlgorithmContext algorithmContext = null;
        List<AlgorithmContext> algorithmContexts =
                entityManager.createQuery("FROM AlgorithmContext ac " +
                        "WHERE ac.environment = :environment "
                        + "AND ac.uid = :uid")
                        .setParameter("environment", environment)
                        .setParameter("uid", algorithmContextUid)
                        .setHint("org.hibernate.cacheable", true)
                        .setHint("org.hibernate.cacheRegion", "query.environmentService")
                        .getResultList();
        if (algorithmContexts.size() == 1) {
            log.debug("found AlgorithmContext");
            algorithmContext = algorithmContexts.get(0);
        } else {
            log.debug("AlgorithmContext NOT found");
        }
        return algorithmContext;
    }

    // ItemDefinition

    // TODO: this forces a separate load of itemValueDefinitions

    public List<ItemDefinition> getItemDefinitions(Set<String> categoryUids) {
        List<ItemDefinition> itemDefinitions = entityManager.createQuery(
                "SELECT DISTINCT id " +
                        "FROM ItemDefinition id " +
                        "LEFT JOIN FETCH id.itemValueDefinitions ivd " +
                        "LEFT JOIN id.dataCategories dc " +
                        "WHERE dc.uid IN (:categoryUids)")
                .setParameter("categoryUids", categoryUids)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        return itemDefinitions;
    }

    public ItemDefinition getItemDefinition(Environment environment, String uid) {
        ItemDefinition itemDefinition = null;
        List<ItemDefinition> itemDefinitions = entityManager.createQuery(
                "SELECT DISTINCT id " +
                        "FROM ItemDefinition id " +
                        "LEFT JOIN FETCH id.itemValueDefinitions ivd " +
                        "WHERE id.environment = :environment AND " +
                        "id.uid = :uid")
                .setParameter("environment", environment)
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        if (itemDefinitions.size() == 1) {
            log.debug("found ItemDefinition");
            itemDefinition = itemDefinitions.get(0);
        } else {
            log.debug("ItemDefinition NOT found");
        }
        return itemDefinition;
    }

    public List<ItemDefinition> getItemDefinitions(Environment environment) {
        List<ItemDefinition> itemDefinitions = entityManager.createQuery(
                "SELECT DISTINCT id " +
                        "FROM ItemDefinition id " +
                        "LEFT JOIN FETCH id.itemValueDefinitions ivd " +
                        "WHERE id.environment = :environment " +
                        "ORDER BY id.name")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        return itemDefinitions;
    }

    public List<ItemDefinition> getItemDefinitions(Environment environment, Pager pager) {
        // first count all entities
        long count = (Long) entityManager.createQuery(
                "SELECT count(id) " +
                        "FROM ItemDefinition id " +
                        "WHERE id.environment = :environment")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getSingleResult();
        // tell pager how many entities there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the entities for the current page
        List<ItemDefinition> itemDefinitions = entityManager.createQuery(
                "SELECT id " +
                        "FROM ItemDefinition id " +
                        "WHERE id.environment = :environment " +
                        "ORDER BY id.name")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
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

    public ItemValueDefinition getItemValueDefinition(ItemDefinition itemDefinition, String uid) {
        ItemValueDefinition itemValueDefinition = null;
        List<ItemValueDefinition> itemValueDefinitions = entityManager.createQuery(
                "FROM ItemValueDefinition ivd " +
                        "WHERE ivd.uid = :uid " +
                        "AND ivd.itemDefinition = :itemDefinition")
                .setParameter("uid", uid)
                .setParameter("itemDefinition", itemDefinition)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        if (itemValueDefinitions.size() == 1) {
            log.debug("found ItemValueDefinition");
            itemValueDefinition = itemValueDefinitions.get(0);
        } else {
            log.debug("ItemValueDefinition NOT found");
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
                        "WHERE vd.environment = :environment " +
                        "ORDER BY vd.name")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
    }

    public List<ValueDefinition> getValueDefinitions(Environment environment, Pager pager) {
        // first count all entities
        long count = (Long) entityManager.createQuery(
                "SELECT count(vd) " +
                        "FROM ValueDefinition vd " +
                        "WHERE vd.environment = :environment")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getSingleResult();
        // tell pager how many entities there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the entities for the current page
        List<ValueDefinition> valueDefinitions = entityManager.createQuery(
                "SELECT vd " +
                        "FROM ValueDefinition vd " +
                        "WHERE vd.environment = :environment " +
                        "ORDER BY id.name")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(valueDefinitions.size());
        return valueDefinitions;
    }

    public ValueDefinition getValueDefinition(Environment environment, String uid) {
        ValueDefinition valueDefinition = null;
        List<ValueDefinition> valueDefinitions = entityManager.createQuery(
                "FROM ValueDefinition vd " +
                        "WHERE vd.uid = :uid " +
                        "AND vd.environment = :environment")
                .setParameter("uid", uid)
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.environmentService")
                .getResultList();
        if (valueDefinitions.size() == 1) {
            log.debug("found ValueDefinition");
            valueDefinition = valueDefinitions.get(0);
        } else {
            log.debug("ValueDefinition NOT found");
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
                        "WHERE ivd.valueDefinition = :valueDefinition")
                .setParameter("valueDefinition", valueDefinition)
                .getResultList();
        for (ItemValueDefinition itemValueDefinition : itemValueDefinitions) {
            remove(itemValueDefinition);
        }
        // remove ValueDefinition
        entityManager.remove(valueDefinition);
    }
}