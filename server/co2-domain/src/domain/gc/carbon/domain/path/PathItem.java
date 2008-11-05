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
package gc.carbon.domain.path;

import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.ObjectType;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PathItem implements APIObject, Serializable, Comparable {

    private PathItemGroup pathItemGroup = null;
    private Long id = 0L;
    private String uid = "";
    private ObjectType objectType = null;
    private String path = "";
    private String name = "";
    private PathItem parent = null;
    private Set<PathItem> children = new TreeSet<PathItem>();

    public PathItem() {
        super();
    }

    public PathItem(Pathable pathable) {
        super();
        setId(pathable.getId());
        setUid(pathable.getUid());
        setObjectType(pathable.getObjectType());
        setPath(pathable.getDisplayPath());
        setName(pathable.getDisplayName());
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("path", getPath());
        return obj;
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return getJSONObject();
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return new JSONObject().put("uid", getUid());
    }

    public Element getElement(Document document) {
        Element element = document.createElement(getObjectType().getLabel());
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Path", getPath()));
        return element;
    }

    public Element getElement(Document document, boolean detailed) {
        return getElement(document);
    }

    public Element getIdentityElement(Document document) {
        Element element = document.createElement(getObjectType().getLabel());
        element.setAttribute("uid", getUid());
        return element;
    }

    public boolean equals(Object o) {
        PathItem other = (PathItem) o;
        return getPath().equalsIgnoreCase(other.getPath());
    }

    public int compareTo(Object o) {
        PathItem other = (PathItem) o;
        return getPath().compareToIgnoreCase(other.getPath());
    }

    public int hashCode() {
        return getPath().toLowerCase().hashCode();
    }

    public String toString() {
        return getPath();
    }

    public void add(PathItem child) {
        children.add(child);
        child.setParent(this);
        getPathItemGroup().add(child);
    }

    public PathItem findLastPathItem(List<String> segments) {
        PathItem result = null;
        PathItem child;
        if (segments.size() > 0) {
            String segment = segments.get(0);
            result = findChildPathItem(segment);
            if (result != null) {
                segments.remove(0);
                if (segments.size() > 0) {
                    child = result.findLastPathItem(segments);
                    if (child != null) {
                        result = child;
                    } else {
                        result = null;
                    }
                }
            }
        }
        return result;
    }

    public PathItem findChildPathItem(String segment) {
        for (PathItem child : getChildren()) {
            if (child.getPath().equalsIgnoreCase(segment)) {
                return child;
            }
        }
        return null;
    }

    public List<PathItem> getPathItems() {
        List<PathItem> pathItems = new ArrayList<PathItem>();
        if (hasParent()) {
            pathItems.addAll(getParent().getPathItems());
        }
        pathItems.add(this);
        return pathItems;
    }

    public List<String> getSegments() {
        String path;
        List<String> segments = new ArrayList<String>();
        for (PathItem pathItem : getPathItems()) {
            path = pathItem.getPath();
            if (path.length() > 0) {
                segments.add(path);
            }
        }
        return segments;
    }

    public String getFullPath() {
        String path = "";
        for (String segment : getSegments()) {
            path = path.concat("/");
            path = path.concat(segment);
        }
        return path;
    }

    public Set<String> findChildUidsByType(String typeName) {
        Set<String> uids = new TreeSet<String>();
        for (PathItem pathItem : getChildrenByType(typeName, true)) {
            uids.add(pathItem.getUid());
        }
        return uids;
    }

    /**
     * For template languages like FreeMarker.
     *
     * @param typeName
     * @return the children
     */
    public Set<PathItem> findChildrenByType(String typeName) {
        return getChildrenByType(typeName);
    }

    public Set<PathItem> getChildrenByType(String typeName) {
        return getChildrenByType(typeName, false);
    }

    public Set<PathItem> getChildrenByType(String typeName, boolean recurse) {
        Set<PathItem> childrenByType = new TreeSet<PathItem>();
        for (PathItem child : getChildren()) {
            if (child.getObjectType().getName().equalsIgnoreCase(typeName)) {
                childrenByType.add(child);
            }
            if (recurse) {
                childrenByType.addAll(child.getChildrenByType(typeName, recurse));
            }
        }
        return childrenByType;
    }

    public String getInternalPath() {
        ObjectType ot = getObjectType();
        if (ot.equals(ObjectType.DC)) {
            return "/categories/" + getUid();
        } else if (ot.equals(ObjectType.DI)) {
            return getParent().getInternalPath() + "/items/" + getUid();
        } else if (ot.equals(ObjectType.PI)) {
            return getParent().getInternalPath() + "/items/" + getUid();
        } else if (ot.equals(ObjectType.IV)) {
            return getParent().getInternalPath() + "/values/" + getUid();
        } else {
            return null; // TODO: or throw an exception
        }
    }

    public PathItemGroup getPathItemGroup() {
        return pathItemGroup;
    }

    public void setPathItemGroup(PathItemGroup pathItemGroup) {
        this.pathItemGroup = pathItemGroup;
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
        this.uid = uid;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasParent() {
        return getParent() != null;
    }

    public PathItem getParent() {
        return parent;
    }

    public void setParent(PathItem parent) {
        this.parent = parent;
    }

    public boolean isChildrenAvailable() {
        return !getChildren().isEmpty();
    }

    public Set<PathItem> getChildren() {
        return children;
    }

    public void setChildren(Set<PathItem> children) {
        this.children = children;
    }
}
