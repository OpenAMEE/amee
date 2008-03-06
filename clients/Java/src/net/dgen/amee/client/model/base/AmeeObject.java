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

import net.dgen.amee.client.AmeeException;
import net.dgen.amee.client.service.AmeeObjectFactory;

import java.io.Serializable;

public abstract class AmeeObject implements Serializable {

    private String uid = null;
    private String name = null;
    private AmeeObjectReference objectReference = null;
    private boolean fetched = false;

    public AmeeObject() {
        super();
    }

    public AmeeObject(AmeeObjectReference ref) {
        this();
        setObjectReference(ref);
    }

    public AmeeObject(String path, AmeeObjectType objectType) {
        this(new AmeeObjectReference(path, objectType));
    }

    public void populate(AmeeObject copy) {
        copy.setUid(uid);
        copy.setName(name);
        copy.setObjectReference(objectReference);
        copy.setFetched(fetched);
    }

    public abstract AmeeObject getCopy();

    public void fetch() throws AmeeException {
        AmeeObjectFactory.getInstance().fetch(this);
    }

    public void save() throws AmeeException {
        AmeeObjectFactory.getInstance().save(this);
    }

    public void delete() throws AmeeException {
        AmeeObjectFactory.getInstance().delete(this);
    }

    public String getUri() {
        return getObjectReference().getUri();
    }

    public String getLocalPath() {
        return getObjectReference().getLocalPart();
    }

    public String getParentUri() {
        return getObjectReference().getParentUri();
    }

    public AmeeObjectType getObjectType() {
        return getObjectReference().getObjectType();
    }

    public AmeeObjectReference getObjectReference() {
        return objectReference;
    }

    public void setObjectReference(AmeeObjectReference ameeObjectReference) {
        if (ameeObjectReference != null) {
            this.objectReference = ameeObjectReference;
        }
    }

    public String getUid() throws AmeeException {
        if ((uid == null) && !isFetched()) {
            fetch();
        }
        return uid;
    }

    public void setUid(String uid) {
        if (uid != null) {
            this.uid = uid;
        }
    }

    public String getName() throws AmeeException {
        if ((name == null) && !isFetched()) {
            fetch();
        }
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public boolean isFetched() {
        return fetched;
    }

    public void setFetched(boolean fetched) {
        this.fetched = fetched;
    }
}