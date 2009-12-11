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

    public void addAll(Collection<PathItem> pathItems) {
        for (PathItem pathItem : pathItems) {
            add(pathItem);
        }
    }

    // Used by DataFinder.
    public PathItem findByPath(String path, boolean forProfile) {
        return findBySegments(new ArrayList<String>(Arrays.asList(path.split("/"))), forProfile);
    }

    // Used by DataFilter & ProfileFilter.
    public PathItem findBySegments(List<String> segments, boolean forProfile) {
        PathItem rootPathItem = getRootPathItem();
        if (rootPathItem != null) {
            if (segments.isEmpty()) {
                return rootPathItem;
            } else {
                return rootPathItem.findLastPathItem(segments, forProfile);
            }
        } else {
            return null;
        }
    }

    /**
     * Return the {@link PathItem} corresponding to the passed uid.
     *
     * @param uid
     * @return the {@link PathItem} corresponding to the passed uid. NULL will be returned if there exists no
     * {@link PathItem} for the passed uid. 
     */
    public PathItem findByUId(String uid) {
        return pathItems.get(uid);
    }

    // Used by DataFilter.
    public PathItem getRootPathItem() {
        return rootPathItem;
    }

    public void setRootPathItem(PathItem rootPathItem) {
        this.rootPathItem = rootPathItem;
    }
}