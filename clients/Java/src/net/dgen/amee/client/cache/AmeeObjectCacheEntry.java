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
package net.dgen.amee.client.cache;

import net.dgen.amee.client.model.base.AmeeObject;
import net.dgen.amee.client.model.base.AmeeObjectReference;

import java.io.Serializable;

public class AmeeObjectCacheEntry implements Serializable, Comparable {

    private AmeeObjectReference objectReference = null;
    private AmeeObject object = null;

    private AmeeObjectCacheEntry() {
        super();
    }

    public AmeeObjectCacheEntry(AmeeObjectReference ref, AmeeObject object) {
        this();
        setObjectReference(ref);
        setObject(object);
    }

    public boolean equals(Object o) {
        return getObjectReference().equals(o);
    }

    public int compareTo(Object o) {
        return getObjectReference().compareTo(o);
    }

    public int hashCode() {
        return getObjectReference().hashCode();
    }

    public String toString() {
        return getObjectReference().toString();
    }

    public AmeeObjectReference getObjectReference() {
        return objectReference;
    }

    public void setObjectReference(AmeeObjectReference ref) {
        if (ref != null) {
            this.objectReference = ref;
        }
    }

    public AmeeObject getObject() {
        return object;
    }

    public void setObject(AmeeObject object) {
        if (object != null) {
            this.object = object;
        }
    }
}