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
package com.amee.service.path;

import com.amee.domain.data.DataCategory;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BasePIGFactory implements Serializable {

    protected DataCategory findRootDataCategory(List<DataCategory> dataCategories) {
        Iterator<DataCategory> iterator = dataCategories.iterator();
        while (iterator.hasNext()) {
            DataCategory dataCategory = iterator.next();
            if (dataCategory.getDataCategory() == null) {
                iterator.remove();
                return dataCategory;
            }
        }
        return null;
    }

    protected void addDataCategories(PathItemGroup pathItemGroup, List<DataCategory> dataCategories) {
        PathItem pathItem;
        Map<String, PathItem> pathItems = pathItemGroup.getPathItems();
        Iterator<DataCategory> iterator = dataCategories.iterator();
        while (iterator.hasNext()) {
            DataCategory dataCategory = iterator.next();
            pathItem = pathItems.get(dataCategory.getDataCategory().getUid());
            if (pathItem != null) {
                iterator.remove();
                pathItem.add(new PathItem(dataCategory));
            }
        }
    }
}
