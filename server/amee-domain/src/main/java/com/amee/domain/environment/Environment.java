package com.amee.domain.environment;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.AMEEEntity;
import com.amee.domain.ObjectType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ENVIRONMENT")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Environment extends AMEEEntity implements Comparable {

    public final static int NAME_SIZE = 255;
    public final static int PATH_SIZE = 255;
    public final static int DESCRIPTION_SIZE = 1000;
    public final static int OWNER_SIZE = 255;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "PATH", length = PATH_SIZE, nullable = false)
    private String path = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    @Column(name = "OWNER", length = OWNER_SIZE, nullable = false)
    private String owner = "";

    @Column(name = "ITEMS_PER_PAGE", nullable = false)
    private Integer itemsPerPage = 10;

    @Column(name = "ITEMS_PER_FEED", nullable = false)
    private Integer itemsPerFeed = 10;

    public Environment() {
        super();
    }

    public Environment(String name) {
        this();
        setName(name);
    }

    @Override
    public String toString() {
        return "Environment_" + getUid();
    }

    public int compareTo(Object o) {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Environment environment = (Environment) o;
        return getUid().compareTo(environment.getUid());
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("path", getPath());
        obj.put("description", getDescription());
        obj.put("owner", getOwner());
        obj.put("itemsPerPage", getItemsPerPage());
        obj.put("itemsPerFeed", getItemsPerFeed());
        if (detailed) {
            obj.put("created", getCreated().toString());
            obj.put("modified", getModified().toString());
        }
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return XMLUtils.getIdentityJSONObject(this);
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("Environment");
        element.setAttribute("uid", getUid());
        element.appendChild(XMLUtils.getElement(document, "Name", getName()));
        element.appendChild(XMLUtils.getElement(document, "Path", getPath()));
        element.appendChild(XMLUtils.getElement(document, "Description", getDescription()));
        element.appendChild(XMLUtils.getElement(document, "Owner", getOwner()));
        element.appendChild(XMLUtils.getElement(document, "ItemsPerPage", getItemsPerPage().toString()));
        element.appendChild(XMLUtils.getElement(document, "ItemsPerFeed", getItemsPerFeed().toString()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    public Element getIdentityElement(Document document) {
        return XMLUtils.getIdentityElement(document, this);
    }

    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setPath(element.elementText("Path"));
        setDescription(element.elementText("Description"));
        setOwner(element.elementText("Owner"));
        setItemsPerPage(element.elementText("ItemsPerPage"));
        setItemsPerFeed(element.elementText("ItemsPerFeed"));
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        if (itemsPerPage != null) {
            this.itemsPerPage = itemsPerPage;
        }
    }

    public void setItemsPerPage(String itemsPerPage) {
        try {
            setItemsPerPage(Integer.parseInt(itemsPerPage));
        } catch (NumberFormatException e) {
            // swallow
        }
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        if (owner == null) {
            owner = "";
        }
        this.owner = owner;
    }

    public Integer getItemsPerFeed() {
        return itemsPerFeed;
    }

    public void setItemsPerFeed(Integer itemsPerFeed) {
        this.itemsPerFeed = itemsPerFeed;
    }

    public void setItemsPerFeed(String itemsPerFeed) {
        try {
            setItemsPerFeed(Integer.parseInt(itemsPerFeed));
        } catch (NumberFormatException e) {
            // swallow
        }
    }

    public ObjectType getObjectType() {
        return ObjectType.ENV;
    }
}