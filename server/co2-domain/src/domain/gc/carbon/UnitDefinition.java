package gc.carbon;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import org.json.JSONObject;
import org.json.JSONException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.jboss.seam.annotations.Name;

import javax.persistence.*;

import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.kiwi.Environment;

import java.util.*;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */

@Entity
@Table(name = "UNIT_DEFINITION")
@Name("unitDefinition")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UnitDefinition implements PersistentObject {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description = "";

    @Column(name = "UNITS")
    private String units;

    @Column(name = "INTERNAL_UNIT")
    private String internalUnit;

    @Column(name = "CREATED")
    private Date created = null;

    @Column(name = "MODIFIED")
    private Date modified = null;

    public UnitDefinition() {
        super();
        setUid(UidGen.getUid());
    }

    public UnitDefinition(Environment environment, String name, List<String> choices) {
        this();
        setEnvironment(environment);
        setName(name);
        setUnits(choices);
    }

    public String toString() {
        return "UnitDefinition_" + getUid();
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
        obj.put("units", getUnits().toArray());
        obj.put("internalUnit", getInternalUnit());
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            obj.put("description", getDescription());
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
        Element element = document.createElement("ValueDefinition");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        Element choices = document.createElement("Choices");
        for (String choice : getUnits()) {
            choices.appendChild(APIUtils.getElement(document, "Choice", choice));
        }
        element.appendChild(choices);
        element.appendChild(APIUtils.getElement(document, "InternalUnit", getInternalUnit()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
            element.appendChild(getEnvironment().getIdentityElement(document));
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

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
        this.environment = environment;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description;
    }

    public List<String> getUnits() {
        return Arrays.asList(units.split(","));
    }

    public void setUnits(List<String> units) {
        if (units.isEmpty())
            return;
        String unitsStr = units.remove(0);
        for (String unit : units) {
            unitsStr = unitsStr + "," + unit;
        }
        this.units = unitsStr;
    }

    public String getInternalUnit() {
        return internalUnit;
    }

    public void setInternalUnit(String internalUnit) {
        this.internalUnit = internalUnit;
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

    public boolean has(String unit) {
        for (String choice: getUnits()) {
            if (choice.equals(unit))
                return true;
        }
        return false;
    }

}
