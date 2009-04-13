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
package com.amee.domain.path;

import com.amee.core.ObjectType;
import com.amee.domain.APIObject;
import com.amee.core.APIUtils;
import com.amee.domain.UidGen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.*;

public class PathItem implements APIObject, Serializable, Comparable {

    // TODO: store full path in each PathItem (don't work out full path via parents)
    // TODO: more tightly match the AMEE domain model, less abstraction
    // TODO: don't navigate the graph unnecessarilly

    private final Log log = LogFactory.getLog(getClass());

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
        return getElement(document, getObjectType().getLabel());
    }

    public Element getElement(Document document, String name) {
        Element element = document.createElement(name);
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
        return getFullPath().equalsIgnoreCase(other.getFullPath());
    }

    public int compareTo(Object o) {
        PathItem other = (PathItem) o;
        return getFullPath().compareToIgnoreCase(other.getFullPath());
    }

    public int hashCode() {
        return getFullPath().toLowerCase().hashCode();
    }

    public String toString() {
        return getFullPath();
    }

    // Used by BasePIGFactory & ProfilePIGFactory.
    public void add(PathItem child) {
        children.add(child);
        child.setParent(this);
        getPathItemGroup().add(child);
    }

    // Used by PathItemGroup.
    public PathItem findLastPathItem(List<String> segments, boolean forProfile) {
        PathItem result = null;
        PathItem child;
        if (segments.size() > 0) {
            String segment = segments.get(0);
            result = findChildPathItem(segment, forProfile);
            if (result != null) {
                segments.remove(0);
                if (segments.size() > 0) {
                    child = result.findLastPathItem(segments, forProfile);
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

    protected PathItem findChildPathItem(String segment, boolean forProfile) {
        PathItem child = null;
        // find child in the 'persistent' children set
        for (PathItem pi : getChildren()) {
            if (pi.getPath().equalsIgnoreCase(segment)) {
                child = pi;
                break;
            }
        }
        if (child == null) {
            // create 'transient' child if it looks like an item or value
            switch (getObjectType()) {
                case DC:
                    child = new PathItem();
                    child.setObjectType(forProfile ? ObjectType.PI : ObjectType.DI);
                    child.setPath(segment);
                    child.setUid(UidGen.isValid(segment) ? segment : "");
                    child.setParent(this);
                    child.setPathItemGroup(getPathItemGroup());
                    break;
                case DI:
                case PI:
                    child = new PathItem();
                    child.setObjectType(ObjectType.IV);
                    child.setPath(segment);
                    child.setParent(this);
                    child.setPathItemGroup(getPathItemGroup());
                    break;
            }
        }
        return child;
    }

    // used in dataTrail.ftl & profileTrail.ftl
    public List<PathItem> getPathItems() {
        List<PathItem> pathItems = new ArrayList<PathItem>();
        if (hasParent()) {
            pathItems.addAll(getParent().getPathItems());
        }
        pathItems.add(this);
        return pathItems;
    }

    protected List<String> getSegments() {
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

    // Used lots.
    public String getFullPath() {
        String path = "";
        for (String segment : getSegments()) {
            path = path.concat("/");
            path = path.concat(segment);
        }
        return path;
    }

    // Only used by dataCategory.ftl & profileCategory.ftl. FreeMarker needed a distinct method name.
    public Set<PathItem> findChildrenByType(String typeName) {
        return getChildrenByType(typeName);
    }

    // Used by DataCategoryResourceBuilder, BaseProfileResource & ProfileCategoryResourceBuilder.
    public Set<PathItem> getChildrenByType(String typeName) {
        return getChildrenByType(typeName, false);
    }

    // Used by DataCategoryResourceBuilder, BaseProfileResource & ProfileCategoryResourceBuilder.
    protected Set<PathItem> getChildrenByType(String typeName, boolean recurse) {
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

    /**
     * Returns true if this PathItem represents or contains PathItems representing DataCategories (ObjectType.DC) with
     * the supplied IDs. Will optionally search recursivly.
     *
     * @param dataCategoryIds the DataCategory IDs to search for
     * @param recurse         optionally search recursively
     * @return true if this PathItem contains matching PathItems
     */
    public boolean hasDataCategories(Collection<Long> dataCategoryIds, boolean recurse) {
        if (dataCategoryIds.contains(getId())) {
            return true;
        }
        for (PathItem pi : getChildren()) {
            if (pi.getObjectType().equals(ObjectType.DC) && dataCategoryIds.contains(pi.getId())) {
                return true;
            }
            if (recurse && pi.hasDataCategories(dataCategoryIds, recurse)) {
                return true;
            }
        }
        return false;
    }

    // Used internally & by DataFilter, ProfileFilter.
    public String getInternalPath() {
        ObjectType ot = getObjectType();
        if (ot.equals(ObjectType.DC)) {
            return "/categories/" + getUid();
        } else if (ot.equals(ObjectType.DI)) {
            return getParent().getInternalPath() + "/items/" + getPath();
        } else if (ot.equals(ObjectType.PI)) {
            return getParent().getInternalPath() + "/items/" + getUid();
        } else if (ot.equals(ObjectType.IV)) {
            return getParent().getInternalPath() + "/values/" + getPath();
        } else {
            log.error("Unexpected ObjectType.");
            throw new RuntimeException("Unexpected ObjectType.");
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
