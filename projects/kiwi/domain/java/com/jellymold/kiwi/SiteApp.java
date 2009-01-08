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

@Entity
@Table(name = "SITE_APP")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SiteApp implements EnvironmentObject, DatedObject {

    public final static int URI_PATTERN_SIZE = 255;
    public final static int SKIN_PATH_SIZE = 255;

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
    @JoinColumn(name = "APP_ID")
    private App app;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SITE_ID")
    private Site site;

    @Column(name = "URI_PATTERN", length = URI_PATTERN_SIZE, nullable = false)
    private String uriPattern = "";

    @Column(name = "SKIN_PATH", length = SKIN_PATH_SIZE, nullable = false)
    private String skinPath = "";

    @Column(name = "DEFAULT_APP")
    private boolean defaultApp = false;

    @Column(name = "ENABLED")
    private boolean enabled = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public SiteApp() {
        super();
        setUid(UidGen.getUid());
    }

    public SiteApp(App app, Site site) {
        this();
        setEnvironment(site.getEnvironment());
        setApp(app);
        setSite(site);
    }

    public SiteApp(App app, Site site, String uriPattern, String skinPath, boolean defaultApp) {
        this(app, site);
        setUriPattern(uriPattern);
        setSkinPath(skinPath);
        setDefaultApp(defaultApp);
    }

    public String toString() {
        return "SiteApp_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiteApp)) return false;
        SiteApp siteApp = (SiteApp) o;
        // TODO: compare on something else too?
        return getUriPattern().equalsIgnoreCase(siteApp.getUriPattern());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        SiteApp siteApp = (SiteApp) o;
        // TODO: compare on something else too?
        return getUriPattern().compareToIgnoreCase(siteApp.getUriPattern());
    }

    public int hashCode() {
        // TODO: compare on something else too?
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
        obj.put("app", getApp().getIdentityJSONObject());
        obj.put("uriPattern", getUriPattern());
        obj.put("skinPath", getSkinPath());
        obj.put("defaultApp", isDefaultApp());
        obj.put("enabled", isEnabled());
        if (detailed) {
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("site", getSite().getIdentityJSONObject());
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
        Element element = document.createElement("SiteApp");
        element.setAttribute("uid", getUid());
        element.appendChild(getApp().getIdentityElement(document));
        element.appendChild(APIUtils.getElement(document, "UriPattern", getUriPattern()));
        element.appendChild(APIUtils.getElement(document, "SkinPath", getSkinPath()));
        element.appendChild(APIUtils.getElement(document, "DefaultApp", "" + isDefaultApp()));
        element.appendChild(APIUtils.getElement(document, "Enabled", "" + isEnabled()));
        if (detailed) {
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.appendChild(getSite().getIdentityElement(document));
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
        setUriPattern(element.elementText("UriPattern"));
        setSkinPath(element.elementText("SkinPath"));
        setDefaultApp(element.elementText("DefaultApp"));
        setEnabled(element.elementText("Enabled"));
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

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        if (environment != null) {
            this.environment = environment;
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

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        if (site != null) {
            this.site = site;
        }
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

    public String getSkinPath() {
        return skinPath;
    }

    public void setSkinPath(String skinPath) {
        if (skinPath == null) {
            skinPath = "";
        }
        this.skinPath = skinPath;
    }

    public boolean isDefaultApp() {
        return defaultApp;
    }

    public void setDefaultApp(boolean defaultApp) {
        this.defaultApp = defaultApp;
    }

    public void setDefaultApp(String defaultApp) {
        setDefaultApp(Boolean.parseBoolean(defaultApp));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnabled(String enabled) {
        setEnabled(Boolean.parseBoolean(enabled));
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