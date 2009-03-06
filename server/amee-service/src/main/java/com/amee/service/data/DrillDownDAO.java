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
package com.amee.service.data;

import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.sheet.Choice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.*;

/**
 * Uses native SQL to perform a drill down into DataItem values.
 * <p/>
 * Note: I was unable to use the JPA EntityManger for this SQL so have used
 * the native Hibernate Session instead. This seems to be due to
 * the ITEM_VALUE.VALUE column being MEDIUMTEXT. This is the error message
 * I was getting: "No Dialect mapping for JDBC type: -1". After lots of
 * searching the options seem to be: 1) create a custom hibernate SQL dialect
 * that can handle MEDIUMTEXT to String conversion, 2) change the type of the
 * VALUE column, 3) use SQLQuery.addScalar to get hibernate to understand
 * the VALUE column. I've opted for option 3 here.
 */
@Service
class DrillDownDAO implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    public DrillDownDAO() {
        super();
    }

    public List<Choice> getDataItemValueChoices(
            DataCategory dataCategory,
            List<Choice> selections,
            String name) {

        ItemDefinition itemDefinition;
        ItemValueDefinition itemValueDefinition;
        List<Choice> choices;
        Collection<Long> dataItemIds;

        // check arguments
        if ((dataCategory == null) ||
                (dataCategory.getItemDefinition() == null) ||
                (selections == null) ||
                (name == null)) {
            throw new IllegalArgumentException("A required argument is missing.");
        }

        // get choices
        choices = new ArrayList<Choice>();
        itemDefinition = dataCategory.getItemDefinition();
        itemValueDefinition = itemDefinition.getItemValueDefinition(name);
        if (itemValueDefinition != null) {
            if (!selections.isEmpty()) {
                // get choices based on selections
                dataItemIds = getDataItemIdsBySelections(dataCategory, selections);
                if (!dataItemIds.isEmpty()) {
                    for (String value : getValues(
                            itemValueDefinition.getId(),
                            getDataItemIdsBySelections(dataCategory, selections))) {
                        choices.add(new Choice(value));
                    }
                }
            } else {
                // get choices for top level (no selections)
                for (String value : getDataItemValues(
                        dataCategory.getId(),
                        itemDefinition.getId(),
                        itemValueDefinition.getId())) {
                    choices.add(new Choice(value));
                }
            }
        } else {
            throw new IllegalArgumentException("ItemValueDefinition not found: " + name);
        }

        return choices;
    }

    public List<Choice> getDataItemUIDChoices(
            DataCategory dataCategory,
            List<Choice> selections) {

        ItemDefinition itemDefinition;
        List<Choice> choices;
        Collection<Long> dataItemIds;

        // check arguments
        if ((dataCategory == null) ||
                (dataCategory.getItemDefinition() == null) ||
                (selections == null)) {
            throw new IllegalArgumentException("A required argument is missing.");
        }

        // get choices
        choices = new ArrayList<Choice>();
        itemDefinition = dataCategory.getItemDefinition();
        if (!selections.isEmpty()) {
            // get choices based on selections
            dataItemIds = getDataItemIdsBySelections(dataCategory, selections);
            if (!dataItemIds.isEmpty()) {
                for (String value : this.getDataItemUIDs(dataItemIds)) {
                    choices.add(new Choice(value));
                }
            }
        } else {
            // get choices for top level (no selections)
            for (String value : getDataItemUIDs(
                    dataCategory.getId(),
                    itemDefinition.getId())) {
                choices.add(new Choice(value));
            }
        }

        return choices;
    }

    public Collection<Long> getDataItemIdsBySelections(
            DataCategory dataCategory,
            List<Choice> selections) {

        Collection<Long> dataItemIds;
        Set<Long> allDataItemIds;
        Collection<Collection<Long>> collections;
        ItemValueDefinition itemValueDefinition;

        // check arguments
        if ((dataCategory == null) ||
                (dataCategory.getItemDefinition() == null) ||
                (selections == null)) {
            throw new IllegalArgumentException("Must have a DataCategory with an ItemDefinition and some selections.");
        }

        // iterate over selections and fetch DataItem IDs
        allDataItemIds = new HashSet<Long>();
        collections = new ArrayList<Collection<Long>>();
        for (Choice selection : selections) {
            itemValueDefinition = dataCategory.getItemDefinition().getItemValueDefinition(selection.getName());
            if (itemValueDefinition != null) {
                dataItemIds = getDataItemIdsByValue(
                        dataCategory.getId(),
                        dataCategory.getItemDefinition().getId(),
                        itemValueDefinition.getId(),
                        selection.getValue());
                collections.add(dataItemIds);
                allDataItemIds.addAll(dataItemIds);
            } else {
                throw new IllegalArgumentException("Could not locate ItemValueDefinition: " + selection.getName());
            }
        }

        // reduce all to intersection
        for (Collection<Long> c : collections) {
            allDataItemIds.retainAll(c);
        }

        return allDataItemIds;
    }

    public List<String> getValues(
            Long itemValueDefinitionId,
            Collection<Long> ids) {

        StringBuilder sql;
        SQLQuery query;

        // check arguments
        if ((itemValueDefinitionId == null) || (ids == null) || (ids.isEmpty())) {
            throw new IllegalArgumentException("A required argument is missing.");
        }

        // create SQL
        sql = new StringBuilder();
        sql.append("SELECT DISTINCT VALUE ");
        sql.append("FROM ITEM_VALUE ");
        sql.append("WHERE ITEM_VALUE_DEFINITION_ID = :itemValueDefinitionId ");
        sql.append("AND ITEM_ID IN (:ids) ");
        sql.append("ORDER BY LCASE(VALUE) ASC");

        // create query
        Session session = (Session) entityManager.getDelegate();
        query = session.createSQLQuery(sql.toString());
        query.addScalar("VALUE", Hibernate.STRING);

        // set parameters
        query.setLong("itemValueDefinitionId", itemValueDefinitionId);
        query.setParameterList("ids", ids, Hibernate.LONG);

        // execute SQL
        try {
            List<String> results = query.list();
            log.debug("getDataItemValues() results: " + results.size());
            return results;
        } catch (Exception e) {
            log.error("Caught Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<String> getDataItemUIDs(Collection<Long> ids) {

        StringBuilder sql;
        SQLQuery query;

        // check arguments
        if ((ids == null) || (ids.isEmpty())) {
            throw new IllegalArgumentException("A required argument is missing.");
        }

        // create SQL
        sql = new StringBuilder();
        sql.append("SELECT UID ");
        sql.append("FROM ITEM ");
        sql.append("WHERE ID IN (:ids)");

        // create query
        Session session = (Session) entityManager.getDelegate();
        query = session.createSQLQuery(sql.toString());
        query.addScalar("UID", Hibernate.STRING);

        // set parameters
        query.setParameterList("ids", ids, Hibernate.LONG);

        // execute SQL
        try {
            List<String> results = query.list();
            log.debug("getDataItemUIDs() results: " + results.size());
            return results;
        } catch (Exception e) {
            log.error("getDataItemUIDs() Caught Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<String> getDataItemUIDs(
            Long dataCategoryId,
            Long itemDefinitionId) {

        StringBuilder sql;
        SQLQuery query;

        // check arguments
        if ((dataCategoryId == null) || (itemDefinitionId == null)) {
            throw new IllegalArgumentException("A required argument is missing.");
        }

        // create SQL
        sql = new StringBuilder();
        sql.append("SELECT UID ");
        sql.append("FROM ITEM ");
        sql.append("WHERE TYPE = 'DI' ");
        sql.append("AND DATA_CATEGORY_ID = :dataCategoryId ");
        sql.append("AND ITEM_DEFINITION_ID = :itemDefinitionId ");

        // create query
        Session session = (Session) entityManager.getDelegate();
        query = session.createSQLQuery(sql.toString());
        query.addScalar("UID", Hibernate.STRING);

        // set parameters
        query.setLong("dataCategoryId", dataCategoryId);
        query.setLong("itemDefinitionId", itemDefinitionId);

        // execute SQL
        try {
            List<String> results = query.list();
            log.debug("getDataItemUIDs() results: " + results.size());
            return results;
        } catch (Exception e) {
            log.error("getDataItemUIDs() Caught Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<String> getDataItemValues(
            Long dataCategoryId,
            Long itemDefinitionId,
            Long itemValueDefinitionId) {

        StringBuilder sql;
        SQLQuery query;

        // check arguments
        if ((dataCategoryId == null) ||
                (itemDefinitionId == null) ||
                (itemValueDefinitionId == null)) {
            throw new IllegalArgumentException("A required argument is missing.");
        }

        // create SQL
        sql = new StringBuilder();
        sql.append("SELECT DISTINCT iv.VALUE VALUE ");
        sql.append("FROM ITEM_VALUE iv, ITEM i ");
        sql.append("WHERE iv.ITEM_ID = i.ID ");
        sql.append("AND i.TYPE = 'DI' ");
        sql.append("AND i.DATA_CATEGORY_ID = :dataCategoryId ");
        sql.append("AND i.ITEM_DEFINITION_ID = :itemDefinitionId ");
        sql.append("AND iv.ITEM_VALUE_DEFINITION_ID = :itemValueDefinitionId ");
        sql.append("ORDER BY LCASE(VALUE) ASC");

        // create query
        Session session = (Session) entityManager.getDelegate();
        query = session.createSQLQuery(sql.toString());
        query.addScalar("VALUE", Hibernate.STRING);

        // set parameters
        query.setLong("dataCategoryId", dataCategoryId);
        query.setLong("itemDefinitionId", itemDefinitionId);
        query.setLong("itemValueDefinitionId", itemValueDefinitionId);

        // execute SQL
        try {
            List<String> results = query.list();
            log.debug("getDataItemValues() results: " + results.size());
            return results;
        } catch (Exception e) {
            log.error("getDataItemValues() Caught Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Collection<Long> getDataItemIdsByValue(
            Long dataCategoryId,
            Long itemDefinitionId,
            Long itemValueDefinitionId,
            String value) {

        StringBuilder sql;
        SQLQuery query;

        // create SQL
        sql = new StringBuilder();
        sql.append("SELECT DISTINCT i.ID ID ");
        sql.append("FROM ITEM i, ITEM_VALUE iv ");
        sql.append("WHERE i.ID = iv.ITEM_ID ");
        sql.append("AND i.TYPE = 'DI' ");
        sql.append("AND i.DATA_CATEGORY_ID = :dataCategoryId ");
        sql.append("AND i.ITEM_DEFINITION_ID = :itemDefinitionId ");
        sql.append("AND iv.ITEM_VALUE_DEFINITION_ID = :itemValueDefinitionId ");
        sql.append("AND iv.value = :value");

        // create query
        Session session = (Session) entityManager.getDelegate();
        query = session.createSQLQuery(sql.toString());
        query.addScalar("ID", Hibernate.LONG);

        // set parameters
        query.setLong("dataCategoryId", dataCategoryId);
        query.setLong("itemDefinitionId", itemDefinitionId);
        query.setLong("itemValueDefinitionId", itemValueDefinitionId);
        query.setString("value", value);

        // execute SQL
        try {
            List<Long> results = query.list();
            log.debug("getDataItemIdsByValue() results: " + results.size());
            return results;
        } catch (Exception e) {
            log.error("Caught Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}