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
/**
 * This file is part of AMEE Java Client Library.
 *
 * AMEE Java Client Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE Java Client Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dgen.amee.client.model.base;

import net.dgen.amee.client.util.UriUtils;

import java.io.Serializable;

// TODO: deal with query strings too

public class AmeeObjectReference implements Serializable, Comparable {

    private String uri = "";
    private AmeeObjectType objectType = AmeeObjectType.UNKNOWN;

    private AmeeObjectReference() {
        super();
    }

    public AmeeObjectReference(String path, AmeeObjectType objectType) {
        this();
        setUri(path);
        setObjectType(objectType);
    }

    public boolean equals(Object o) {
        AmeeObjectReference other = (AmeeObjectReference) o;
        return getUri().equalsIgnoreCase(other.getUri());
    }

    public int compareTo(Object o) {
        AmeeObjectReference other = (AmeeObjectReference) o;
        return getUri().compareToIgnoreCase(other.getUri());
    }

    public int hashCode() {
        return getUri().toLowerCase().hashCode();
    }

    public String toString() {
        return getUri();
    }

    public boolean isRoot() {
        return getUri().length() == 0;
    }

    public String getPath() {
        String path = getUri();
        int pos = path.indexOf("?");
        if (pos >= 0) {
            path = path.substring(0, pos);
        }
        return path;
    }

    public String getLocalPart() {
        return UriUtils.getLastPart(getUri());
    }

    public String getParentUri() {
        return UriUtils.getParentUri(getUri());
    }

    public String getUriExceptFirstPart() {
        return UriUtils.getUriExceptXParts(getUri(), 1);
    }

    public String getUriExceptFirstTwoParts() {
        return UriUtils.getUriExceptXParts(getUri(), 2);
    }

    public String getUriFirstTwoParts() {
        return UriUtils.getUriFirstTwoParts(getUri());
    }

    public AmeeObjectReference getParent(AmeeObjectType objectType) {
        return new AmeeObjectReference(getParentUri(), objectType);
    }

    public AmeeObjectReference getChild(String localPath, AmeeObjectType objectType) {
        return new AmeeObjectReference(getParentUri() + "/" + localPath, objectType);
    }

    public String getUri() {
        return uri;
    }

    private void setUri(String uri) {
        if (uri != null) {
            // ensure uri does not end with a '/'
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
            // ensure uri does not start with a '/'
            if (uri.startsWith("/")) {
                if (uri.length() > 1) {
                    uri = uri.substring(1);
                } else {
                    uri = "";
                }
            }
        }
        this.uri = uri;
    }

    public AmeeObjectType getObjectType() {
        return objectType;
    }

    private void setObjectType(AmeeObjectType objectType) {
        if (objectType != null) {
            this.objectType = objectType;
        }
    }
}