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
import com.amee.core.ObjectType;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.Pathable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;

@Entity
@Table(name = "DATA_CATEGORY")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DataCategory extends AMEEEnvironmentEntity implements Pathable {

    public final static int NAME_SIZE = 255;
    public final static int PATH_SIZE = 255;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_CATEGORY_ID")
    private DataCategory dataCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ITEM_DEFINITION_ID")
    private ItemDefinition itemDefinition;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "PATH", length = PATH_SIZE, nullable = true)
    @Index(name = "PATH_IND")
    private String path = "";

    public DataCategory() {
        super();
    }

    public DataCategory(Environment environment) {
        super(environment);
    }

    public DataCategory(Environment environment, String name, String path) {
        this(environment);
        setName(name);
        setPath(path);
    }

    public DataCategory(DataCategory dataCategory) {
        this(dataCategory.getEnvironment());
        setDataCategory(dataCategory);
    }

    public DataCategory(DataCategory dataCategory, String name, String path) {
        this(dataCategory);
        setName(name);
        setPath(path);
    }

    public DataCategory(DataCategory dataCategory, String name, String path, ItemDefinition itemDefinition) {
        this(dataCategory, name, path);
        setItemDefinition(itemDefinition);
    }

    public String toString() {
        return "DataCategory_" + getUid();
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("path", getPath());
        obj.put("name", getName());
        if (detailed) {
            obj.put("created", getCreated().toString());
            obj.put("modified", getModified().toString());
            obj.put("environment", getEnvironment().getJSONObject(false));
            if (getDataCategory() != null) {
                obj.put("dataCategory", getDataCategory().getIdentityJSONObject());
            }
            if (getItemDefinition() != null) {
                obj.put("itemDefinition", getItemDefinition().getJSONObject());
            }
        }
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return getJSONObject(false);
    }

    public Element getElement(Document document, boolean detailed) {
        Element profileElement = document.createElement("DataCategory");
        profileElement.setAttribute("uid", getUid());
        profileElement.appendChild(APIUtils.getElement(document, "Name", getName()));
        profileElement.appendChild(APIUtils.getElement(document, "Path", getPath()));
        if (detailed) {
            profileElement.setAttribute("created", getCreated().toString());
            profileElement.setAttribute("modified", getModified().toString());
            profileElement.appendChild(getEnvironment().getIdentityElement(document));
            if (getDataCategory() != null) {
                profileElement.appendChild(getDataCategory().getIdentityElement(document));
            }
            if (getItemDefinition() != null) {
                profileElement.appendChild(getItemDefinition().getIdentityElement(document));
            }
        }
        return profileElement;
    }

    public Element getIdentityElement(Document document) {
        return getElement(document, false);
    }

    public String getDisplayPath() {
        return getPath();
    }

    public String getDisplayName() {
        if (getName().length() > 0) {
            return getName();
        } else {
            return getDisplayPath();
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

    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }

    public void setItemDefinition(ItemDefinition itemDefinition) {
        this.itemDefinition = itemDefinition;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            path = "";
        }
        this.path = path;
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.DC;
    }
}