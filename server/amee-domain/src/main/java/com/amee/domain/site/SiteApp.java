package com.amee.domain.site;

import com.amee.core.APIUtils;
import com.amee.domain.AMEEEnvironmentEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;

@Entity
@Table(name = "SITE_APP")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SiteApp extends AMEEEnvironmentEntity {

    public final static int SKIN_PATH_SIZE = 255;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "APP_ID")
    private App app;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SITE_ID")
    private Site site;

    @Column(name = "SKIN_PATH", length = SKIN_PATH_SIZE, nullable = false)
    private String skinPath = "";

    public SiteApp() {
        super();
    }

    public SiteApp(App app, Site site) {
        super(site.getEnvironment());
        setApp(app);
        setSite(site);
    }

    public SiteApp(App app, Site site, String skinPath) {
        this(app, site);
        setSkinPath(skinPath);
    }

    public String toString() {
        return "SiteApp_" + getUid();
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        SiteApp siteApp = (SiteApp) o;
        return getUid().compareToIgnoreCase(siteApp.getUid());
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
        obj.put("skinPath", getSkinPath());
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
        element.appendChild(APIUtils.getElement(document, "SkinPath", getSkinPath()));
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
        setSkinPath(element.elementText("SkinPath"));
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

    public String getSkinPath() {
        return skinPath;
    }

    public void setSkinPath(String skinPath) {
        if (skinPath == null) {
            skinPath = "";
        }
        this.skinPath = skinPath;
    }
}