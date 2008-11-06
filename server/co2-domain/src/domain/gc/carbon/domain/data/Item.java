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

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import gc.carbon.domain.path.InternalItemValue;
import gc.carbon.domain.path.Pathable;
import gc.carbon.builder.domain.BuildableItem;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.*;

@Entity
@Inheritance
@Table(name = "ITEM")
// TODO: add index to TYPE
@DiscriminatorColumn(name = "TYPE", length = 3)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class Item implements PersistentObject, Pathable, BuildableItem {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_DEFINITION_ID")
    private ItemDefinition itemDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_CATEGORY_ID")
    private DataCategory dataCategory;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<ItemValue> itemValues = new ArrayList<ItemValue>();

    @Column(name = "NAME", nullable = false)
    private String name = "";

    @Column(name = "CREATED")
    private Date created = Calendar.getInstance().getTime();

    @Column(name = "MODIFIED")
    private Date modified = Calendar.getInstance().getTime();

    public Item() {
        super();
        setUid(UidGen.getUid());
    }

    public Item(DataCategory dataCategory, ItemDefinition itemDefinition) {
        this();
        setDataCategory(dataCategory);
        setEnvironment(dataCategory.getEnvironment());
        setItemDefinition(itemDefinition);
    }

    public void add(ItemValue itemValue) {
        getItemValues().add(itemValue);
    }

    public Map<String, ItemValue> getItemValuesMap() {
        Map<String, ItemValue> itemValuesMap = new HashMap<String, ItemValue>();
        for (ItemValue itemValue : getItemValues()) {
            itemValuesMap.put(itemValue.getDisplayPath(), itemValue);
        }
        return itemValuesMap;
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    @Transient
    public void buildElement(JSONObject obj, boolean detailed) throws JSONException {
        obj.put("uid", getUid());
        obj.put("name", getDisplayName());
        JSONArray itemValues = new JSONArray();
        for (ItemValue itemValue : getItemValues()) {
            itemValues.put(itemValue.getJSONObject(false));
        }
        obj.put("itemValues", itemValues);
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("itemDefinition", getItemDefinition().getIdentityJSONObject());
            obj.put("dataCategory", getDataCategory().getIdentityJSONObject());
        }
    }

    @Transient
    public void buildElement(Document document, Element element, boolean detailed) {
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getDisplayName()));
        Element itemValuesElem = document.createElement("ItemValues");
        for (ItemValue itemValue : getItemValues()) {
            itemValuesElem.appendChild(itemValue.getElement(document, false));
        }
        element.appendChild(itemValuesElem);
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.appendChild(getItemDefinition().getIdentityElement(document));
            element.appendChild(getDataCategory().getIdentityElement(document));
        }
    }

    @Transient
    public abstract String getType();

    @PrePersist
    public void onCreate() {
        setCreated(Calendar.getInstance().getTime());
        setModified(getCreated());
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

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        if (environment != null) {
            this.environment = environment;
        }
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

    public List<ItemValue> getItemValues() {
        return itemValues;
    }

    public void setItemValues(List<ItemValue> itemValues) {
        if (itemValues != null) {
            this.itemValues = itemValues;
        }
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

    public void appendInternalValues(Map<ItemValueDefinition, InternalItemValue> values) {
        for (ItemValue iv : getItemValues()) {
            if (iv.getUsableValue() != null)
                values.put(iv.getItemValueDefinition(), new InternalItemValue(iv));
        }
    }

    //TODO - Remove these calls. Domain objects should not implement APIObject...

    public JSONObject getJSONObject(boolean b) throws JSONException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Element getElement(Document document, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
