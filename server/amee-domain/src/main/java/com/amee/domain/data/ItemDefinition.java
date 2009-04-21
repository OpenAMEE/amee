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

import com.amee.core.ObjectType;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.core.APIUtils;
import com.amee.domain.APIVersion;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.environment.Environment;
import com.amee.domain.InternalValue;
import com.amee.domain.sheet.Choice;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "ITEM_DEFINITION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemDefinition extends AMEEEnvironmentEntity {

    public final static int NAME_SIZE = 255;
    public final static int DRILL_DOWN_SIZE = 255;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "DRILL_DOWN", length = DRILL_DOWN_SIZE, nullable = true)
    private String drillDown = "";

    @OneToMany(mappedBy = "itemDefinition", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("name")
    private Set<Algorithm> algorithms = new HashSet<Algorithm>();

    @OneToMany(mappedBy = "itemDefinition", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("name")
    private Set<ItemValueDefinition> itemValueDefinitions = new HashSet<ItemValueDefinition>();

    public ItemDefinition() {
        super();
    }

    public ItemDefinition(Environment environment) {
        super(environment);
    }

    public ItemDefinition(Environment environment, String name) {
        this(environment);
        setName(name);
    }

    public String toString() {
        return "ItemDefinition_" + getUid();
    }

    public void add(Algorithm algorithm) {
        getAlgorithms().add(algorithm);
    }

    public void remove(Algorithm algorithm) {
        getAlgorithms().remove(algorithm);
    }

    public void add(ItemValueDefinition itemValueDefinition) {
        getItemValueDefinitions().add(itemValueDefinition);
    }

    public void remove(ItemValueDefinition itemValueDefinition) {
        getItemValueDefinitions().remove(itemValueDefinition);
    }

    @Transient
    public boolean hasDrillDownAvailable() {
        return getDrillDown().length() > 0;
    }

    @Transient
    public List<Choice> getDrillDownChoices() {
        return Choice.parseChoices(getDrillDown());
    }

    @Transient
    public ItemValueDefinition getItemValueDefinition(String path) {
        for (ItemValueDefinition itemValueDefinition : getItemValueDefinitions()) {
            if (itemValueDefinition.getPath().equalsIgnoreCase(path)) {
                return itemValueDefinition;
            }
        }
        return null;
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("drillDown", getDrillDown());
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
        }
        return obj;
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
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ItemDefinition");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "DrillDown", getDrillDown()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(getEnvironment().getIdentityElement(document));
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
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

    public String getDrillDown() {
        return drillDown;
    }

    public void setDrillDown(String drillDown) {
        if (drillDown == null) {
            drillDown = "";
        }
        this.drillDown = drillDown;
    }

    public Set<Algorithm> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(Set<Algorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public Set<ItemValueDefinition> getItemValueDefinitions() {
        return itemValueDefinitions;
    }

    public void setItemValueDefinitions(Set<ItemValueDefinition> itemValueDefinitions) {
        this.itemValueDefinitions = itemValueDefinitions;
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.ID;
    }

    //
    /**
     * Get the algorithm corresponding to the supplied name.
     *
     * @param name - the name of the Algorithm to retreive
     * @return the Algorithm corresponding to the supplied name
     */
    public Algorithm getAlgorithm(String name) {
        for (Algorithm algorithm : getAlgorithms()) {
            if (algorithm.getName().equalsIgnoreCase(name)) {
                return algorithm;
            }
        }
        return null;
    }

    public void appendInternalValues(Map<ItemValueDefinition, InternalValue> values, APIVersion version) {
        for (ItemValueDefinition ivd : getItemValueDefinitions()) {
            if (ivd.isUsableValue() && ivd.isValidInAPIVersion(version))
                values.put(ivd, new InternalValue(ivd));
        }
    }

}