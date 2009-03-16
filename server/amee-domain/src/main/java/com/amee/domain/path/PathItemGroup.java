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

import java.io.Serializable;
import java.util.*;

public class PathItemGroup implements Serializable {

    private Map<String, PathItem> pathItems = new HashMap<String, PathItem>();
    private PathItem rootPathItem = null;
    private String key = "";

    private PathItemGroup() {
        super();
    }

    public PathItemGroup(PathItem rootPathItem) {
        this();
        setRootPathItem(rootPathItem);
        add(rootPathItem);
    }

    public void add(PathItem pathItem) {
        pathItems.put(pathItem.getUid(), pathItem);
        pathItem.setPathItemGroup(this);
    }

    // Used by DataFinder * ServiceResource.
    public PathItem findByPath(String path) {
        return findBySegments(new ArrayList<String>(Arrays.asList(path.split("/")))); 
    }

    // Used by DataFilter & ProfileFilter.
    public PathItem findBySegments(List<String> segments) {
        PathItem rootDataPathItem = getRootPathItem();
        if (rootDataPathItem != null) {
            if (segments.isEmpty()) {
                return rootDataPathItem;
            } else {
                return rootDataPathItem.findLastPathItem(segments);
            }
        } else {
            return null;
        }
    }

    // Used by BasePIGFactory & ProfilePIGFactory.
    public Map<String, PathItem> getPathItems() {
        return pathItems;
    }

    // Used by ServiceResource.
    public PathItem getRootPathItem() {
        return rootPathItem;
    }

    public void setRootPathItem(PathItem rootPathItem) {
        this.rootPathItem = rootPathItem;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}