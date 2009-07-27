package com.amee.domain.auth;

import com.amee.core.APIUtils;
import com.amee.domain.AMEEEnvironmentEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Collections;
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
public class GroupUser extends AMEEEnvironmentEntity implements Comparable {

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
            inverseJoinColumns = {@JoinColumn(name = "ROLE_ID")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("name")
    private Set<Role> roles = new HashSet<Role>();

    public GroupUser() {
        super();
    }

    public GroupUser(Group group, User user) {
        super(group.getEnvironment());
        setGroup(group);
        setUser(user);
    }

    public void add(Role role) {
        roles.add(role);
    }

    public void remove(Role role) {
        roles.remove(role);
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

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

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

    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

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

    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
    }

    public boolean hasRole(String role) {
        if (getRoles() == null) return false;
        for (Role r : getRoles()) {
            if (r.getName().compareToIgnoreCase(role) == 0) {
                return true;
            }
        }
        return false;
    }

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

    public boolean hasAction(String action) {
        if (getRoles() == null) return false;
        for (Role r : getRoles()) {
            if (r.hasAction(action)) {
                return true;
            }
        }
        return false;
    }

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
        return getActiveRoles();
    }

    public Set<Role> getActiveRoles() {
        Set<Role> activeRoles = new HashSet<Role>();
        for (Role role : roles) {
            if (!role.isTrash()) {
                activeRoles.add(role);
            }
        }
        return Collections.unmodifiableSet(activeRoles);
    }
}