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
import net.dgen.amee.client.util.Choice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AmeeItem extends AmeeObject implements Serializable {

    private AmeeObjectReference parentRef = null;
    private String label = null;
    private List<AmeeObjectReference> valueRefs = new ArrayList<AmeeObjectReference>();

    public AmeeItem() {
        super();
    }

    public AmeeItem(AmeeObjectReference ref) {
        super(ref);
    }

    public AmeeItem(String path, AmeeObjectType objectType) {
        super(path, objectType);
    }

    public void populate(AmeeItem copy) {
        super.populate(copy);
        copy.setParentRef(parentRef);
        copy.setLabel(label);
        copy.setValueRefs(new ArrayList<AmeeObjectReference>(valueRefs));
    }

    public abstract void setParentRef();

    public List<AmeeValue> getValues() throws AmeeException {
        AmeeObjectFactory ameeObjectFactory = AmeeObjectFactory.getInstance();
        List<AmeeValue> values = new ArrayList<AmeeValue>();
        AmeeObject ameeObject;
        for (AmeeObjectReference ref : getValueRefs()) {
            ameeObject = ameeObjectFactory.getObject(ref);
            if (ameeObject != null) {
                values.add((AmeeValue) ameeObject);
            }
        }
        return values;
    }

    public void setValues(List<Choice> values) throws AmeeException {
        AmeeObjectFactory.getInstance().setItemValues(this, values);
    }

    public AmeeCategory getParent() throws AmeeException {
        AmeeCategory category = null;
        if (getParentRef() != null) {
            category = (AmeeCategory) AmeeObjectFactory.getInstance().getObject(getParentRef());
        }
        return category;
    }

    public AmeeObjectReference getParentRef() {
        if (parentRef == null) {
            setParentRef();
        }
        return parentRef;
    }

    public void setParentRef(AmeeObjectReference parentRef) {
        if (parentRef != null) {
            this.parentRef = parentRef;
        }
    }

    public AmeeValue getValue(String localPath) throws AmeeException {
        return (AmeeValue) AmeeObjectFactory.getInstance().getObject(getUri() + "/" + localPath, AmeeObjectType.VALUE);
    }

    public String getLabel() throws AmeeException {
        if (!isFetched()) {
            fetch();
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<AmeeObjectReference> getValueRefs() throws AmeeException {
        return getValueRefs(true);
    }

    public List<AmeeObjectReference> getValueRefs(boolean fetchIfNotFetched) throws AmeeException {
        if (fetchIfNotFetched && !isFetched()) {
            fetch();
        }
        return valueRefs;
    }

    public void addValueRef(AmeeObjectReference ref) {
        valueRefs.add(ref);
    }

    public void clearValueRefs() {
        valueRefs.clear();
    }

    public void setValueRefs(List<AmeeObjectReference> valueRefs) {
        if (valueRefs != null) {
            this.valueRefs = valueRefs;
        }
    }
}