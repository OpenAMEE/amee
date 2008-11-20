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
package gc.carbon.domain.profile;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.Permission;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.UidGen;
import gc.carbon.domain.EngineUtils;
import gc.carbon.domain.ObjectType;
import gc.carbon.domain.path.Pathable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.jboss.seam.annotations.Name;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

// TODO: reference to user, if relevant

@Entity
@Table(name = "PROFILE")
@Name("profile")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Profile implements Serializable, Pathable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

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
        setUid(UidGen.getUid());
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

    @Transient
    public String getDisplayPath() {
        return EngineUtils.getDisplayPath(this);
    }

    @Transient
    public String getDisplayName() {
        return EngineUtils.getDisplayName(this);
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
        if (uid == null) {
            uid = "";
        }
        this.uid = uid;
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
}