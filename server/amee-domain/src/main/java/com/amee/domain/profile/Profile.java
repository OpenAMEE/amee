/**
 * This file is part of AMEE.
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.domain.profile;

import com.amee.core.ObjectType;
import com.amee.domain.AMEEEntity;
import com.amee.domain.APIUtils;
import com.amee.domain.APIVersion;
import com.amee.domain.auth.Permission;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.Pathable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "PROFILE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Profile extends AMEEEntity implements Pathable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "PERMISSION_ID")
    private Permission permission;

    @Column(name = "PATH")
    private String path = "";

    @Column(name = "NAME")
    private String name = "";

    @Column(name = "CREATED")
    @Index(name = "CREATED_IND")
    private Date created = Calendar.getInstance().getTime();

    @Column(name = "MODIFIED")
    @Index(name = "MODIFIED_IND")
    private Date modified = Calendar.getInstance().getTime();

    public Profile() {
        super();
    }

    public Profile(Environment environment, Permission permission) {
        this();
        setEnvironment(environment);
        permission.setObject(this);
        setPermission(permission);
    }

    public String toString() {
        return "Profile_" + getUid();
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("path", getDisplayPath());
        obj.put("name", getDisplayName());
        if (detailed) {
            obj.put("created", getCreated().toString());
            obj.put("modified", getModified().toString());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("permission", getPermission().getJSONObject());
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
        Element element = document.createElement("Profile");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Path", getPath()));
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.appendChild(getPermission().getElement(document));
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    @PrePersist
    public void onCreate() {
        Date now = Calendar.getInstance().getTime();
        setCreated(now);
        setModified(now);
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

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        if (permission != null) {
            this.permission = permission;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            path = "";
        }
        this.path = path;
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

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.PR;
    }

    public APIVersion getAPIVersion() {
        return getPermission().getAPIVersion();
    }

    public String getDisplayPath() {
        if (getPath().length() > 0) {
            return getPath();
        } else {
            return getUid();
        }
    }

    public String getDisplayName() {
        if (getName().length() > 0) {
            return getName();
        } else {
            return getDisplayPath();
        }
    }
}