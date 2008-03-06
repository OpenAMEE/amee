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

import net.dgen.amee.client.model.data.AmeeDataCategory;
import net.dgen.amee.client.model.data.AmeeDataItem;
import net.dgen.amee.client.model.data.AmeeDrillDown;
import net.dgen.amee.client.model.profile.AmeeProfile;
import net.dgen.amee.client.model.profile.AmeeProfileCategory;
import net.dgen.amee.client.model.profile.AmeeProfileItem;

import java.io.Serializable;

public enum AmeeObjectType implements Serializable {

    DATA_CATEGORY,
    DATA_ITEM,
    DRILL_DOWN,
    PROFILE,
    PROFILE_CATEGORY,
    PROFILE_ITEM,
    UNKNOWN,
    VALUE;

    private String[] names = {
            "DATA_CATEGORY",
            "DATA_ITEM",
            "DRILL_DOWN",
            "PROFILE",
            "PROFILE_CATEGORY",
            "PROFILE_ITEM",
            "UNKNOWN",
            "VALUE"};

    private String[] labels = {
            "Data Category",
            "Data Item",
            "Drill Down",
            "Profile",
            "Profile Category",
            "Profile Item",
            "Unknown",
            "Value"};

    private Class[] clazzes = {
            AmeeDataCategory.class,
            AmeeDataItem.class,
            AmeeDrillDown.class,
            AmeeProfile.class,
            AmeeProfileCategory.class,
            AmeeProfileItem.class,
            Object.class,
            AmeeValue.class};

    public String toString() {
        return getName();
    }

    public String getName() {
        return names[this.ordinal()];
    }

    public String getLabel() {
        return labels[this.ordinal()];
    }

    public Class getClazz() {
        return clazzes[this.ordinal()];
    }
}