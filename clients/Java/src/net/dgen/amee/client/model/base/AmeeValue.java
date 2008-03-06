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

public class AmeeValue extends AmeeObject implements Serializable {

    private AmeeObjectReference itemRef = null;
    private String value = "";

    public AmeeValue() {
        super();
    }

    public AmeeValue(AmeeObjectReference ref) {
        super(ref);
    }

    public AmeeValue(String path, AmeeObjectType objectType) {
        super(path, objectType);
    }

    public void populate(AmeeValue copy) {
        super.populate(copy);
        copy.setItemRef(itemRef);
        copy.setValue(value);
    }

    public AmeeObject getCopy() {
        AmeeValue copy = new AmeeValue();
        populate(copy);
        return copy;
    }

    public AmeeItem getItem() throws AmeeException {
        AmeeItem item = null;
        if (getItemRef() != null) {
            item = (AmeeItem) AmeeObjectFactory.getInstance().getObject(getItemRef());
        }
        return item;
    }

    public AmeeObjectReference getItemRef() {
        return itemRef;
    }

    public void setItemRef(AmeeObjectReference itemRef) {
        if (itemRef != null) {
            this.itemRef = itemRef;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}