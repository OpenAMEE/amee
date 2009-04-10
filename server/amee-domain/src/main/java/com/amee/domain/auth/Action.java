package com.amee.domain.auth;

import com.amee.domain.AMEEEntity;
import com.amee.domain.APIUtils;
import com.amee.domain.site.App;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;

/**
 * Actions...
 * <p/>
 * Actions belong to an App.
 * <p/>
 * When deleting an Action we need to ensure the Action is removed from relevant Roles.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "ACTION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Action extends AMEEEntity implements Comparable {

    public final static int NAME_SIZE = 100;
    public final static int DESCRIPTION_SIZE = 1000;
    public final static int ACTION_KEY_SIZE = 100;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "APP_ID")
    private App app;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    @Column(name = "ACTION_KEY", length = ACTION_KEY_SIZE, nullable = false)
    @Index(name = "ACTION_KEY_IND")
    private String key = ""; // TODO: validate to be in right format

    public Action() {
        super();
    }

    public Action(App app) {
        this();
        setApp(app);
        app.getActions().add(this);
    }

    public Action(App app, String name, String key) {
        this(app);
        setName(name);
        setKey(key);
    }

    public String toString() {
        return "Action_" + getUid();
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Action action = (Action) o;
        return getKey().compareToIgnoreCase(action.getKey());
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
        obj.put("key", getKey());
        if (detailed) {
            obj.put("app", getApp().getIdentityJSONObject());
            obj.put("created", getCreated());
            obj.put("modified", getModified());
        }
        return obj;
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = APIUtils.getIdentityJSONObject(this);
        obj.put("key", getKey());
        return obj;
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("Action");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        element.appendChild(APIUtils.getElement(document, "Key", getKey()));
        if (detailed) {
            element.appendChild(getApp().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        Element element = APIUtils.getIdentityElement(document, this);
        element.appendChild(APIUtils.getElement(document, "Key", getKey()));
        return element;
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setDescription(element.elementText("Description"));
        setKey(element.elementText("Key"));
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        if (app != null) {
            this.app = app;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        if (key == null) {
            key = "";
        }
        this.key = key;
    }
}
