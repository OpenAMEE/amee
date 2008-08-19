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
import gc.carbon.path.PathItemService;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.restlet.ext.seam.SeamController;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Clear caches after entity removal.
 * TODO: Any other cache operations to put here?
 */
@Name("dataService")
@Scope(ScopeType.EVENT)
public class DataService implements Serializable {

    private final static Logger log = Logger.getLogger(DataService.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private DataSheetService dataSheetService;

    @In(create = true)
    private PathItemService pathItemService;

    public DataService() {
        super();
    }

    // Handle events

    @Observer("beforeEnvironmentDelete")
    public void beforeEnvironmentDelete(Environment environment) {
        log.debug("beforeEnvironmentDelete");
        // delete root DataCategories
        List<DataCategory> dataCategories = entityManager.createQuery(
                "FROM DataCategory dc " +
                        "WHERE dc.environment = :environment " +
                        "AND dc.dataCategory IS NULL")
                .setParameter("environment", environment)
                .getResultList();
        for (DataCategory dataCategory : dataCategories) {
            remove(dataCategory);
        }
    }

    @Observer("beforeItemDefinitionDelete")
    public void beforeItemDefinitionDelete(ItemDefinition itemDefinition) {
        log.debug("beforeItemDefinitionDelete");
        Events.instance().raiseEvent("beforeDataItemsDelete", itemDefinition);
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
                        "WHERE di.itemDefinition = :itemDefinition)")
                .setParameter("itemDefinition", itemDefinition)
                .executeUpdate();
    }

    @Observer("beforeItemValueDefinitionDelete")
    public void beforeItemValueDefinitionDelete(ItemValueDefinition itemValueDefinition) {
        log.debug("beforeItemValueDefinitionDelete");
        // remove ItemValues (from DataItems and ProfileItems)
        entityManager.createQuery(
                "DELETE FROM ItemValue iv " +
                        "WHERE iv.itemValueDefinition = :itemValueDefinition")
                .setParameter("itemValueDefinition", itemValueDefinition)
                .executeUpdate();
    }

    // DataCategories

    /**
     * Get DataCategory for given uid. We don't fetch all items as there could be thousands.
     *
     * @param uid
     * @return DataCategory
     */
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
        Events.instance().raiseEvent("beforeDataCategoryDelete", dataCategory);
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
                        "WHERE di.dataCategory = :dataCategory)")
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

    public List<DataItem> getDataItems(DataCategory dataCategory) {
        if ((dataCategory != null) && (dataCategory.getItemDefinition() != null)) {
            List<DataItem> dataItems = entityManager.createQuery(
                    "SELECT DISTINCT di " +
                            "FROM DataItem di " +
                            "LEFT JOIN FETCH di.itemValues " +
                            "WHERE di.itemDefinition.id = :itemDefinitionId " +
                            "AND di.dataCategory = :dataCategory")
                    .setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId())
                    .setParameter("dataCategory", dataCategory)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.dataService")
                    .getResultList();
            return dataItems;
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
        Events.instance().raiseEvent("beforeDataItemDelete", dataItem);
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
            SeamController.getInstance().beginTransaction();
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