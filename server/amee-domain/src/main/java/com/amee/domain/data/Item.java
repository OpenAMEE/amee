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
    private List<ItemValue> currentItemValues;

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
        for (ItemValue itemValue : getItemValues()) {
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
     * Get an unmodifiable List of {@link ItemValue} owned by this Item. For an historical sequence of {@link ItemValue}, only the
     * latest in that sequence is returned.
     *
     * @return - the List of {@link ItemValue}
     */
    public List<ItemValue> getItemValues() {
        return Collections.unmodifiableList(getCurrentItemValues());
    }

    // Filter-out any ItemValue instances within an historical sequence of ItemValues that are not the final entry in
    // the given datetime range.
    @SuppressWarnings("unchecked")
    private List<ItemValue> getCurrentItemValues() {
        if (currentItemValues == null) {
            final List<ItemValue> activeItemValues = getActiveItemValues();
            currentItemValues = (List) CollectionUtils.select(activeItemValues, new Predicate() {
                public boolean evaluate(Object o) {
                    ItemValue iv = (ItemValue) o;
                    StartEndDate startDate = iv.getStartDate();
                    String path = iv.getItemValueDefinition().getPath();
                    for (ItemValue itemValue : activeItemValues) {
                        if (startDate.before(itemValue.getStartDate()) &&
                                itemValue.getItemValueDefinition().getPath().equals(path)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
        }
        return Collections.unmodifiableList(currentItemValues);
    }

    private List<ItemValue> getActiveItemValues() {
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
                Set<ItemValue> usableSet = (Set<ItemValue>) CollectionUtils.select(itemValues, new UsableValuePredicate());
                values.put(ivd, new InternalValue(usableSet));
            }
        }
    }

    public ItemValueMap getItemValuesMap() {
        if (itemValuesMap == null) {
            itemValuesMap = new ItemValueMap();
            for (ItemValue itemValue : getItemValues()) {
                itemValuesMap.put(itemValue.getDisplayPath(), itemValue);
            }
        }
        return itemValuesMap;
    }

    private void resetItemValueCollections() {
        itemValuesMap = null;
        activeItemValues = null;
        currentItemValues = null;
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

    public boolean supportsCalculation() {
        return !getItemDefinition().getAlgorithms().isEmpty();
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