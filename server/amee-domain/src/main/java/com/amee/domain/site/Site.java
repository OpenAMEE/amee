package com.amee.domain.site;

import com.amee.domain.AMEEEntity;
import com.amee.core.APIUtils;
import com.amee.domain.DatedObject;
import com.amee.domain.environment.Environment;
import com.amee.domain.environment.EnvironmentObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A Site represents a single web site consisting of a collection of Apps.
 * <p/>
 * Sites are joined to Apps via SiteApps.
 * <p/>
 * Sites belong to a Environment.
 * <p/>
 * When deleting a Site we need to ensure all SiteApps are also removed.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "SITE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Site extends AMEEEntity implements EnvironmentObject, DatedObject, Comparable, Serializable {

    public final static int NAME_SIZE = 100;
    public final static int DESCRIPTION_SIZE = 1000;
    public final static int AUTH_COOKIE_DOMAIN_SIZE = 255;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    @Column(name = "SECURE_AVAILABLE")
    private boolean secureAvailable = false;

    @Column(name = "CHECK_REMOTE_ADDRESS")
    private boolean checkRemoteAddress = false;

    @Column(name = "AUTH_COOKIE_DOMAIN", length = AUTH_COOKIE_DOMAIN_SIZE, nullable = false)
    private String authCookieDomain = "";

    @Column(name = "MAX_AUTH_DURATION", nullable = false)
    private Long maxAuthDuration = -1L;

    @Column(name = "MAX_AUTH_IDLE", nullable = false)
    private Long maxAuthIdle = -1L;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<SiteApp> siteApps = new HashSet<SiteApp>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public Site() {
        super();
    }

    public Site(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public void add(SiteApp siteApp) {
        siteApp.setSite(this);
        getSiteApps().add(siteApp);
    }

    public void remove(SiteApp siteApp) {
        getSiteApps().remove(siteApp);
    }

    public String toString() {
        return "Site_" + getUid();
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Site site = (Site) o;
        return getName().compareToIgnoreCase(site.getName());
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