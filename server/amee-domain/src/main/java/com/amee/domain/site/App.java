package com.amee.domain.site;

import com.amee.domain.AMEEEntity;
import com.amee.core.APIUtils;
import com.amee.domain.DatedObject;
import com.amee.domain.auth.Action;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * An App encapsulates a group of web resources into a logical group under a URI. Apps can be attached
 * to multiple Sites via SiteApps.
 * <p/>
 * Apps define a set of Actions which can be performed by Users within the App.
 * <p/>
 * When deleting an App we need to ensure all relevant SiteApps are also removed. Actions should
 * be automatically removed but we need to deal with dependancies of Actions.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "APP")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class App extends AMEEEntity implements DatedObject, Comparable {

    public final static int NAME_SIZE = 100;
    public final static int DESCRIPTION_SIZE = 1000;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("key")
    private Set<Action> actions = new HashSet<Action>();

    @Column(name = "ALLOW_CLIENT_CACHE", nullable = false)
    private Boolean allowClientCache = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public App() {
        super();
    }

    public App(String name) {
        this();
        setName(name);
    }

    public App(String name, String description) {
        this(name);
        setDescription(description);
    }

    public void add(Action action) {
        action.setApp(this);
        getActions().add(action);
    }

    public void remove(Action action) {
        getActions().remove(action);
    }

    public String toString() {
        return "App_" + getUid();
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        App app = (App) o;
        return getName().compareToIgnoreCase(app.getName());
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
        obj.put("description", getDescription());
        obj.put("allowClientCache", getAllowClientCache());
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
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
        Element element = document.createElement("App");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        element.appendChild(APIUtils.getElement(document, "AllowClientCache", "" + getAllowClientCache()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        Element element = APIUtils.getIdentityElement(document, this);
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        return element;
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setDescription(element.elementText("Description"));
        setAllowClientCache(Boolean.parseBoolean(element.elementText("AllowClientCache")));
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

    public Boolean getAllowClientCache() {
        return allowClientCache;
    }

    public Boolean isAllowClientCache() {
        return allowClientCache;
    }

    public void setAllowClientCache(Boolean allowClientCache) {
        if (allowClientCache != null) {
            this.allowClientCache = allowClientCache;
        }
    }

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        if (actions == null) {
            actions = new HashSet<Action>();
        }
        this.actions = actions;
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