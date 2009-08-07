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
package com.amee.domain.data;

import com.amee.core.APIUtils;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.InternalValue;
import com.amee.domain.StartEndDate;
import com.amee.domain.path.Pathable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.joda.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.*;

@Entity
@Inheritance
@Table(name = "ITEM")
@DiscriminatorColumn(name = "TYPE", length = 3)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class Item extends AMEEEnvironmentEntity implements Pathable {

    public final static int NAME_SIZE = 255;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_DEFINITION_ID")
    private ItemDefinition itemDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_CATEGORY_ID")
    private DataCategory dataCategory;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("startDate DESC")
    private List<ItemValue> itemValues = new ArrayList<ItemValue>();

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "START_DATE")
    @Index(name = "START_DATE_IND")
    protected Date startDate = Calendar.getInstance().getTime();

    @Column(name = "END_DATE")
    @Index(name = "END_DATE_IND")
    protected Date endDate;

    @Transient
    private ItemValueMap itemValuesMap;

    @Transient
    private Set<ItemValue> activeItemValues;

    @Transient
    private Date effectiveStartDate;

    @Transient
    private Date effectiveEndDate;

    public Item() {
        super();
    }

    public Item(DataCategory dataCategory, ItemDefinition itemDefinition) {
        super(dataCategory.getEnvironment());
        setDataCategory(dataCategory);
        setItemDefinition(itemDefinition);
    }

    public void addItemValue(ItemValue itemValue) {
        itemValues.add(itemValue);
        resetItemValueCollections();
    }

    public Set<ItemValueDefinition> getItemValueDefinitions() {
        Set<ItemValueDefinition> itemValueDefinitions = new HashSet<ItemValueDefinition>();
        for (ItemValue itemValue : getActiveItemValues()) {
            itemValueDefinitions.add(itemValue.getItemValueDefinition());
        }
        return itemValueDefinitions;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    public abstract JSONObject getJSONObject(boolean detailed) throws JSONException;

    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }

    public void setItemDefinition(ItemDefinition itemDefinition) {
        if (itemDefinition != null) {
            this.itemDefinition = itemDefinition;
        }
    }

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(DataCategory dataCategory) {
        if (dataCategory != null) {
            this.dataCategory = dataCategory;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayPath() {
        if (getPath().length() > 0) {
            return getPath();
        } else {
            return getUid();
        }
    }

    @Override
    public String getDisplayName() {
        if (getName().length() > 0) {
            return getName();
        } else {
            return getDisplayPath();
        }
    }

    /**
     * Get an unmodifiable List of {@link ItemValue}s owned by this Item.
     *
     * For an historical sequence of {@link ItemValue}s, only the active entry for each {@link ItemValueDefinition} 
     * for the prevailing datetime context is returned.
     *
     * @return - the List of {@link ItemValue}
     */
    public List<ItemValue> getItemValues() {
        return Collections.unmodifiableList(getItemValuesMap().getAll(getEffectiveStartDate()));
    }

    /**
     * Get an unmodifiable List of ALL {@link ItemValue}s owned by this Item for a particular {@link ItemValueDefinition}
     *
     * @param itemValuePath - the {@link ItemValueDefinition} path
     * @return - the List of {@link ItemValue}
     */
    public List<ItemValue> getAllItemValues(String itemValuePath) {
        return Collections.unmodifiableList(getItemValuesMap().getAll(itemValuePath));
    }

    /**
     * Get an unmodifiable List of all active (not deleted) {@link ItemValue}s owned by this Item.
     *
     * @return - the Set of {@link ItemValue}
     */
    private Set<ItemValue> getActiveItemValues() {
        if (activeItemValues == null) {
            activeItemValues = new HashSet<ItemValue>();
            for (ItemValue iv : itemValues) {
                if (!iv.isTrash()) {
                    activeItemValues.add(iv);
                }
            }
        }
        return Collections.unmodifiableSet(activeItemValues);
    }

    /**
     * Return an {@link ItemValueMap} of {@link ItemValue}s belonging to this Item.
     * The key is the value returned by {@link com.amee.domain.data.ItemValue#getDisplayPath()}.
     *
     * @return {@link ItemValueMap}
     */
    public ItemValueMap getItemValuesMap() {
        if (itemValuesMap == null) {
            itemValuesMap = new ItemValueMap();
            for (ItemValue itemValue : getActiveItemValues()) {
                itemValuesMap.put(itemValue.getDisplayPath(), itemValue);
            }
        }
        return itemValuesMap;
    }

    /**
     * Attempt to match an {@link ItemValue} belonging to this Item using some identifier. The identifer may be a path
     * or UID.
     *
     * @param identifier - a value to be compared to the path and then the uid of the {@link ItemValue}s belonging
     * to this Item.
     * @param startDate - the startDate to use in the {@link ItemValue} lookup
     * @return the matched {@link ItemValue} or NULL if no match is found.
     */
    public ItemValue getItemValue(String identifier, Date startDate) {
        ItemValue iv = getItemValuesMap().get(identifier, startDate);
        if (iv == null) {
            iv = getByUid(identifier);
        }
        return iv;
    }

     /**
     * Get an {@link ItemValue} belonging to this Item using some identifier and prevailing datetime context.
     *
     * @param identifier - a value to be compared to the path and then the uid of the {@link ItemValue}s belonging
     * to this Item.
     * @return the matched {@link ItemValue} or NULL if no match is found.
     */
    public ItemValue getItemValue(String identifier) {
        return getItemValue(identifier, getEffectiveStartDate());
    }

    /**
     * Get an {@link ItemValue} by UID
     *
     * @param uid - the {@link ItemValue} UID
     * @return the {@link ItemValue} if found or NULL
     */
    private ItemValue getByUid(final String uid) {
        return(ItemValue) CollectionUtils.find(getActiveItemValues(), new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                ItemValue iv = (ItemValue) o;
                return iv.getUid().equals(uid);
            }
        });
    }

    /**
     * Add the Item's {@link ItemValue} collection to the passed {@link com.amee.domain.InternalValue} collection.
     *
     * @param values - the {@link com.amee.domain.InternalValue} collection
     */
    @SuppressWarnings("unchecked")
    public void appendInternalValues(Map<ItemValueDefinition, InternalValue> values) {
        ItemValueMap itemValueMap = getItemValuesMap();
        for (Object path : itemValueMap.keySet()) {
            // Get all ItemValues with this ItemValueDefinition path.
            List<ItemValue> itemValues = getAllItemValues((String) path);
            if (itemValues.size() > 1 || itemValues.get(0).getItemValueDefinition().isForceTimeSeries() ) {
                appendTimeSeriesItemValue(values, itemValues);
            } else if (itemValues.size() == 1) {
                appendSingleValuedItemValue(values, itemValues.get(0));
            }
        }
    }

    // Add an ItemValue timeseries to the InternalValue collection.
    @SuppressWarnings("unchecked")
    private void appendTimeSeriesItemValue(Map<ItemValueDefinition, InternalValue> values, List<ItemValue> itemValues) {
        ItemValueDefinition ivd = itemValues.get(0).getItemValueDefinition();

        // Add all ItemValues with usable values
        List<ItemValue> usableSet = (List<ItemValue>) CollectionUtils.select(itemValues, new UsableValuePredicate());

        if (!usableSet.isEmpty()) {
            values.put(ivd, new InternalValue(usableSet, getEffectiveStartDate(), getEffectiveEndDate()));
        }
    }

    // Add a single-valued ItemValue to the InternalValue collection.
    private void appendSingleValuedItemValue(Map<ItemValueDefinition, InternalValue> values, ItemValue itemValue) {
        if (itemValue.isUsableValue()) {
            values.put(itemValue.getItemValueDefinition(), new InternalValue(itemValue));
        }
    }

    private void resetItemValueCollections() {
        itemValuesMap = null;
        activeItemValues = null;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    public StartEndDate getStartDate() {
        return new StartEndDate(startDate);
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public StartEndDate getEndDate() {
        if (endDate != null) {
            return new StartEndDate(endDate);
        } else {
            return null;
        }
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Duration getDuration() {
        if (endDate != null) {
            return new Duration(startDate.getTime(), endDate.getTime());
        } else {
            return null;
        }
    }

    /**
     * @return returns true if this Item supports CO2 amounts, otherwise false.
     */
    public boolean supportsCalculation() {
        return !getItemDefinition().getAlgorithms().isEmpty();
    }

    /**
     * Check whether a date is within the lifetime of an Item.
     *
     * @param date - the {@link Date} to check if within the lifetime as this Item.
     * @return true if the the passed date is greater or equal to the start date of this Item
     *  and less than the end date of this Item, otherwise false.
     */
    public boolean isWithinLifeTime(Date date) {
        return (date.equals(getStartDate()) || date.after(getStartDate())) &&
                (getEndDate() == null || date.before(getEndDate()));
    }

    /**
     * Set the effective start date for {@link ItemValue} look-ups.
     *
     *
     * @param effectiveStartDate - the effective start date for {@link ItemValue} look-ups. If NULL or
     * before {@link com.amee.domain.data.Item#getStartDate()} this value is ignored.
     */
    public void setEffectiveStartDate(Date effectiveStartDate) {
        if (effectiveStartDate == null || effectiveStartDate.before(getStartDate()))
            return;
        this.effectiveStartDate = effectiveStartDate;
    }

    /**
     * Get the effective start date for {@link ItemValue} look-ups.
     *
     * @return - the effective start date. If no date has been explicitly specified, then either
     * now or the Item startDate is returned, whichever is the later.
     */
    protected Date getEffectiveStartDate() {
        if (effectiveStartDate != null) {
            return effectiveStartDate;
        } else {
            Date now = new Date();
            return (getStartDate().before(now) ? now : getStartDate());
        }
    }

    /**
     * Set the effective end date for {@link ItemValue} look-ups.
     *
     * @param effectiveEndDate - the effective end date for {@link ItemValue} look-ups. If NULL or
     * after {@link com.amee.domain.data.Item#getEndDate()} (if set) this value is ignored.
     */
    public void setEffectiveEndDate(Date effectiveEndDate) {
        if (effectiveEndDate == null ||
                (getEndDate() != null && effectiveEndDate.after(getEndDate())))
            return;
        this.effectiveEndDate = effectiveEndDate;
    }

    /**
     * Get the effective end date for {@link ItemValue} look-ups.
     *
     * @return - the effective end date. If no date has been explicitly specified, then Date(Long.MAX_VALUE)
     * is returned
     */
    protected Date getEffectiveEndDate() {
        if (effectiveEndDate != null) {
            return effectiveEndDate;
        } else {
            return new Date(Long.MAX_VALUE);
        }
    }

    /**
     * Check if there exists amongst the current set of ItemValues, an entry with the given
     * itemValueDefinition and startDate.
     * @param itemValueDefinitionUid - a {@link ItemValueDefinition} UID
     * @param startDate - an {@link ItemValue} startDate
     * @return - true if the newItemValue is unique, otherwise false
     */
    public boolean isUnique(String itemValueDefinitionUid, StartEndDate startDate) {
        String uniqueId = itemValueDefinitionUid + startDate.getTime();
        for (ItemValue iv : getActiveItemValues()) {
            String checkId = iv.getItemValueDefinition().getUid() + iv.getStartDate().getTime();
            if (uniqueId.equals(checkId)) {
                return false;
            }
        }
        return true;
    }
}

/**
 * Basic Predicate testing {@link ItemValue} instances for usable values.
 * {@see ItemValue#isUsableValue()}
 */
class UsableValuePredicate implements Predicate {
    @Override
    public boolean evaluate(Object o) {
        return ((ItemValue) o).isUsableValue();
    }
}

/**
 * Predicate for obtaining the latest ItemValue in an historical sequence.
 */
class CurrentItemValuePredicate implements Predicate {

    private List<ItemValue> itemValues;

    public CurrentItemValuePredicate(List<ItemValue> itemValues) {
        this.itemValues = itemValues;
    }

    @Override
    public boolean evaluate(Object o) {
        ItemValue iv = (ItemValue) o;
        StartEndDate startDate = iv.getStartDate();
        String path = iv.getItemValueDefinition().getPath();
        for (ItemValue itemValue : itemValues) {
            if (startDate.before(itemValue.getStartDate()) &&
                    itemValue.getItemValueDefinition().getPath().equals(path)) {
                return false;
            }
        }
        return true;
    }
}