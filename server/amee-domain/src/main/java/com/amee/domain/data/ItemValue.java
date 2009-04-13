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

import com.amee.core.*;
import com.amee.domain.AMEEEntity;
import com.amee.domain.Builder;
import com.amee.domain.StartEndDate;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.Pathable;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "ITEM_VALUE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemValue extends AMEEEntity implements Pathable {

    // 32767 because this is bigger than 255, smaller than 65535 and fits into an exact number of bits
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

    @Column(name = "CREATED")
    private Date created = Calendar.getInstance().getTime();

    @Column(name = "MODIFIED")
    private Date modified = Calendar.getInstance().getTime();

    @Column(name = "UNIT", nullable = true, length = UNIT_SIZE)
    private String unit;

    @Column(name = "PER_UNIT", nullable = true, length = PER_UNIT_SIZE)
    private String perUnit;

    @Column(name = "START_DATE")
    @Index(name = "START_DATE_IND")
    protected Date startDate = Calendar.getInstance().getTime();

    @Column(name = "END_DATE")
    @Index(name = "END_DATE_IND")
    protected Date endDate;

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

    @Transient
    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    @Transient
    public String getUsableValue() {
        if (!isUsableValue())
            return null;

        return getValue();
    }

    public boolean isUsableValue() {
        return !StringUtils.isBlank(getValue());
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return builder.getJSONObject(detailed);
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("path", getPath());
        return obj;
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        return builder.getElement(document, detailed);
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);

    }

    @Transient
    public Environment getEnvironment() {
        return getItem().getEnvironment();
    }

    @Transient
    public String getName() {
        return getItemValueDefinition().getName();
    }

    @Transient
    public String getDisplayName() {
        return getItemValueDefinition().getName();
    }

    @Transient
    public String getPath() {
        return getItemValueDefinition().getPath();
    }

    @Transient
    public String getDisplayPath() {
        return getPath();
    }

    @PrePersist
    public void onCreate() {
        Date now = Calendar.getInstance().getTime();
        setCreated(now);
        setModified(now);
    }

    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
    }

    @Transient
    public ItemValueDefinition getItemValueDefinition() {
        return itemValueDefinition;
    }

    @Transient
    public void setItemValueDefinition(ItemValueDefinition itemValueDefinition) {
        this.itemValueDefinition = itemValueDefinition;
    }

    @Transient
    public Item getItem() {
        return item;
    }

    @Transient
    public void setItem(Item item) {
        this.item = item;
    }

    @Transient
    public String getValue() {
        return value;
    }

    @Transient
    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        if (value.length() > VALUE_SIZE) {
            value = value.substring(0, VALUE_SIZE - 1);
        }
        this.value = value;
    }

    @Transient
    public Date getCreated() {
        return created;
    }

    @Transient
    public void setCreated(Date created) {
        this.created = created;
    }

    @Transient
    public Date getModified() {
        return modified;
    }

    @Transient
    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Transient
    public StartEndDate getStartDate() {
        return new StartEndDate(startDate);
    }

    @Transient
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Transient
    public StartEndDate getEndDate() {
        if (endDate != null) {
            return new StartEndDate(endDate);
        } else {
            return null;
        }
    }

    @Transient
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.IV;
    }

    @Transient
    public DecimalUnit getUnit() {
        return (unit != null) ? DecimalUnit.valueOf(unit) : itemValueDefinition.getUnit();
    }

    @Transient
    public void setUnit(String unit) throws IllegalArgumentException {
        if (!itemValueDefinition.isValidUnit(unit)) {
            throw new IllegalArgumentException();
        }
        this.unit = unit;
    }

    @Transient
    public DecimalPerUnit getPerUnit() {
        if (perUnit != null) {
            if (perUnit.equals("none")) {
                 return DecimalPerUnit.valueOf(getItem().getDuration());
             } else {
                 return DecimalPerUnit.valueOf(perUnit);
             }
         } else {                                                             
            return itemValueDefinition.getPerUnit();
        }
    }

    @Transient
    public void setPerUnit(String perUnit) throws IllegalArgumentException {
        if (!itemValueDefinition.isValidPerUnit(perUnit)) {
            throw new IllegalArgumentException();
        }
        this.perUnit = perUnit;
    }

    @Transient
    public DecimalCompoundUnit getCompoundUnit() {
        return getUnit().with(getPerUnit());
    }

    public boolean hasUnit() {
        return itemValueDefinition.hasUnits();
    }

    public boolean hasPerUnit() {
        return itemValueDefinition.hasPerUnits();
    }

    public boolean hasPerTimeUnit() {
        return hasPerUnit() && getPerUnit().isTime();
    }

    public boolean isNonZero() {
        return getItemValueDefinition().isDecimal() &&
                !StringUtils.isBlank(getValue()) &&
                !new BigDecimal(getValue()).equals(BigDecimal.ZERO);
    }

    @Transient
    public ItemValue getCopy() {
        ItemValue clone = new ItemValue();
        clone.setUid(getUid());
        clone.setValue(getValue());
        clone.setItemValueDefinition(getItemValueDefinition());
        clone.setItem(getItem());
        if (hasUnit())
            clone.setUnit(getUnit().toString());
        if (hasPerUnit())
            clone.setPerUnit(getPerUnit().toString());
        return clone;
    }
}