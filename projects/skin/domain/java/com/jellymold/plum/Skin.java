package com.jellymold.plum;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.DatedObject;
import com.jellymold.utils.domain.UidGen;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "SKIN")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Skin implements DatedObject, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "PARENT_ID")
    private Skin parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("path")
    private List<Skin> children = new ArrayList<Skin>();

    @Column(name = "NAME", length = 100, nullable = false)
    private String name = "";

    @Column(name = "PATH", length = 100, nullable = false)
    @Index(name = "PATH_IND")
    private String path = "";

    @Column(name = "DESCRIPTION", length = 1000)
    private String description = "";

    @Column(name = "SVN_URL", length = 255, nullable = false)
    private String svnUrl = "";

    @Column(name = "SVN_USERNAME", length = 255, nullable = false)
    private String svnUsername = "";

    @Column(name = "SVN_PASSWORD", length = 255, nullable = false)
    private String svnPassword = "";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "SKIN_IMPORT",
            joinColumns = {@JoinColumn(name = "SKIN_ID")},
            inverseJoinColumns = {@JoinColumn(name = "IMPORTED_SKIN_ID")}
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Skin> importedSkins = new HashSet<Skin>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public Skin() {
        super();
        setUid(UidGen.getUid());
    }

    public Skin(Skin parent) {
        this();
        setParent(parent);
    }

    @Transient
    public void add(Skin childSkin) {
        getChildren().add(childSkin);
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", getId());
        obj.put("name", getName());
        obj.put("path", getPath());
        if (detailed) {
            // detailed output
            obj.put("parentId", (getParent() != null) ? getParent().getIdentityJSONObject() : 0);
            obj.put("description", getDescription());
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
        Element skinElem = document.createElement("Skin");
        skinElem.setAttribute("uid", getUid());
        skinElem.appendChild(APIUtils.getElement(document, "Name", getName()));
        skinElem.appendChild(APIUtils.getElement(document, "Path", getPath()));
        if (detailed) {
            // parent
            if (getParent() != null) {
                skinElem.appendChild(getParent().getIdentityElement(document, "Parent", true));
            }
            // imported Skins
            Element importedSkinsElem = document.createElement("ImportedSkins");
            for (Skin skin : getImportedSkins()) {
                importedSkinsElem.appendChild(skin.getIdentityElement(document, true));
            }
            skinElem.appendChild(importedSkinsElem);
            skinElem.appendChild(APIUtils.getElement(document, "Description", getDescription()));
            skinElem.appendChild(APIUtils.getElement(document, "SvnUrl", getSvnUrl()));
            skinElem.appendChild(APIUtils.getElement(document, "SvnUsername", getSvnUsername()));
            skinElem.appendChild(APIUtils.getElement(document, "SvnPassword", getSvnPassword()));
            skinElem.setAttribute("created", getCreated().toString());
            skinElem.setAttribute("modified", getModified().toString());
        }
        return skinElem;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return getIdentityElement(document, false);
    }

    @Transient
    public Element getIdentityElement(Document document, boolean detailed) {
        return getIdentityElement(document, "Skin", detailed);
    }

    @Transient
    public Element getIdentityElement(Document document, String name, boolean detailed) {
        Element skinElem = document.createElement(name);
        skinElem.setAttribute("uid", getUid());
        if (detailed) {
            skinElem.appendChild(APIUtils.getElement(document, "Name", getName()));
            skinElem.appendChild(APIUtils.getElement(document, "Path", getPath()));
        }
        return skinElem;
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setPath(element.elementText("Path"));
        setDescription(element.elementText("Description"));
        setSvnUsername(element.elementText("SvnUsername"));
        setSvnPassword(element.elementText("SvnPassword"));
        setSvnUrl(element.elementText("SvnUrl"));
    }

    public String toString() {
        return "Skin_" + getUid();
    }

    public Set<Skin> getImportedSkins() {
        return importedSkins;
    }

    public void setImportedSkins(Set<Skin> importedSkins) {
        if (importedSkins == null) {
            importedSkins = new HashSet<Skin>();
        }
        this.importedSkins = importedSkins;
    }

    public void setImportedSkin(Skin importedSkin) {
        getImportedSkins().add(importedSkin);
    }

    @Transient
    public boolean isParentAvailable() {
        return getParent() != null;
    }

    @Transient
    public boolean isChildrenAvailable() {
        return !getChildren().isEmpty();
    }

    @Transient
    public boolean isSvnReady() {
        return (getSvnUrl().length() > 0) &&
                (getSvnUsername().length() > 0) &&
                (getSvnPassword().length() > 0);
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

    public Skin getParent() {
        return parent;
    }

    public void setParent(Skin parent) {
        this.parent = parent;
    }

    public List<Skin> getChildren() {
        return children;
    }

    public void setChildren(List<Skin> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            path = "";
        }
        this.path = path.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description.trim();
    }

    public String getSvnUrl() {
        return svnUrl;
    }

    public void setSvnUrl(String svnUrl) {
        if (svnUrl == null) {
            svnUrl = "";
        }
        this.svnUrl = svnUrl;
    }

    public String getSvnUsername() {
        return svnUsername;
    }

    public void setSvnUsername(String svnUsername) {
        if (svnUsername == null) {
            svnUsername = "";
        }
        this.svnUsername = svnUsername;
    }

    public String getSvnPassword() {
        return svnPassword;
    }

    public void setSvnPassword(String svnPassword) {
        if (svnPassword == null) {
            svnPassword = "";
        }
        this.svnPassword = svnPassword;
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