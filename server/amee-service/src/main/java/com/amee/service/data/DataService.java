/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.service.data;

import com.amee.domain.APIVersion;
import com.amee.domain.UidGen;
import com.amee.domain.data.*;
import com.amee.domain.environment.Environment;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.domain.sheet.Sheet;
import com.amee.service.BaseService;
import com.amee.service.path.PathItemService;
import com.amee.service.transaction.TransactionController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Primary service interface to Data Resources.
 */
@Service
public class DataService extends BaseService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private DataServiceDAO dao;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private DrillDownService drillDownService;

    // DataCategories

    public DataCategory getDataCategoryByUid(String uid) {
        DataCategory dataCategory = dao.getDataCategoryByUid(uid);
        if (dataCategory != null && !dataCategory.isTrash()) {
            return dataCategory;
        } else {
            return null;
        }
    }

    // TODO: Populating activeCategories seems redundant. Doesn't dao.getDataCategories only return actives?
    public List<DataCategory> getDataCategories(Environment environment) {
        List<DataCategory> activeCategories = new ArrayList<DataCategory>();
        for (DataCategory dataCategory : dao.getDataCategories(environment)) {
            if (dataCategory != null && !dataCategory.isTrash()) {
                activeCategories.add(dataCategory);
            }
        }
        return activeCategories;
    }

    public void persist(DataCategory dataCategory) {
        dao.persist(dataCategory);
    }

    public void remove(DataCategory dataCategory) {
        dao.remove(dataCategory);
        for (DataCategory alias : dataCategory.getAliases()) {
            dao.remove(alias);
        }
    }

    /**
     * Clears all caches related to the supplied DataCategory.
     *
     * @param dataCategory to clear caches for
     */
    public void clearCaches(DataCategory dataCategory) {
        log.debug("clearCaches()");
        drillDownService.clearDrillDownCache();
        pathItemService.removePathItemGroup(dataCategory.getEnvironment());
        dataSheetService.removeSheet(dataCategory);
    }

    // DataItems

    public DataItem getDataItem(Environment environment, String path) {
        DataItem dataItem = null;
        if (!StringUtils.isBlank(path)) {
            if (UidGen.isValid(path)) {
                dataItem = getDataItemByUid(environment, path);
            }
            if (dataItem == null) {
                dataItem = getDataItemByPath(environment, path);
            }
        }
        return dataItem;
    }

    private DataItem getDataItemByUid(Environment environment, String uid) {
        DataItem dataItem = dao.getDataItemByUid(uid);
        if (dataItem != null && !dataItem.isTrash()) {
            checkEnvironmentObject(environment, dataItem);
            checkDataItem(dataItem);
            return dataItem;
        } else {
            return null;
        }
    }

    private DataItem getDataItemByPath(Environment environment, String path) {
        DataItem dataItem = dao.getDataItemByPath(environment, path);
        if (dataItem != null && !dataItem.isTrash()) {
            checkEnvironmentObject(environment, dataItem);
            checkDataItem(dataItem);
            return dataItem;
        } else {
            return null;
        }
    }

    public List<DataItem> getDataItems(DataCategory dataCategory) {
        return checkDataItems(dao.getDataItems(dataCategory));
    }

    private List<DataItem> checkDataItems(List<DataItem> dataItems) {
        List<DataItem> activeDataItems = new ArrayList<DataItem>();
        for (DataItem dataItem : dataItems) {
            if (!dataItem.isTrash()) {
                checkDataItem(dataItem);
                activeDataItems.add(dataItem);
            }
        }
        return activeDataItems;
    }

    /**
     * Add to the {@link com.amee.domain.data.DataItem} any {@link com.amee.domain.data.ItemValue}s it is missing.
     * This will be the case on first persist (this method acting as a reification function), and between GETs if any
     * new {@link com.amee.domain.data.ItemValueDefinition}s have been added to the underlying
     * {@link com.amee.domain.data.ItemDefinition}.
     * <p/>
     * Any updates to the {@link com.amee.domain.data.DataItem} will be persisted to the database.
     *
     * @param dataItem - the DataItem to check
     */
    @SuppressWarnings(value = "unchecked")
    public void checkDataItem(DataItem dataItem) {
        if (dataItem == null) {
            return;
        }
        Set<ItemValueDefinition> existingItemValueDefinitions = dataItem.getItemValueDefinitions();
        Set<ItemValueDefinition> missingItemValueDefinitions = new HashSet<ItemValueDefinition>();

        // find ItemValueDefinitions not currently implemented in this Item
        for (ItemValueDefinition ivd : dataItem.getItemDefinition().getItemValueDefinitions()) {
            if (ivd.isFromData()) {
                if (!existingItemValueDefinitions.contains(ivd)) {
                    missingItemValueDefinitions.add(ivd);
                }
            }
        }

        // Do we need to add any ItemValueDefinitions?
        if (missingItemValueDefinitions.size() > 0) {

            // Ensure a transaction has been opened. The implementation of open-session-in-view we are using
            // does not open transactions for GETs. This method is called for certain GETs.
            transactionController.begin(true);

            // create missing ItemValues
            for (ItemValueDefinition ivd : missingItemValueDefinitions) {
                new ItemValue(ivd, dataItem, "");
            }

            // clear caches
            drillDownService.clearDrillDownCache();
            pathItemService.removePathItemGroup(dataItem.getEnvironment());
            dataSheetService.removeSheet(dataItem.getDataCategory());
        }
    }

    public void persist(DataItem dataItem) {
        dao.persist(dataItem);
        checkDataItem(dataItem);
    }

    public void remove(DataItem dataItem) {
        dao.remove(dataItem);
    }

    public void remove(ItemValue dataItemValue) {
        dao.remove(dataItemValue);
    }

    public void remove(LocaleName localeName) {
        dao.remove(localeName);
    }

    // Sheets & Choices

    public Sheet getSheet(DataBrowser browser) {
        return dataSheetService.getSheet(browser);
    }

    @SuppressWarnings(value = "unchecked")
    public Choices getUserValueChoices(DataItem di, APIVersion apiVersion) {
        List<Choice> userValueChoices = new ArrayList<Choice>();
        for (ItemValueDefinition ivd : di.getItemDefinition().getItemValueDefinitions()) {
            if (ivd.isFromProfile() && ivd.isValidInAPIVersion(apiVersion)) {
                // start default value with value from ItemValueDefinition
                String defaultValue = ivd.getValue();
                // next give DataItem a chance to set the default value, if appropriate
                if (ivd.isFromData()) {
                    ItemValue dataItemValue = di.getItemValue(ivd.getPath());
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
}
