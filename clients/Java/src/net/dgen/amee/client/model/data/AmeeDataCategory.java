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

import net.dgen.amee.client.AmeeException;
import net.dgen.amee.client.model.base.AmeeCategory;
import net.dgen.amee.client.model.base.AmeeItem;
import net.dgen.amee.client.model.base.AmeeObject;
import net.dgen.amee.client.model.base.AmeeObjectReference;
import net.dgen.amee.client.model.base.AmeeObjectType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AmeeDataCategory extends AmeeCategory implements Serializable {

    public AmeeDataCategory() {
        super();
    }

    public AmeeDataCategory(AmeeObjectReference ref) {
        super(ref);
    }

    public AmeeDataCategory(String path, AmeeObjectType objectType) {
        super(path, objectType);
    }

    public void populate(AmeeDataCategory copy) {
        super.populate(copy);
    }

    public AmeeObject getCopy() {
        AmeeDataCategory copy = new AmeeDataCategory();
        populate(copy);
        return copy;
    }

    public void setParentRef() {
        if (getUri().equals("data")) {
            setParentRef(null);
        } else {
            setParentRef(new AmeeObjectReference(getObjectReference().getParentUri(), AmeeObjectType.DATA_CATEGORY));
        }
    }

    public AmeeCategory getNewChildCategory(AmeeObjectReference ref) {
        return new AmeeDataCategory(ref);
    }

    public AmeeObjectType getChildCategoryObjectType() {
        return AmeeObjectType.DATA_CATEGORY;
    }

    public AmeeItem getNewChildItem(AmeeObjectReference ref) {
        return new AmeeDataItem(ref);
    }

    public AmeeObjectType getChildItemObjectType() {
        return AmeeObjectType.DATA_ITEM;
    }

    public List<AmeeDataCategory> getDataCategories() throws AmeeException {
        List<AmeeDataCategory> dataCategories = new ArrayList<AmeeDataCategory>();
        for (AmeeCategory category : super.getCategories()) {
            dataCategories.add((AmeeDataCategory) category);
        }
        return dataCategories;
    }

    public List<AmeeDataItem> getDataItems() throws AmeeException {
        List<AmeeDataItem> dataItems = new ArrayList<AmeeDataItem>();
        for (AmeeItem item : super.getItems()) {
            dataItems.add((AmeeDataItem) item);
        }
        return dataItems;
    }
}