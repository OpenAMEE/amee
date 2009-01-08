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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

// TODO: add an enabled flag

@Entity
@Table(name = "SITE_ALIAS")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SiteAlias implements EnvironmentObject, DatedObject, Comparable, Serializable {

    public final static int NAME_SIZE = 250;
    public final static int DESCRIPTION_SIZE = 1000;
    public final static int SERVER_ALIAS_SIZE = 250;

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
    @JoinColumn(name = "SITE_ID")
    private Site site;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    @Column(name = "SERVER_ALIAS", length = SERVER_ALIAS_SIZE, nullable = false)
    @Index(name = "SERVER_ALIAS_IND")
    private String serverAlias = "";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;


    public SiteAlias() {
        super();
        setUid(UidGen.getUid());
    }

    public SiteAlias(Site site) {
        this();
        setEnvironment(site.getEnvironment());
        setSite(site);
    }

    public SiteAlias(Site site, String name, String serverAlias) {
        this(site);
        setName(name);
        setServerAlias(serverAlias);
    }

    public String toString() {
        return "SiteAlias_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiteAlias)) return false;
        SiteAlias siteAlias = (SiteAlias) o;
        return getServerAlias().equalsIgnoreCase(siteAlias.getServerAlias());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        SiteAlias siteAlias = (SiteAlias) o;
        return getServerAlias().compareToIgnoreCase(siteAlias.getServerAlias());
    }

    public int hashCode() {
        return getServerAlias().toLowerCase().hashCode();
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
        obj.put("serverAlias", getServerAlias());
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
        Element element = document.createElement("SiteAlias");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        element.appendChild(APIUtils.getElement(document, "ServerAlias", getServerAlias()));
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
        setName(element.elementText("Name"));
        setDescription(element.elementText("Description"));
        setServerAlias(element.elementText("ServerAlias"));
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

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        if (site != null) {
            this.site = site;
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

    public String getServerAlias() {
        return serverAlias;
    }

    public void setServerAlias(String serverAlias) {
        if (serverAlias == null) {
            serverAlias = "";
        }
        this.serverAlias = serverAlias;
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