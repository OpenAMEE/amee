package com.jellymold.kiwi;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import com.jellymold.utils.domain.DatedObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;

@Entity
@Table(name = "TARGET")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Target implements DatedObject, Comparable, Serializable {

    public final static int NAME_SIZE = 100;
    public final static int DESCRIPTION_SIZE = 1000;
    public final static int URI_PATTERN_SIZE = 1000;
    public final static int TARGET_SIZE = 1000;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "APP_ID")
    private App app;

    @Column(name = "TYPE")
    private TargetType type = TargetType.SEAM_COMPONENT_RESOURCE;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    @Column(name = "URI_PATTERN", length = URI_PATTERN_SIZE, nullable = false)
    private String uriPattern = "";

    @Column(name = "TARGET", length = TARGET_SIZE, nullable = false)
    private String target = "";

    @Column(name = "DEFAULT_TARGET")
    private boolean defaultTarget = false;

    @Column(name = "ENABLED")
    private boolean enabled = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public Target() {
        super();
        setUid(UidGen.getUid());
    }

    public Target(App app) {
        this();
        setApp(app);
    }

    public Target(String name, String target, String uriPattern) {
        this();
        setName(name);
        setTarget(target);
        setUriPattern(uriPattern);
    }

    public Target(String name, String target, String uriPattern, boolean defaultTarget) {
        this(name, target, uriPattern);
        setDefaultTarget(defaultTarget);
    }

    public Target(String name, String target, String uriPattern, boolean defaultTarget, boolean directoryTarget) {
        this(name, target, uriPattern, defaultTarget);
        if (directoryTarget) {
            setType(TargetType.DIRECTORY_RESOURCE);
        }
    }

    public String toString() {
        return "Target_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Target)) return false;
        Target target = (Target) o;
        return getUriPattern().equalsIgnoreCase(target.getUriPattern());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Target target = (Target) o;
        return getUriPattern().compareToIgnoreCase(target.getUriPattern());
    }

    public int hashCode() {
        return getUriPattern().toLowerCase().hashCode();
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("type", getType());
        obj.put("name", getName());
        obj.put("description", getDescription());
        obj.put("uriPattern", getUriPattern());
        obj.put("target", getTarget());
        obj.put("defaultTarget", isDefaultTarget());
        obj.put("enabled", isEnabled());
        if (detailed) {
            obj.put("app", getApp().getIdentityJSONObject());
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
        Element element = document.createElement("Target");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Type", getType().getName()));
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        element.appendChild(APIUtils.getElement(document, "UriPattern", getUriPattern()));
        element.appendChild(APIUtils.getElement(document, "Target", getTarget()));
        element.appendChild(APIUtils.getElement(document, "DefaultTarget", "" + isDefaultTarget()));
        element.appendChild(APIUtils.getElement(document, "Enabled", "" + isEnabled()));
        if (detailed) {
            element.appendChild(getApp().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setDescription(element.elementText("Description"));
        setUriPattern(element.elementText("UriPattern"));
        setTarget(element.elementText("Target"));
        setDefaultTarget(element.elementText("DefaultTarget"));
        setEnabled(element.elementText("Enabled"));
        setType(TargetType.valueOf(element.elementText("Type")));
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
        if (uid != null) {
            this.uid = uid;
        }
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        if (app != null) {
            this.app = app;
        }
    }

    public TargetType getType() {
        return type;
    }

    public void setType(TargetType type) {
        this.type = type;
    }

    public void setContentType(String name) {
        if (name != null) {
            try {
                setType(TargetType.valueOf(name));
            } catch (IllegalArgumentException e) {
                // swallow
            }
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

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        if (uriPattern == null) {
            uriPattern = "";
        }
        this.uriPattern = uriPattern;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        if (target == null) {
            target = "";
        }
        this.target = target;
    }

    public boolean isDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(boolean defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public void setDefaultTarget(String defaultTarget) {
        try {
            setDefaultTarget(Boolean.parseBoolean(defaultTarget));
        } catch (NumberFormatException e) {
            // swallow
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnabled(String enabled) {
        try {
            setEnabled(Boolean.parseBoolean(enabled));
        } catch (NumberFormatException e) {
            // swallow
        }
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