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
    private List<ItemValue> activeItemValues;

    @Transient
    private Date currentDate;

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
        for (ItemValue itemValue : itemValues) {
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

    /**
     * Get an unmodifiable List of {@link ItemValue}s owned by this Item.
     * For an historical sequence of {@link ItemValue}s, only the active entry on the current date in that sequence is returned.
     *
     * @return - the List of {@link ItemValue}
     */
    public List<ItemValue> getItemValues() {
        return getItemValues(getCurrentDate());
    }

    /**
     * Get an unmodifiable List of {@link ItemValue}s owned by this Item.
     * For an historical sequence of {@link ItemValue}s, the active entry at the given start date in that sequence is returned.
     *
     * @param startDate - the start date for active {@link ItemValue}s
     * @return - the List of {@link ItemValue}
     */
    public List<ItemValue> getItemValues(Date startDate) {
        return Collections.unmodifiableList(getItemValuesMap().get(startDate));
    }

    /**
     * Get an unmodifiable List of all active {@link ItemValue}s owned by this Item.
     *
     * @return - the List of {@link ItemValue}
     */
    public List<ItemValue> getActiveItemValues() {
        if (activeItemValues == null) {
            activeItemValues = new ArrayList<ItemValue>();
            for (ItemValue iv : itemValues) {
                if (iv.isActive()) {
                    activeItemValues.add(iv);
                }
            }
        }
        return Collections.unmodifiableList(activeItemValues);
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
     * Add the collection of {@link ItemValue} belonging to this Item to the passed visitor collection of
     * {@link com.amee.domain.InternalValue}.
     *
     * @param values - the visitor collection of {@link com.amee.domain.InternalValue} to which to add this Item's values.
     */
    @SuppressWarnings("unchecked")
    public void appendInternalValues(Map<ItemValueDefinition, InternalValue> values) {
        ItemValueMap itemValueMap = getItemValuesMap();
        for (Object path : itemValueMap.keySet()) {
            Set<ItemValue> itemValues = itemValueMap.getAll((String) path);
            if (itemValues.size() == 1) {
                ItemValue iv = itemValueMap.get((String) path);
                if (iv.isUsableValue()) {
                    values.put(iv.getItemValueDefinition(), new InternalValue(iv));
                }
            } else if (itemValues.size() > 1) {
                ItemValueDefinition ivd = itemValueMap.get((String) path).getItemValueDefinition();
                // Add all ItemValues with usable values
                List<ItemValue> usableSet = (List<ItemValue>) CollectionUtils.select(itemValues, new UsableValuePredicate());
                values.put(ivd, new InternalValue(usableSet));
            }
        }
    }

    /**
     * Attempt to match an {@link ItemValue} belonging to this Item using some identifier.
     *
     * @param identifier - a value to be compared to the path and then the uid of the {@link ItemValue}s belonging
     * to this Item.
     * @param startDate
     * @return the matched {@link ItemValue} or NULL if no match is found.
     */
    public ItemValue matchItemValue(String identifier, Date startDate) {
        ItemValue iv = getItemValuesMap().get(identifier, startDate);
        if (iv == null) {
            iv = getByUid(identifier);
        }
        return iv;
    }

    /**
     * Attempt to match an {@link ItemValue} belonging to this Item using some identifier.
     *
     * @param identifier - a value to be compared to the path and then the uid of the {@link ItemValue}s belonging
     * to this Item.
     * @return the matched {@link ItemValue} or NULL if no match is found.
     */
    public ItemValue matchItemValue(String identifier) {
        return matchItemValue(identifier, getCurrentDate());
    }

    /**
     * Get an {@link ItemValue} by UID
     *
     * @param uid
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

    private void resetItemValueCollections() {
        itemValuesMap = null;
        activeItemValues = null;
    }

    public String getName() {
        return name;
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

    public String getDisplayPath() {
        if (getPath().length() > 0) {
            return getPath();
        } else {
            return getUid();
        }
    }

    public String getDisplayName() {
        if (getName().length() > 0) {
            return getName();
        } else {
            return getDisplayPath();
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
     * Set the applicable date when dealing with historical sequences.
     *
     * @param currentDate
     */
    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    protected Date getCurrentDate() {
        if (currentDate != null) {
            return currentDate;
        } else {
            return new Date();
        }
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