package com.amee.domain.auth;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.ObjectType;
import com.amee.domain.environment.Environment;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A Group embodies a community of principals. Principals belong to Groups via GroupPrincipal and can be assigned multiple Roles
 * applicable within each Group.
 * <p/>
 * A Group belongs to a Environment.
 * <p/>
 * When deleting a Group we need to ensure all relevant GroupPrincipals are also removed.
 *
 * @author Diggory Briercliffe
 */
@Entity(name = "Group")
// can't use 'GROUP' as that is a reserved word in SQL
@Table(name = "GROUPS")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Group extends AMEEEnvironmentEntity implements Comparable {

    public final static int NAME_SIZE = 100;
    public final static int DESCRIPTION_SIZE = 1000;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    @Index(name = "NAME_IND")
    private String name = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    public Group() {
        super();
    }

    public Group(Environment environment) {
        super(environment);
    }

    public Group(Environment environment, String name) {
        super(environment);
        setName(name);
    }

    @Override
    public String toString() {
        return "Group_" + getUid();
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Group group = (Group) o;
        return getName().compareToIgnoreCase(group.getName());
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("description", getDescription());
        if (detailed) {
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("created", getCreated());
            obj.put("modified", getModified());
        }
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = XMLUtils.getIdentityJSONObject(this);
        obj.put("name", getName());
        return obj;
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("Group");
        element.setAttribute("uid", getUid());
        element.appendChild(XMLUtils.getElement(document, "Name", getName()));
        element.appendChild(XMLUtils.getElement(document, "Description", getName()));
        if (detailed) {
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    public Element getIdentityElement(Document document) {
        Element element = XMLUtils.getIdentityElement(document, this);
        element.appendChild(XMLUtils.getElement(document, "Name", getName()));
        return element;
    }

    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setDescription(element.elementText("Description"));
    }

    public String getAddress() {
        return "/" + getName();
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

    public ObjectType getObjectType() {
        return ObjectType.GRP;
    }
}
