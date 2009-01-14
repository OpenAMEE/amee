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
import com.jellymold.sheet.Choice;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import gc.carbon.domain.*;
import gc.carbon.domain.data.builder.v2.ItemValueDefinitionBuilder;
import gc.carbon.APIVersion;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.*;

// TODO: add a way to define UI widget
// TODO: add a way to define range of values
// TODO: add state (draft, live)
// TODO: max & min values (for numbers)
// TODO: regex validation?

@Entity
@Table(name = "ITEM_VALUE_DEFINITION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemValueDefinition implements PersistentObject {

    public final static int NAME_SIZE = 255;
    public final static int PATH_SIZE = 255;
    public final static int VALUE_SIZE = 255;
    public final static int CHOICES_SIZE = 255;
    public final static int ALLOWED_ROLES_SIZE = 255;
            
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_DEFINITION_ID")
    private ItemDefinition itemDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VALUE_DEFINITION_ID")
    private ValueDefinition valueDefinition;

    @Column(name = "UNIT")
    private String unit;

    @Column(name = "PER_UNIT")
    private String perUnit;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "PATH", length = PATH_SIZE, nullable = false)
    @Index(name = "PATH_IND")
    private String path = "";

    @Column(name = "VALUE", length = VALUE_SIZE, nullable = true)
    private String value = "";

    // Comma separated key/value pairs. Value is key if key not supplied. Example: "key=value,key=value"
    @Column(name = "CHOICES")
    private String choices = "";

    @Column(name = "FROM_PROFILE")
    @Index(name = "FROM_PROFILE_IND")
    private boolean fromProfile = false;

    @Column(name = "FROM_DATA")
    @Index(name = "FROM_PROFILE_IND")
    private boolean fromData = false;

    @Column(name = "ALLOWED_ROLES", length = ALLOWED_ROLES_SIZE, nullable = true)
    private String allowedRoles = "";

    @Column(name = "CREATED")
    private Date created = Calendar.getInstance().getTime();

    @Column(name = "MODIFIED")
    private Date modified = Calendar.getInstance().getTime();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
        name = "ITEM_VALUE_DEFINITION_API_VERSION",
        joinColumns = {@JoinColumn(name = "ITEM_VALUE_DEFINITION_ID")},
        inverseJoinColumns = {@JoinColumn(name = "API_VERSION_ID")}
    )
    private Set<APIVersion> apiVersions = new HashSet<APIVersion>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ALIASED_TO")
    private ItemValueDefinition aliasedTo = null;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ALIASED_TO")
    private List<ItemValueDefinition> aliases = new ArrayList<ItemValueDefinition>();

    @Transient
    private Builder builder;

    public ItemValueDefinition() {
        super();
        setUid(UidGen.getUid());
        setBuilder(new ItemValueDefinitionBuilder(this));
    }

    public ItemValueDefinition(ItemDefinition itemDefinition) {
        this();
        setEnvironment(itemDefinition.getEnvironment());
        setItemDefinition(itemDefinition);
        itemDefinition.add(this);
    }

    public ItemValueDefinition(ItemDefinition itemDefinition, String name) {
        this(itemDefinition);
        setName(name);
    }

    public String toString() {
        return "ItemValueDefinition_" + getUid();
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    @Transient
    public String getUsableValue() {
        String value = getValue();
        if (value != null) {
            if (value.length() == 0) {
                value = null;
            }
            // TODO: more validations, based on ValueDefinition
        }
        return value;
    }

    @Transient
    public boolean isChoicesAvailable() {
        return getChoices().length() > 0;
    }

    @Transient
    public List<Choice> getChoiceList() {
        return Choice.parseChoices(getChoices());
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return builder.getJSONObject(detailed);
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
        return builder.getElement(document, detailed);
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
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
        this.itemDefinition = itemDefinition;
    }

    public ValueDefinition getValueDefinition() {
        return valueDefinition;
    }

    public void setValueDefinition(ValueDefinition valueDefinition) {
        this.valueDefinition = valueDefinition;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        this.value = value;
    }

    public String getChoices() {
        return choices;
    }

    public void setChoices(String choices) {
        if (choices == null) {
            choices = "";
        }
        this.choices = choices;
    }

    public boolean isFromProfile() {
        return fromProfile;
    }

    public void setFromProfile(boolean fromProfile) {
        this.fromProfile = fromProfile;
    }

    public boolean isFromData() {
        return fromData;
    }

    public void setFromData(boolean fromData) {
        this.fromData = fromData;
    }

    public String getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(String allowedRoles) {
        if (allowedRoles == null) {
            allowedRoles = "";
        }
        this.allowedRoles = allowedRoles;
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
        return ObjectType.IVD;
    }

    public void setPerUnit(String perUnit) {
        this.perUnit = perUnit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Unit getUnit() {
        return Unit.valueOf(unit);
    }

    public PerUnit getPerUnit() {
        return PerUnit.valueOf(perUnit);
    }

    public boolean hasUnits() {
        return unit != null;
    }

    public boolean hasPerUnits() {
        return perUnit != null;
    }

    public boolean isValidUnit(String unit) {
        return getUnit().isCompatibleWith(unit);
    }

    public boolean isValidPerUnit(String perUnit) {
        return getPerUnit().isCompatibleWith(perUnit);
    }

    public Unit getCompoundUnit() {
        return getUnit().with(getPerUnit());
    }

    public Set<APIVersion> getAPIVersions() {
        return apiVersions;
    }

    public void setAPIVersions(Set<APIVersion> apiVersions) {
        this.apiVersions = apiVersions;
    }

    public boolean includedInAPIVersion(APIVersion apiVersion) {
        return apiVersions.contains(apiVersion);
    }

    public ItemValueDefinition getAliasedTo() {
        return aliasedTo;
    }

    public void setAliasedTo(ItemValueDefinition ivd) {
        this.aliasedTo = ivd;    
    }

    public List<ItemValueDefinition> getAliases() {
        return aliases;    
    }

    public String getCannonicalPath() {
        if (aliasedTo != null) {
            return aliasedTo.getPath();  
        } else {
            return getPath();
        }
    }

    public String getCannonicalName() {
        if (aliasedTo != null) {
            return aliasedTo.getName();
        } else {
            return getName();
        }
    }

}