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
package gc.carbon.domain.data;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import gc.carbon.domain.ObjectType;
import gc.carbon.domain.Builder;
import gc.carbon.domain.data.builder.BuildableItemValue;
import gc.carbon.domain.data.builder.v2.ItemValueBuilder;
import gc.carbon.domain.path.Pathable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

// TODO: add state (draft, live)

@Entity
@Table(name = "ITEM_VALUE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemValue implements PersistentObject, Pathable, BuildableItemValue {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_VALUE_DEFINITION_ID")
    private ItemValueDefinition itemValueDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    // TODO: sizing
    // TODO: also see setValue below
    @Column(name = "VALUE")
    private String value = "";

    @Column(name = "CREATED")
    private Date created = Calendar.getInstance().getTime();

    @Column(name = "MODIFIED")
    private Date modified = Calendar.getInstance().getTime();

    @Column(name = "UNIT")
    private String unit;

    @Column(name = "PER_UNIT")
    private String perUnit;

    @Transient
    private Builder builder;

    public ItemValue() {
        super();
        setUid(UidGen.getUid());
        setBuilder(new ItemValueBuilder(this));
    }

    public ItemValue(ItemValueDefinition itemValueDefinition, Item item, String value) {
        this();
        setItemValueDefinition(itemValueDefinition);
        setItem(item);
        setValue(value);
        item.add(this);
    }

    public String toString() {
        return "ItemValue_" + getUid();
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public String getUsableValue() {
        String value = getValue();
        if (value != null) {
            if (value.length() == 0) {
                value = null;
            }
            // TODO: more validations, based on ValueType
        }
        return value;
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
        return getItemValueDefinition().getPath();
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid == null) {
            uid = "";
        }
        this.uid = uid;
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
        return value;
    }

    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        // TODO: better way to enforce this limit
        // TODO: needs to match limit of VALUE column
        if (value.length() > 255) {
            value = value.substring(0, 255);
        }
        this.value = value;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.IV;
    }


    public String getUnit() {
        return (unit != null) ? unit : (hasUnits()) ? itemValueDefinition.getInternalUnit() : null;
    }

    public void setUnit(String unit) throws IllegalArgumentException {
        if (unit == null || unit.length() == 0) {
            return;
        }
        if (!hasUnits() || !itemValueDefinition.isValidUnit(unit)) {
            throw new IllegalArgumentException();
        }
        this.unit = unit;
    }

    public String getPerUnit() {
        return (perUnit != null) ? perUnit : (hasPerUnits()) ? itemValueDefinition.getInternalPerUnit() : null;
    }

    public void setPerUnit(String perUnit) throws IllegalArgumentException {
        if (perUnit == null || perUnit.length() == 0) {
            return;
        }
        if (!hasPerUnits() || !itemValueDefinition.isValidPerUnit(perUnit)) {
            throw new IllegalArgumentException();
        }
        this.perUnit = perUnit;
    }

    public boolean hasUnits() {
        return itemValueDefinition.hasUnits();
    }

    public boolean hasPerUnits() {
        return itemValueDefinition.hasPerUnits();
    }
}