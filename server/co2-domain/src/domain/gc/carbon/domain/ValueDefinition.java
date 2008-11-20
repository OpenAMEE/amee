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
package gc.carbon.domain;

import com.jellymold.kiwi.Environment;
import com.jellymold.sheet.ValueType;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jboss.seam.annotations.Name;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "VALUE_DEFINITION")
@Name("valueDefinition")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ValueDefinition implements PersistentObject {

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
    private String name = "";

    @Column(name = "DESCRIPTION")
    private String description = "";

    @Column(name = "VALUE_TYPE")
    private ValueType valueType = ValueType.TEXT;

    @Column(name = "CREATED")
    private Date created = null;

    @Column(name = "MODIFIED")
    private Date modified = null;

    public ValueDefinition() {
        super();
        setUid(UidGen.getUid());
    }

    public ValueDefinition(Environment environment, String name, ValueType valueType) {
        this();
        setEnvironment(environment);
        setName(name);
        setValueType(valueType);
    }

    public String toString() {
        return "ValueDefinition_" + getUid();
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
        obj.put("valueType", getValueType());
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
        element.appendChild(APIUtils.getElement(document, "ValueType", getValueType().toString()));
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

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        if (valueType == null) {
            valueType = ValueType.TEXT;
        }
        this.valueType = valueType;
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
}