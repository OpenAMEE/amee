package com.jellymold.kiwi;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import com.jellymold.utils.domain.DatedObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A Site represents a single web site consisting of a collection of Apps.
 * <p/>
 * A Site can be alaised by SiteAliases.
 * <p/>
 * Sites are joined to Apps via SiteApps.
 * <p/>
 * Sites belong to a Environment.
 * <p/>
 * When deleting a Site we need to ensure all SiteAliases and SiteApps are also removed.
 * <p/>
 * // TODO: add an enabled flag
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "SITE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Site implements EnvironmentObject, DatedObject, Comparable, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name = "";

    @Column(name = "DESCRIPTION", length = 1000, nullable = false)
    private String description = "";

    @Column(name = "SERVER_NAME", length = 255, nullable = false)
    @Index(name = "SERVER_NAME_IND")
    private String serverName = "";

    @Column(name = "SERVER_ADDRESS", length = 255, nullable = false)
    private String serverAddress = "";

    @Column(name = "SERVER_PORT", length = 255, nullable = false)
    private String serverPort = "";

    @Column(name = "SERVER_SCHEME", length = 255, nullable = false)
    private String serverScheme = "http";

    @Column(name = "SECURE_AVAILABLE")
    private boolean secureAvailable = false;

    @Column(name = "CHECK_REMOTE_ADDRESS")
    private boolean checkRemoteAddress = false;

    @Column(name = "AUTH_COOKIE_DOMAIN", length = 255, nullable = false)
    private String authCookieDomain = "";

    @Column(name = "MAX_AUTH_DURATION", nullable = false)
    private Long maxAuthDuration = -1L;

    @Column(name = "MAX_AUTH_IDLE", nullable = false)
    private Long maxAuthIdle = -1L;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("serverAlias")
    private Set<SiteAlias> siteAliases = new HashSet<SiteAlias>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("uriPattern")
    private Set<SiteApp> siteApps = new HashSet<SiteApp>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public Site() {
        super();
        setUid(UidGen.getUid());
    }

    public Site(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public void add(SiteApp siteApp) {
        siteApp.setSite(this);
        getSiteApps().add(siteApp);
    }

    public void add(SiteAlias siteAlias) {
        siteAlias.setSite(this);
        getSiteAliases().add(siteAlias);
    }

    public void remove(SiteAlias siteAlias) {
        getSiteAliases().remove(siteAlias);
    }

    public void remove(SiteApp siteApp) {
        getSiteApps().remove(siteApp);
    }

    public String toString() {
        return "Site_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Site)) return false;
        Site site = (Site) o;
        return getServerName().equalsIgnoreCase(site.getServerName());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Site site = (Site) o;
        return getServerName().compareToIgnoreCase(site.getServerName());
    }

    public int hashCode() {
        return getServerName().toLowerCase().hashCode();
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
        obj.put("serverName", getServerName());
        obj.put("serverAddress", getServerAddress());
        obj.put("serverPort", getServerPort());
        obj.put("serverScheme", getServerScheme());
        obj.put("secureAvailable", isSecureAvailable());
        obj.put("checkRemoteAddress", isCheckRemoteAddress());
        obj.put("maxAuthDuration", getMaxAuthDuration());
        obj.put("maxAuthIdle", getMaxAuthIdle());
        if (detailed) {
            obj.put("environment", getEnvironment().getIdentityJSONObject());
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
        Element element = document.createElement("Site");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        element.appendChild(APIUtils.getElement(document, "ServerName", getServerName()));
        element.appendChild(APIUtils.getElement(document, "ServerAddress", getServerAddress()));
        element.appendChild(APIUtils.getElement(document, "ServerPort", getServerPort()));
        element.appendChild(APIUtils.getElement(document, "ServerScheme", getServerScheme()));
        element.appendChild(APIUtils.getElement(document, "SecureAvailable", "" + isSecureAvailable()));
        element.appendChild(APIUtils.getElement(document, "CheckRemoteAddress", "" + isCheckRemoteAddress()));
        element.appendChild(APIUtils.getElement(document, "MaxAuthDuration", "" + getMaxAuthDuration()));
        element.appendChild(APIUtils.getElement(document, "MaxAuthIdle", "" + getMaxAuthIdle()));
        if (detailed) {
            element.appendChild(getEnvironment().getIdentityElement(document));
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
        setServerName(element.elementText("ServerName"));
        setServerAddress(element.elementText("ServerAddress"));
        setServerPort(element.elementText("ServerPort"));
        setServerScheme(element.elementText("ServerScheme"));
        setSecureAvailable(element.elementText("SecureAvailable"));
        setCheckRemoteAddress(element.elementText("CheckRemoteAddress"));
        setMaxAuthDuration(element.elementText("MaxAuthDuration"));
        setMaxAuthIdle(element.elementText("MaxAuthIdle"));
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

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        if (serverName == null) {
            serverName = "";
        }
        this.serverName = serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        if (serverAddress == null) {
            serverAddress = "";
        }
        this.serverAddress = serverAddress;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        if (serverPort == null) {
            serverPort = "";
        }
        this.serverPort = serverPort;
    }

    public String getServerScheme() {
        return serverScheme;
    }

    public void setServerScheme(String serverScheme) {
        if (serverScheme == null) {
            serverScheme = "";
        }
        this.serverScheme = serverScheme;
    }

    public boolean isSecureAvailable() {
        return secureAvailable;
    }

    public void setSecureAvailable(boolean secureAvailable) {
        this.secureAvailable = secureAvailable;
    }

    public void setSecureAvailable(String secureAvailable) {
        setSecureAvailable(Boolean.parseBoolean(secureAvailable));
    }

    public boolean isCheckRemoteAddress() {
        return checkRemoteAddress;
    }

    public void setCheckRemoteAddress(boolean checkRemoteAddress) {
        this.checkRemoteAddress = checkRemoteAddress;
    }

    public void setCheckRemoteAddress(String checkRemoteAddress) {
        setCheckRemoteAddress(Boolean.parseBoolean(checkRemoteAddress));
    }

    public String getAuthCookieDomain() {
        return authCookieDomain;
    }

    public void setAuthCookieDomain(String authCookieDomain) {
        if (authCookieDomain == null) {
            authCookieDomain = "";
        }
        this.authCookieDomain = authCookieDomain;
    }

    public Long getMaxAuthDuration() {
        return maxAuthDuration;
    }

    public void setMaxAuthDuration(Long maxAuthDuration) {
        if ((maxAuthDuration == null) || (maxAuthDuration < 0)) {
            maxAuthDuration = -1L;
        }
        this.maxAuthDuration = maxAuthDuration;
    }

    public void setMaxAuthDuration(String maxAuthDuration) {
        try {
            setMaxAuthDuration(Long.parseLong(maxAuthDuration));
        } catch (NumberFormatException e) {
            // swallow
        }
    }

    public Long getMaxAuthIdle() {
        return maxAuthIdle;
    }

    public void setMaxAuthIdle(Long maxAuthIdle) {
        if ((maxAuthIdle == null) || (maxAuthIdle < 0)) {
            maxAuthIdle = -1L;
        }
        this.maxAuthIdle = maxAuthIdle;
    }

    public void setMaxAuthIdle(String maxAuthIdle) {
        try {
            setMaxAuthIdle(Long.parseLong(maxAuthIdle));
        } catch (NumberFormatException e) {
            // swallow
        }
    }

    public Set<SiteAlias> getSiteAliases() {
        return siteAliases;
    }

    public void setSiteAliases(Set<SiteAlias> siteAliases) {
        if (siteAliases == null) {
            siteAliases = new HashSet<SiteAlias>();
        }
        this.siteAliases = siteAliases;
    }

    public Set<SiteApp> getSiteApps() {
        return siteApps;
    }

    public void setSiteApps(Set<SiteApp> siteApps) {
        if (siteApps == null) {
            siteApps = new HashSet<SiteApp>();
        }
        this.siteApps = siteApps;
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