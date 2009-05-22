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
package com.amee.service.data;

import com.amee.domain.AMEEStatus;
import com.amee.domain.APIVersion;
import com.amee.domain.StartEndDate;
import com.amee.domain.data.*;
import com.amee.domain.environment.Environment;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.service.profile.ProfileServiceDAO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataServiceDAO implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.dataService";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProfileServiceDAO profileServiceDao;

    // Handle events

    public void beforeDataCategoryDelete(DataCategory dataCategory) {
        log.debug("beforeDataCategoryDelete");
        profileServiceDao.beforeDataCategoryDelete(dataCategory);
    }

    public void beforeDataItemDelete(DataItem dataItem) {
        log.debug("beforeDataItemDelete");
        // trash ItemValues for ProfileItems
        profileServiceDao.beforeDataItemDelete(dataItem);
        // trash ItemValues for DataItem
        entityManager.createQuery(
                "UPDATE ItemValue iv " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE iv.item.id = :profileItemId " +
                        "AND iv.status = :active")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("profileItemId", dataItem.getId())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    public void beforeEnvironmentDelete(Environment environment) {
        log.debug("beforeEnvironmentDelete");
        // trash root DataCategories
        List<DataCategory> dataCategories = entityManager.createQuery(
                "UPDATE DataCategory " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE environment.id = :environmentId " +
                        "AND dataCategory IS NULL " +
                        "AND status = :active")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("environmentId", environment.getId())
                .getResultList();
        for (DataCategory dataCategory : dataCategories) {
            remove(dataCategory);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void beforeItemDefinitionDelete(ItemDefinition itemDefinition) {
        log.debug("beforeItemDefinitionDelete");
        // remove ItemValues for Items (DataItems & ProfileItems)
        entityManager.createQuery(
                "UPDATE ItemValue " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE item.id IN " +
                        "(SELECT i.id FROM Item i WHERE i.itemDefinition.id = :itemDefinitionId) " +
                        "AND status = :active")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("itemDefinitionId", itemDefinition.getId())
                .executeUpdate();
        // trash Items (DataItems & ProfileItems)
        entityManager.createQuery(
                "UPDATE Item " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE itemDefinition.id = :itemDefinitionId " +
                        "AND status = :active")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("itemDefinitionId", itemDefinition.getId())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    public void beforeItemValueDefinitionDelete(ItemValueDefinition itemValueDefinition) {
        log.debug("beforeItemValueDefinitionDelete");
        // remove ItemValues (from DataItems and ProfileItems)
        entityManager.createQuery(
                "UPDATE ItemValue " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE itemValueDefinition.id = :itemValueDefinitionId " +
                        "AND status = :active")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("itemValueDefinitionId", itemValueDefinition.getId())
                .executeUpdate();
    }

    // DataCategories

    @SuppressWarnings(value = "unchecked")
    public DataCategory getDataCategoryByUid(String uid) {
        DataCategory dataCategory = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(DataCategory.class);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.add(Restrictions.eq("status", AMEEStatus.ACTIVE));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<DataCategory> dataCategories = criteria.list();
            if (dataCategories.size() == 1) {
                if (log.isTraceEnabled()) {
                    log.trace("getDataCategoryByUid() found: " + uid);
                }
                dataCategory = dataCategories.get(0);
            } else {
                log.debug("getDataCategoryByUid() NOT found: " + uid);
            }
        }
        return dataCategory;
    }

    @SuppressWarnings(value = "unchecked")
    public List<DataCategory> getDataCategories(Environment environment) {
        return (List<DataCategory>) entityManager.createQuery(
                "FROM DataCategory " +
                        "WHERE environment.id = :environmentId " +
                        "AND status = :active")
                .setParameter("environmentId", environment.getId())
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
    }

    public void persist(DataCategory dc) {
        entityManager.persist(dc);
    }

    @SuppressWarnings(value = "unchecked")
    public void remove(DataCategory dataCategory) {
        log.debug("remove: " + dataCategory.getName());
        beforeDataCategoryDelete(dataCategory);
        // trash ItemValues for DataItems
        Session session = (Session) entityManager.getDelegate();
        SQLQuery query = session.createSQLQuery(
                new StringBuilder()
                        .append("UPDATE ITEM_VALUE iv, ITEM i ")
                        .append("SET iv.STATUS = :active, ")
                        .append("iv.MODIFIED = current_timestamp() ")
                        .append("WHERE iv.ITEM_ID = i.ID ")
                        .append("AND i.TYPE = 'DI' ")
                        .append("AND i.DATA_CATEGORY_ID = :dataCategoryId").toString());
        query.setInteger("active", AMEEStatus.ACTIVE.ordinal());
        query.setLong("dataCategoryId", dataCategory.getId());
        query.addSynchronizedEntityClass(ItemValue.class);
        query.executeUpdate();
        // trash DataItems
        entityManager.createQuery(
                "UPDATE DataItem " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE dataCategory.id = :dataCategoryId " +
                        "AND status = :active")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("dataCategoryId", dataCategory.getId())
                .executeUpdate();
        // trash child DataCategories
        List<DataCategory> dataCategories = entityManager.createQuery(
                "FROM DataCategory di " +
                        "WHERE di.dataCategory.id = :dataCategoryId " +
                        "AND di.status = :active")
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("dataCategoryId", dataCategory.getId())
                .getResultList();
        for (DataCategory child : dataCategories) {
            remove(child);
        }
        // trash this DataCategory
        dataCategory.setStatus(AMEEStatus.TRASH);
    }

    // ItemValues

    @SuppressWarnings(value = "unchecked")
    public ItemValue getItemValueByUid(String uid) {
        ItemValue itemValue = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(ItemValue.class);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.add(Restrictions.eq("status", AMEEStatus.ACTIVE));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<ItemValue> itemValues = criteria.list();
            if (itemValues.size() == 1) {
                if (log.isTraceEnabled()) {
                    log.trace("getItemValueByUid() found: " + uid);
                }
                itemValue = itemValues.get(0);
            } else {
                log.debug("getItemValueByUid() NOT found: " + uid);
            }
        }
        return itemValue;
    }

    // DataItems

    /**
     * Returns the DatItem matching the specified UID.
     *
     * @param uid for the requested DataItem
     * @return the matching DataItem or null if not found
     */
    @SuppressWarnings(value = "unchecked")
    public DataItem getDataItemByUid(String uid) {
        DataItem dataItem = null;
        if (!StringUtils.isBlank(uid)) {
            // See http://www.hibernate.org/117.html#A12 for notes on DISTINCT_ROOT_ENTITY.
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(DataItem.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.add(Restrictions.naturalId().set("uid", uid.toUpperCase()));
            criteria.add(Restrictions.eq("status", AMEEStatus.ACTIVE));
            criteria.setFetchMode("itemValues", FetchMode.JOIN);
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<DataItem> dataItems = criteria.list();
            if (dataItems.size() == 1) {
                if (log.isTraceEnabled()) {
                    log.trace("getDataItemByUid() found: " + uid);
                }
                dataItem = dataItems.get(0);
            } else {
                log.debug("getDataItemByUid() NOT found: " + uid);
            }
        }
        return dataItem;
    }

    @SuppressWarnings(value = "unchecked")
    public DataItem getDataItemByPath(Environment environment, String path) {
        DataItem dataItem = null;
        if ((environment != null) && !StringUtils.isBlank(path)) {
            List<DataItem> dataItems = entityManager.createQuery(
                    "SELECT DISTINCT di " +
                            "FROM DataItem di " +
                            "LEFT JOIN FETCH di.itemValues " +
                            "WHERE di.path = :path " +
                            "AND di.environment.id = :environmentId " +
                            "AND di.status = :active")
                    .setParameter("path", path)
                    .setParameter("environmentId", environment.getId())
                    .setParameter("active", AMEEStatus.ACTIVE)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                    .getResultList();
            if (dataItems.size() == 1) {
                if (log.isTraceEnabled()) {
                    log.trace("getDataItemByPath() found: " + path);
                }
                dataItem = dataItems.get(0);
            } else {
                log.debug("getDataItemByPath() NOT found: " + path);
            }
        }
        return dataItem;
    }

    @SuppressWarnings(value = "unchecked")
    public List<DataItem> getDataItems(DataCategory dataCategory) {
        return (List<DataItem>) entityManager.createQuery(
                "SELECT DISTINCT di " +
                        "FROM DataItem di " +
                        "LEFT JOIN FETCH di.itemValues " +
                        "WHERE di.itemDefinition.id = :itemDefinitionId " +
                        "AND di.dataCategory.id = :dataCategoryId " +
                        "AND di.status = :active")
                .setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId())
                .setParameter("dataCategoryId", dataCategory.getId())
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<DataItem> getDataItems(DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate) {

        String q = "SELECT DISTINCT di " +
                "FROM DataItem di " +
                "LEFT JOIN FETCH di.itemValues " +
                "WHERE di.itemDefinition.id = :itemDefinitionId " +
                "AND di.dataCategory.id = :dataCategoryId " +
                "AND " + ((endDate != null) ? "di.startDate < :endDate AND (di.endDate > :startDate OR di.endDate IS NULL) " : "(di.endDate > :startDate OR di.endDate IS NULL) " +
                "AND di.status = :active");

        if ((dataCategory != null) && (dataCategory.getItemDefinition() != null)) {
            Query query = entityManager.createQuery(q);
            query.setParameter("itemDefinitionId", dataCategory.getItemDefinition().getId());
            query.setParameter("dataCategoryId", dataCategory.getId());
            query.setParameter("startDate", startDate.toDate());
            if (endDate != null) {
                query.setParameter("endDate", endDate.toDate());
            }
            query.setParameter("active", AMEEStatus.ACTIVE);
            query.setHint("org.hibernate.cacheable", true);
            query.setHint("org.hibernate.cacheRegion", CACHE_REGION);
            return query.getResultList();
        } else {
            return null;
        }
    }

    public void persist(DataItem dataItem) {
        entityManager.persist(dataItem);
    }

    public void remove(DataItem dataItem) {
        beforeDataItemDelete(dataItem);
        dataItem.setStatus(AMEEStatus.TRASH);
    }

    // Choices

    @SuppressWarnings(value = "unchecked")
    public Choices getUserValueChoices(DataItem dataItem, APIVersion apiVersion) {
        List<Choice> userValueChoices = new ArrayList<Choice>();
        for (ItemValueDefinition ivd : dataItem.getItemDefinition().getItemValueDefinitions()) {
            if (ivd.isFromProfile() && ivd.isValidInAPIVersion(apiVersion)) {
                // start default value with value from ItemValueDefinition
                String defaultValue = ivd.getValue();
                // next give DataItem a chance to set the default value, if appropriate
                if (ivd.isFromData()) {
                    ItemValueMap dataItemValues = dataItem.getItemValuesMap();
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
}