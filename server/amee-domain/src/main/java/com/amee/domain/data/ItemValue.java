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

import com.amee.base.utils.XMLUtils;
import com.amee.domain.AMEEEntity;
import com.amee.domain.AMEEStatus;
import com.amee.domain.Builder;
import com.amee.domain.LocaleHolder;
import com.amee.domain.ObjectType;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.Pathable;
import com.amee.platform.science.DecimalCompoundUnit;
import com.amee.platform.science.DecimalPerUnit;
import com.amee.platform.science.DecimalUnit;
import com.amee.platform.science.ExternalValue;
import com.amee.platform.science.StartEndDate;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Table(name = "ITEM_VALUE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemValue extends AMEEEntity implements Pathable, ExternalValue {

    // 32767 because this is bigger than 255, smaller than 65535 and fits into an exact number of bits.
    public final static int VALUE_SIZE = 32767;
    public final static int UNIT_SIZE = 255;
    public final static int PER_UNIT_SIZE = 255;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_VALUE_DEFINITION_ID")
    private ItemValueDefinition itemValueDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    @Column(name = "VALUE", nullable = false, length = VALUE_SIZE)
    private String value = "";

    @Column(name = "UNIT", nullable = true, length = UNIT_SIZE)
    private String unit;

    @Column(name = "PER_UNIT", nullable = true, length = PER_UNIT_SIZE)
    private String perUnit;

    @Column(name = "START_DATE")
    @Index(name = "START_DATE_IND")
    private Date startDate = new Date();

//    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @MapKey(name = "locale")
//    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Transient
    private Map<String, LocaleName> localeValues = new HashMap<String, LocaleName>();

    /**
     * Get the collection of locale specific values for this ItemValue.
     *
     * @return the collection of locale specific values. The collection will be empty
     *         if no locale specific values exist.
     */
    public Map<String, LocaleName> getLocaleValues() {
        Map<String, LocaleName> activeLocaleValues = new TreeMap<String, LocaleName>();
        for (String locale : localeValues.keySet()) {
            LocaleName value = localeValues.get(locale);
            if (!value.isTrash()) {
                activeLocaleValues.put(locale, value);
            }
        }
        return activeLocaleValues;
    }

    /*
     * Get the locale specific value of this ItemValue for the locale of the current thread.
     *
     * The locale specific value of this ItemValue for the locale of the current thread.
     * If no locale specific value is found, the default value will be returned.
     */

    @SuppressWarnings("unchecked")
    private String getLocaleValue() {
        String name = null;
        LocaleName localeName = localeValues.get(LocaleHolder.getLocale());
        if (localeName != null && !localeName.isTrash()) {
            name = localeName.getName();
        }
        return name;
    }

    public void addLocaleName(LocaleName localeName) {
        localeValues.put(localeName.getLocale(), localeName);
    }

    @Transient
    private Builder builder;

    public ItemValue() {
        super();
    }

    public ItemValue(ItemValueDefinition itemValueDefinition, Item item, String value) {
        this();
        setItemValueDefinition(itemValueDefinition);
        setItem(item);
        setValue(value);
        item.addItemValue(this);
        // Default startDate is that of the parent Item.
        this.startDate = item.getStartDate();
    }

    public String toString() {
        return "ItemValue_" + getUid();
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public String getUsableValue() {
        if (!isUsableValue())
            return null;

        return getValue();
    }

    public boolean isUsableValue() {
        return !StringUtils.isBlank(getValue());
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return builder.getJSONObject(detailed);
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("path", getPath());
        return obj;
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        return builder.getElement(document, detailed);
    }

    public Element getIdentityElement(Document document) {
        return XMLUtils.getIdentityElement(document, this);
    }

    public Environment getEnvironment() {
        return getItem().getEnvironment();
    }

    public String getName() {
        return getItemValueDefinition().getName();
    }

    public String getDisplayName() {
        return getName();
    }

    public String getPath() {
        return getItemValueDefinition().getPath();
    }

    public String getDisplayPath() {
        return getPath();
    }

    public ItemValueDefinition getItemValueDefinition() {
        return itemValueDefinition;
    }

    public void setItemValueDefinition(ItemValueDefinition itemValueDefinition) {
        this.itemValueDefinition = itemValueDefinition;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getValue() {
        if (getItemValueDefinition().isText() && !LocaleHolder.isDefaultLocale()) {
            String localeValue = getLocaleValue();
            if (localeValue != null) {
                return localeValue;
            }
        }
        return value;
    }

    public void setValue(String value) {

        if (value == null) {
            value = "";
        }
        if (value.length() > VALUE_SIZE) {
            value = value.substring(0, VALUE_SIZE - 1);
        }

        // Ensure numerics are a valid format.
        if (getItemValueDefinition().isDecimal() && !value.isEmpty()) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                log.warn("setValue() - Invalid number format: " + value);
                throw new IllegalArgumentException("Invalid number format: " + value);
            }
        }
        this.value = value;
    }

    public StartEndDate getStartDate() {
        return new StartEndDate(startDate);
    }

    public boolean isDecimal() {
        return getItemValueDefinition().isDecimal();
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ObjectType getObjectType() {
        return ObjectType.IV;
    }

    public DecimalUnit getUnit() {
        return (unit != null) ? DecimalUnit.valueOf(unit) : getItemValueDefinition().getUnit();
    }

    public DecimalUnit getCanonicalUnit() {
        return getItemValueDefinition().getUnit();
    }

    public void setUnit(String unit) throws IllegalArgumentException {
        if (!getItemValueDefinition().isValidUnit(unit)) {
            throw new IllegalArgumentException("The unit argument is not valid: " + unit);
        }
        this.unit = unit;
    }

    public DecimalPerUnit getPerUnit() {
        if (perUnit != null) {
            if (perUnit.equals("none")) {
                return DecimalPerUnit.valueOf(getItem().getDuration());
            } else {
                return DecimalPerUnit.valueOf(perUnit);
            }
        } else {
            return getItemValueDefinition().getPerUnit();
        }
    }

    public DecimalPerUnit getCanonicalPerUnit() {
        return getItemValueDefinition().getPerUnit();
    }

    public void setPerUnit(String perUnit) throws IllegalArgumentException {
        if (!getItemValueDefinition().isValidPerUnit(perUnit)) {
            throw new IllegalArgumentException("The perUnit argument is not valid: " + perUnit);
        }
        this.perUnit = perUnit;
    }

    public DecimalCompoundUnit getCompoundUnit() {
        return getUnit().with(getPerUnit());
    }

    public DecimalCompoundUnit getCanonicalCompoundUnit() {
        return getItemValueDefinition().getCanonicalCompoundUnit();
    }

    public boolean hasUnit() {
        return getItemValueDefinition().hasUnit();
    }

    public boolean hasPerUnit() {
        return getItemValueDefinition().hasPerUnit();
    }

    public boolean hasPerTimeUnit() {
        return hasPerUnit() && getPerUnit().isTime();
    }

    public boolean isNonZero() {
        return getItemValueDefinition().isDecimal() &&
                !StringUtils.isBlank(getValue()) &&
                !new BigDecimal(getValue()).equals(BigDecimal.ZERO);
    }

    public ItemValue getCopy() {
        ItemValue clone = new ItemValue();
        clone.setUid(getUid());
        clone.setItemValueDefinition(getItemValueDefinition());
        clone.setValue(getValue());
        clone.setItem(getItem());
        clone.setStartDate(getStartDate());
        if (hasUnit()) {
            clone.setUnit(getUnit().toString());
        }
        if (hasPerUnit()) {
            clone.setPerUnit(getPerUnit().toString());
        }
        return clone;
    }

    public String getLabel() {
        return getItemValueDefinition().getLabel();
    }

    @Override
    public boolean isTrash() {
        return status.equals(AMEEStatus.TRASH) || getItem().isTrash() || getItemValueDefinition().isTrash();
    }

    /**
     * As an ExternalValue, an ItemValue is convertible. It can be converted, at runtime, from one unit to another.
     *
     * @return true, always
     */
    public boolean isConvertible() {
        return true;
    }
}