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
package net.dgen.amee.client.model.data;

import net.dgen.amee.client.model.base.AmeeItem;
import net.dgen.amee.client.model.base.AmeeObject;
import net.dgen.amee.client.model.base.AmeeObjectReference;
import net.dgen.amee.client.model.base.AmeeObjectType;

import java.io.Serializable;

public class AmeeDataItem extends AmeeItem implements Serializable {

    public AmeeDataItem() {
        super();
    }

    public AmeeDataItem(AmeeObjectReference ref) {
        super(ref);
    }

    public AmeeDataItem(String path, AmeeObjectType objectType) {
        super(path, objectType);
    }

    public void populate(AmeeDataItem copy) {
        super.populate(copy);
    }

    public AmeeObject getCopy() {
        AmeeDataItem copy = new AmeeDataItem();
        populate(copy);
        return copy;
    }

    public void setParentRef() {
        setParentRef(new AmeeObjectReference(getObjectReference().getParentUri(), AmeeObjectType.DATA_CATEGORY));
    }
}
