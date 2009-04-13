package com.amee.domain.auth;

import com.amee.domain.AMEEEntity;
import com.amee.core.APIUtils;
import com.amee.domain.DatedObject;
import com.amee.domain.environment.Environment;
import com.amee.domain.environment.EnvironmentObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONArray;
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
 * A GroupUser joins Users to Groups. Users are assigned Roles within a Group via GroupUsers.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "GROUP_USER")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GroupUser extends AMEEEntity implements EnvironmentObject, DatedObject, Comparable, Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "GROUP_USER_ROLE",
            joinColumns = {@JoinColumn(name = "GROUP_USER_ID")},
            inverseJoinColumns = {@JoinColumn(name = "ROLE_ID")}
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("name")
    private Set<Role> roles = new HashSet<Role>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public GroupUser() {
        super();
    }

    public GroupUser(Group group, User user) {
        this();
        setEnvironment(group.getEnvironment());
        setGroup(group);
        setUser(user);
    }

    public void add(Role role) {
        getRoles().add(role);
    }

    public String toString() {
        return "GroupUser_" + getUid();
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        GroupUser groupUser = (GroupUser) o;
        return getUid().compareTo(groupUser.getUid());
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("group", getGroup().getIdentityJSONObject());
        obj.put("user", getUser().getIdentityJSONObject());
        if (detailed) {
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            JSONArray rolesArr = new JSONArray();
            for (Role role : getRoles()) {
                rolesArr.put(role.getJSONObject());
            }
            obj.put("roles", rolesArr);
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
        Element element = document.createElement("GroupUser");
        element.setAttribute("uid", getUid());
        element.appendChild(getGroup().getIdentityElement(document));
        element.appendChild(getUser().getIdentityElement(document));
        if (detailed) {
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            Element rolesElement = document.createElement("Roles");
            for (Role role : getRoles()) {
                rolesElement.appendChild(role.getIdentityElement(document));
            }
            element.appendChild(rolesElement);
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
    }

    @Transient
    public boolean hasRole(String role) {
        if (getRoles() == null) return false;
        for (Role r : getRoles()) {
            if (r.getName().compareToIgnoreCase(role) == 0) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean hasRoles(String roles) {
        boolean result = false;
        if (roles != null) {
            String[] rolesArray = roles.split(",");
            for (String role : rolesArray) {
                if (!hasRole(role.trim())) {
                    return false;
                } else {
                    // will only return true if;
                    //      at least one role was found
                    //      and all roles were found
                    result = true;
                }
            }
        }
        return result;
    }

    @Transient
    public boolean hasAction(String action) {
        if (getRoles() == null) return false;
        for (Role r : getRoles()) {
            if (r.hasAction(action)) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean hasActions(String actions) {
        boolean result = false;
        if (actions != null) {
            String[] actionsArray = actions.split(",");
            for (String action : actionsArray) {
                if (!hasAction(action.trim())) {
                    return false;
                } else {
                    // will only return true if;
                    //      at least one action was found
                    //      and all actions were found
                    result = true;
                }
            }
        }
        return result;
    }

    @Transient
    public void addRole(Role role) {
        getRoles().add(role);
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        if (roles == null) {
            roles = new HashSet<Role>();
        }
        this.roles = roles;
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