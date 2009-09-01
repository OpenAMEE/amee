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

import com.amee.core.APIUtils;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.ObjectType;
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.auth.User;
import com.amee.domain.auth.Permission;
import com.amee.domain.path.Pathable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PROFILE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Profile extends AMEEEnvironmentEntity implements Pathable {

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "PATH")
    private String path = "";

    @Column(name = "NAME")
    private String name = "";

    public Profile() {
        super();
    }

    public Profile(User user) {
        super(user.getEnvironment());
        setUser(user);
    }

    public String toString() {
        return "Profile_" + getUid();
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("path", getDisplayPath());
        obj.put("name", getDisplayName());
        if (detailed) {
            obj.put("created", getCreated().toString());
            obj.put("modified", getModified().toString());
            obj.put("user", getUser().getIdentityJSONObject());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
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
        Element element = document.createElement("Profile");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Path", getPath()));
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(getUser().getIdentityElement(document));
            element.appendChild(getEnvironment().getIdentityElement(document));
        }
        return element;
    }

    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }


    /**
     * Add 'built-in' Permission to this Profile, such that the associated User owns the Profile.
     *
     * @param permissions the Permissions List to modify
     */
    protected void addBuiltInPermissions(List<Permission> permissions) {
        Permission permission = new Permission();
        permission.setEntityReference(new AMEEEntityReference(this));
        permission.setPrincipleReference(new AMEEEntityReference(getUser()));
        permission.addEntry(Permission.OWN);
        permissions.add(permission);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }
        this.user = user;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            path = "";
        }
        this.path = path;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.PR;
    }

    @Override
    public String getDisplayPath() {
        if (getPath().length() > 0) {
            return getPath();
        } else {
            return getUid();
        }
    }

    @Override
    public String getDisplayName() {
        if (getName().length() > 0) {
            return getName();
        } else {
            return getDisplayPath();
        }
    }
}