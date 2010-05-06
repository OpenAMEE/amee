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
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.AMEEStatus;
import com.amee.domain.IMetadataService;
import com.amee.domain.LocaleHolder;
import com.amee.domain.Metadata;
import com.amee.domain.ObjectType;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.Pathable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Resource;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Table(name = "DATA_CATEGORY")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Configurable(autowire = Autowire.BY_TYPE)
public class DataCategory extends AMEEEnvironmentEntity implements Pathable {

    public final static int NAME_MIN_SIZE = 3;
    public final static int NAME_MAX_SIZE = 255;
    public final static int PATH_MIN_SIZE = 3;
    public final static int PATH_MAX_SIZE = 255;
    public final static int WIKI_NAME_MIN_SIZE = 3;
    public final static int WIKI_NAME_MAX_SIZE = 255;
    public final static int WIKI_DOC_MAX_SIZE = Metadata.VALUE_SIZE;
    public final static int PROVENANCE_MAX_SIZE = 255;
    public final static int AUTHORITY_MAX_SIZE = 255;

    @Transient
    @Resource
    private IMetadataService metadataService;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_CATEGORY_ID")
    private DataCategory dataCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ITEM_DEFINITION_ID")
    private ItemDefinition itemDefinition;

    @Column(name = "NAME", length = NAME_MAX_SIZE, nullable = false)
    private String name = "";

    @Column(name = "PATH", length = PATH_MAX_SIZE, nullable = false)
    @Index(name = "PATH_IND")
    private String path = "";

    @Column(name = "WIKI_NAME", length = WIKI_NAME_MAX_SIZE, nullable = false)
    @Index(name = "WIKI_NAME_IND")
    private String wikiName = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ALIASED_TO_ID")
    private DataCategory aliasedTo;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ALIASED_TO_ID")
    private List<DataCategory> aliases = new ArrayList<DataCategory>();

//    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @MapKey(name = "locale")
//    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Transient
    private Map<String, LocaleName> localeNames = new HashMap<String, LocaleName>();

    @Transient
    private Map<String, Metadata> metadatas = new HashMap<String, Metadata>();

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

    @Override
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
        obj.put("deprecated", isDeprecated());
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
        Element element = document.createElement("DataCategory");
        element.setAttribute("uid", getUid());
        element.appendChild(XMLUtils.getElement(document, "Name", getName()));
        element.appendChild(XMLUtils.getElement(document, "Path", getPath()));
        element.appendChild(XMLUtils.getElement(document, "Deprecated", "" + isDeprecated()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(getEnvironment().getIdentityElement(document));
            if (getDataCategory() != null) {
                element.appendChild(getDataCategory().getIdentityElement(document));
            }
            if (getItemDefinition() != null) {
                element.appendChild(getItemDefinition().getIdentityElement(document));
            }
        }
        return element;
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
        String localeName = getLocaleName();
        if (localeName != null) {
            return localeName;
        } else {
            return name;
        }
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

    public String getWikiName() {
        return wikiName;
    }

    public void setWikiName(String wikiName) {
        if (wikiName == null) {
            wikiName = "";
        }
        this.wikiName = wikiName;
    }

    @Override
    public boolean isTrash() {
        return status.equals(AMEEStatus.TRASH) || ((getItemDefinition() != null) && getItemDefinition().isTrash());
    }

    public DataCategory getAliasedCategory() {
        return aliasedTo;
    }

    public void setAliasedTo(DataCategory aliasedTo) {
        this.aliasedTo = aliasedTo;
    }

    public List<DataCategory> getAliases() {
        return aliases;
    }

    /**
     * Get the collection of locale specific names for this DataCategory.
     *
     * @return the collection of locale specific names. The collection will be empty
     *         if no locale specific names exist.
     */
    public Map<String, LocaleName> getLocaleNames() {
        Map<String, LocaleName> activeLocaleNames = new TreeMap<String, LocaleName>();
        for (String locale : localeNames.keySet()) {
            LocaleName name = localeNames.get(locale);
            if (!name.isTrash()) {
                activeLocaleNames.put(locale, name);
            }
        }
        return activeLocaleNames;
    }

    /*
     * Get the locale specific name of this DataCategory for the locale of the current thread.
     *
     * The locale specific name of this DataCategory for the locale of the current thread.
     * If no locale specific name is found, the default name will be returned.
     */

    @SuppressWarnings("unchecked")
    private String getLocaleName() {
        String name = null;
        LocaleName localeName = localeNames.get(LocaleHolder.getLocale());
        if (localeName != null && !localeName.isTrash()) {
            name = localeName.getName();
        }
        return name;
    }

    public void addLocaleName(LocaleName localeName) {
        localeNames.put(localeName.getLocale(), localeName);
    }

    public String getWikiDoc() {
        return getMetadataValue("wikiDoc");
    }

    public void setWikiDoc(String wikiDoc) {
        getOrCreateMetadata("wikiDoc").setValue(wikiDoc);
    }

    public String getProvenance() {
        return getMetadataValue("provenance");
    }

    public void setProvenance(String provenance) {
        getOrCreateMetadata("provenance").setValue(provenance);
    }

    public String getAuthority() {
        return getMetadataValue("authority");
    }

    public void setAuthority(String authority) {
        getOrCreateMetadata("authority").setValue(authority);
    }

    // TODO: The following three methods are cut-and-pasted between various entities. They should be consolidated.

    private Metadata getMetadata(String key) {
        if (!metadatas.containsKey(key)) {
            metadatas.put(key, metadataService.getMetadataForEntity(this, key));
        }
        return metadatas.get(key);
    }

    private String getMetadataValue(String key) {
        Metadata metadata = getMetadata(key);
        if (metadata != null) {
            return metadata.getValue();
        } else {
            return "";
        }
    }

    protected Metadata getOrCreateMetadata(String key) {
        Metadata metadata = getMetadata(key);
        if (metadata == null) {
            metadata = new Metadata(this, key);
            metadataService.persist(metadata);
            metadatas.put(key, metadata);
        }
        return metadata;
    }

    @Override
    public void setStatus(AMEEStatus status) {
        this.status = status;
        for (DataCategory alias : aliases) {
            alias.setStatus(status);
        }
    }

    public ObjectType getObjectType() {
        return ObjectType.DC;
    }
}