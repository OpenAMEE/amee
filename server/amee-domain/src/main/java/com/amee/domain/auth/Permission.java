package com.amee.domain.auth;

import com.amee.domain.*;
import com.amee.domain.environment.Environment;
import com.amee.domain.environment.EnvironmentObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;


/**
 * Permission.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "PERMISSION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Permission implements EnvironmentObject, DatedObject, Comparable, Serializable {

    public final static int OBJECT_CLASS_SIZE = 255;
    public final static int OBJECT_UID_SIZE = 12;

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
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "OBJECT_CLASS", length = OBJECT_CLASS_SIZE, nullable = false)
    @Index(name = "OBJECT_CLASS_IND")
    private String objectClass = "";

    @Column(name = "OBJECT_UID", nullable = false, length = OBJECT_UID_SIZE)
    @Index(name = "OBJECT_UID_IND")
    private String objectUid = "";

    @Column(name = "GROUP_ALLOW_VIEW")
    private boolean groupAllowView = false;

    @Column(name = "GROUP_ALLOW_MODIFY")
    private boolean groupAllowModify = false;

    @Column(name = "OTHER_ALLOW_VIEW")
    private boolean otherAllowView = false;

    @Column(name = "OTHER_ALLOW_MODIFY")
    private boolean otherAllowModify = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public Permission() {
        super();
        setUid(UidGen.getUid());
    }

    public Permission(Group group, User user) {
        this();
        setEnvironment(group.getEnvironment());
        setGroup(group);
        setUser(user);
    }

    public Permission(Group group, User user, PersistentObject persistentObject) {
        this(group, user);
        setObject(persistentObject);
    }

    public void setObject(PersistentObject persistentObject) {
        setObjectClass(persistentObject.getClass().getName());
        setObjectUid(persistentObject.getUid());
    }

    public String toString() {
        return "Permission_" + getUid();
    }

    // TODO: do this properly
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission permission = (Permission) o;
        return getGroup().equals(permission.getGroup()) &&
                getUser().equals(permission.getUser());
    }

    // TODO: do this properly
    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Permission permission = (Permission) o;
        int result = getEnvironment().compareTo(permission.getEnvironment());
        if (result == 0) {
            result = getGroup().compareTo(permission.getGroup());
            if (result == 0) {
                result = getUser().compareTo(permission.getUser());
            }
        }
        return result;
    }

    public int hashCode() {
        return getUid().hashCode();
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("created", getCreated());
        obj.put("modified", getModified());
        obj.put("environmentUid", getEnvironment().getUid());
        JSONObject groupObj = new JSONObject();
        groupObj.put("uid", getGroup().getUid());
        groupObj.put("name", getGroup().getName());
        obj.put("group", groupObj);
        JSONObject userObj = new JSONObject();
        userObj.put("uid", getUser().getUid());
        userObj.put("username", getUser().getUsername());
        obj.put("auth", userObj);
        // TODO: addItemValue flags
        return obj;
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return getJSONObject();
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    @Transient
    public Element getElement(Document document) {
        Element element = document.createElement("Permission");
        element.setAttribute("uid", getUid());
        element.setAttribute("created", getCreated().toString());
        element.setAttribute("modified", getModified().toString());
        element.appendChild(getEnvironment().getIdentityElement(document));
        element.appendChild(getGroup().getIdentityElement(document)
                .appendChild(APIUtils.getElement(document, "Name", getGroup().getName())));
        element.appendChild(getUser().getIdentityElement(document)
                .appendChild(APIUtils.getElement(document, "Username", getUser().getUsername())));
        // TODO: addItemValue flags
        return element;
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        return getElement(document);
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (group != null) {
            this.group = group;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null) {
            this.user = user;
        }
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        if (objectClass == null) {
            objectClass = "";
        }
        this.objectClass = objectClass;
    }

    public String getObjectUid() {
        return objectUid;
    }

    public void setObjectUid(String objectUid) {
        if (objectUid == null) {
            objectUid = "";
        }
        this.objectUid = objectUid;
    }

    public boolean isGroupAllowView() {
        return groupAllowView;
    }

    public void setGroupAllowView(boolean groupAllowView) {
        this.groupAllowView = groupAllowView;
    }

    public boolean isGroupAllowModify() {
        return groupAllowModify;
    }

    public void setGroupAllowModify(boolean groupAllowModify) {
        this.groupAllowModify = groupAllowModify;
    }

    public boolean isOtherAllowView() {
        return otherAllowView;
    }

    public void setOtherAllowView(boolean otherAllowView) {
        this.otherAllowView = otherAllowView;
    }

    public boolean isOtherAllowModify() {
        return otherAllowModify;
    }

    public void setOtherAllowModify(boolean otherAllowModify) {
        this.otherAllowModify = otherAllowModify;
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

    public APIVersion getAPIVersion() {
        return user.getAPIVersion();
    }
}
