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
package gc.carbon.data;

import com.jellymold.kiwi.Environment;
import com.jellymold.sheet.Choice;
import com.jellymold.sheet.Choices;
import com.jellymold.utils.event.ObserveEventService;
import com.jellymold.utils.event.ObservedEvent;
import gc.carbon.domain.data.*;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.path.PathItemService;
import gc.carbon.data.DataSheetService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.ext.seam.SpringController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.integration.annotation.ServiceActivator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Clear caches after entity removal.
 * TODO: Any other cache operations to put here?
 */
@Service
class DataServiceDAO implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SpringController springController;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private PathItemService pathItemService;

    @Autowired(required = true)
    private ObserveEventService observeEventService;

    public DataServiceDAO() {
        super();
    }

    // Handle events

    @ServiceActivator(inputChannel="beforeEnvironmentDelete")
    public void beforeEnvironmentDelete(ObservedEvent oe) {
        log.debug("beforeEnvironmentDelete");
        // delete root DataCategories
        List<DataCategory> dataCategories = entityManager.createQuery(
                "FROM DataCategory dc " +
                        "WHERE dc.environment = :environment " +
                        "AND dc.dataCategory IS NULL")
                .setParameter("environment", ((Environment) oe.getPayload()))
                .getResultList();
        for (DataCategory dataCategory : dataCategories) {
            remove(dataCategory);
        }
    }

    @ServiceActivator(inputChannel="beforeItemDefinitionDelete")
    public void beforeItemDefinitionDelete(ObservedEvent oe) {
        ItemDefinition itemDefinition = (ItemDefinition) oe.getPayload();
        log.debug("beforeItemDefinitionDelete");
        observeEventService.raiseEvent("beforeDataItemsDelete", itemDefinition);
        // remove ItemValues for DataItems
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.item IN " +
                        "(SELECT di FROM DataItem di WHERE di.itemDefinition = :itemDefinition)")
                .setParameter("itemDefinition", itemDefinition)
                .executeUpdate();
        // remove DataItems
        entityManager.createQuery(
                "DELETE FROM DataItem di " +
                        "WHERE di.itemDefinition = :itemDefinition")
                .setParameter("itemDefinition", itemDefinition)
                .executeUpdate();
    }

    @ServiceActivator(inputChannel="beforeItemValueDefinitionDelete")
    public void beforeItemValueDefinitionDelete(ObservedEvent oe) {
        log.debug("beforeItemValueDefinitionDelete");
        // remove ItemValues (from DataItems and ProfileItems)
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.itemValueDefinition = :itemValueDefinition")
                .setParameter("itemValueDefinition", ((ItemValueDefinition) oe.getPayload()))
                .executeUpdate();
    }

    // DataCategories

    public DataCategory getDataCategory(Environment environment, String uid) {
        DataCategory dataCategory = null;
        List<DataCategory> dataCategories = entityManager.createQuery(
                "FROM DataCategory dc " +
                        "WHERE dc.uid = :uid " +
                        "AND dc.environment = :environment")
                .setParameter("uid", uid)
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        if (dataCategories.size() == 1) {
            log.debug("found DataCategory");
            dataCategory = dataCategories.get(0);
        } else {
            log.debug("DataCategory NOT found");
        }
        return dataCategory;
    }

    public DataCategory getDataCategory(DataCategory parentDataCategory, String uid) {
        DataCategory dataCategory = null;
        List<DataCategory> dataCategories = entityManager.createQuery(
                "SELECT DISTINCT dc " +
                        "FROM DataCategory dc " +
                        "WHERE dc.dataCategory = :parentDataCategory " +
                        "AND dc.uid = :uid")
                .setParameter("parentDataCategory", parentDataCategory)
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        if (dataCategories.size() == 1) {
            log.debug("found DataCategory");
            dataCategory = dataCategories.get(0);
        } else {
            log.debug("DataCategory NOT found");
        }
        return dataCategory;
    }

    public List<DataCategory> getDataCategories(Environment environment) {
        List<DataCategory> dataCategories = entityManager.createQuery(
                "FROM DataCategory " +
                        "WHERE environment = :environment")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        return dataCategories;
    }

    public void remove(DataCategory dataCategory) {
        log.debug("remove: " + dataCategory.getName());
        observeEventService.raiseEvent("beforeDataCategoryDelete", dataCategory);
        // remove ItemValues for DataItems
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.item IN " +
                        "(SELECT di FROM DataItem di WHERE di.dataCategory = :dataCategory)")
                .setParameter("dataCategory", dataCategory)
                .executeUpdate();
        // remove DataItems
        entityManager.createQuery(
                "DELETE FROM DataItem di " +
                        "WHERE di.dataCategory = :dataCategory")
                .setParameter("dataCategory", dataCategory)
                .executeUpdate();
        // remove child DataCategories
        List<DataCategory> dataCategories = entityManager.createQuery(
                "FROM DataCategory di " +
                        "WHERE di.dataCategory = :dataCategory")
                .setParameter("dataCategory", dataCategory)
                .getResultList();
        for (DataCategory child : dataCategories) {
            remove(child);
        }
        // remove this DataCategory
        entityManager.remove(dataCategory);
    }

    // ItemValues

    public ItemValue getItemValue(Item item, String uid) {
        ItemValue itemValue = null;
        List<ItemValue> itemValues;
        itemValues = entityManager.createQuery(
                "FROM ItemValue iv " +
                        "WHERE iv.uid = :uid " +
                        "AND iv.item = :item")
                .setParameter("uid", uid)
                .setParameter("item", item)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        if (itemValues.size() == 1) {
            log.debug("found ItemValue");
            itemValue = itemValues.get(0);
        } else {
            log.debug("ItemValue NOT found");
        }
        return itemValue;
    }

    // DataItems

    public Item getItem(Environment environment, String uid) {
        DataItem dataItem = null;
        List<DataItem> dataItems = entityManager.createQuery(
                "SELECT DISTINCT di " +
                        "FROM DataItem di " +
                        "LEFT JOIN FETCH di.itemValues " +
                        "WHERE di.uid = :uid " +
                        "AND di.environment = :environment")
                .setParameter("uid", uid)
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        if (dataItems.size() == 1) {
            log.debug("found DataItem");
            dataItem = dataItems.get(0);
            checkDataItem(dataItem);
        } else {
            log.debug("DataItem NOT found");
        }
        return dataItem;
    }

    public DataItem getDataItem(DataCategory dataCategory, String uid) {
        DataItem dataItem = null;
        List<DataItem> dataItems;
        dataItems = entityManager.createQuery(
                "SELECT DISTINCT di " +
                        "FROM DataItem di " +
                        "LEFT JOIN FETCH di.itemValues " +
                        "WHERE di.dataCategory = :dataCategory " +
                        "AND di.uid = :uid")
                .setParameter("dataCategory", dataCategory)
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        if (dataItems.size() == 1) {
            log.debug("found DataItem");
            dataItem = dataItems.get(0);
            checkDataItem(dataItem);
        } else {
            log.debug("DataItem NOT found");
        }
        return dataItem;
    }

    public DataItem getDataItem(Environment environment, String uid) {
        DataItem dataItem = null;
        List<DataItem> dataItems;
        dataItems = entityManager.createQuery(
                "SELECT DISTINCT di " +
                        "FROM DataItem di " +
                        "LEFT JOIN FETCH di.itemValues " +
                        "WHERE di.environment.id = :environmentId " +
                        "AND di.uid = :uid")
                .setParameter("environmentId", environment.getId())
                .setParameter("uid", uid)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        if (dataItems.size() == 1) {
            log.debug("found DataItem");
            dataItem = dataItems.get(0);
            checkDataItem(dataItem);
        } else {
            log.debug("DataItem NOT found");
        }
        return dataItem;
    }

    // TODO: This uses up lots of memory - so what?!
    public List<DataItem> getDataItems(Environment environment) {
        List<DataItem> dataItems = entityManager.createQuery(
                "SELECT DISTINCT di " +
                        "FROM DataItem di " +
                        "LEFT JOIN FETCH di.itemValues " +
                        "WHERE di.environment = :environment")
                .setParameter("environment", environment)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        return dataItems;                                                     
    }

    public List<DataItem> getDataItems(DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate) {

        String q = "SELECT DISTINCT di " +
                "FROM DataItem di " +
                "LEFT JOIN FETCH di.itemValues " +
                "WHERE di.itemDefinition.id = :itemDefinitionId " +
                "AND di.dataCategory = :dataCategory AND " +
                ((endDate != null) ? "di.startDate <= :endDate AND (di.endDate >= :startDate OR di.endDate IS NULL" : "di.endDate > :startDate OR di.endDate IS NULL");

        if ((dataCategory != null) && (dataCategory.getItemDefinition() != null)) {
            Query query = entityManager.createQuery(q);
            query.setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId());
            query.setParameter("dataCategory", dataCategory);
            query.setParameter("startDate", startDate.toDate());
            if (endDate != null)
                query.setParameter("endDate", endDate.toDate());
            query.setHint("org.hibernate.cacheable", true);
            query.setHint("org.hibernate.cacheRegion", "query.dataService");
            return query.getResultList();
        } else {
            return null;
        }
    }

    public Choices getUserValueChoices(DataItem dataItem) {
        List<Choice> userValueChoices = new ArrayList<Choice>();
        for (ItemValueDefinition ivd : dataItem.getItemDefinition().getItemValueDefinitions()) {
            if (ivd.isFromProfile()) {
                // start default value with value from ItemValueDefinition
                String defaultValue = ivd.getValue();
                // next give DataItem a chance to set the default value, if appropriate
                if (ivd.isFromData()) {
                    Map<String, ItemValue> dataItemValues = dataItem.getItemValuesMap();
                    ItemValue dataItemValue = dataItemValues.get(ivd.getPath());
                    if ((dataItemValue != null) && (dataItemValue.getValue().length() > 0)) {
                        defaultValue = dataItemValue.getValue();
                    }
                }
                // create Choice
                userValueChoices.add(new Choice(ivd.getPath(), defaultValue));
            }
        }
        return new Choices("userValueChoices", userValueChoices);
    }

    public void remove(DataItem dataItem) {
        observeEventService.raiseEvent("beforeDataItemDelete", dataItem);
        entityManager.remove(dataItem);
    }

    public void checkDataItem(DataItem dataItem) {
        // find ItemValueDefinitions not currently implemented in this Item
        List<ItemValueDefinition> itemValueDefinitions = entityManager.createQuery(
                "FROM ItemValueDefinition ivd " +
                        "WHERE ivd NOT IN (" +
                        "   SELECT iv.itemValueDefinition " +
                        "   FROM ItemValue iv " +
                        "   WHERE iv.item = :dataItem) " +
                        "AND ivd.fromData = :fromData " +
                        "AND ivd.itemDefinition.id = :itemDefinitionId")
                .setParameter("dataItem", dataItem)
                .setParameter("itemDefinitionId", dataItem.getItemDefinition().getId())
                .setParameter("fromData", true)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.dataService")
                .getResultList();
        if (itemValueDefinitions.size() > 0) {
            // ensure transaction has been started
            springController.beginTransaction();
            // create missing ItemValues
            for (ItemValueDefinition ivd : itemValueDefinitions) {
                entityManager.persist(new ItemValue(ivd, dataItem, ""));
            }
            // clear caches
            pathItemService.removePathItemGroup(dataItem.getEnvironment());
            dataSheetService.removeSheet(dataItem.getDataCategory());
        }
    }
}