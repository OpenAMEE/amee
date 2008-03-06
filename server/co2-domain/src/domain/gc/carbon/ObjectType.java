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
package gc.carbon;

import gc.carbon.data.Algorithm;
import gc.carbon.data.DataCategory;
import gc.carbon.data.DataItem;
import gc.carbon.data.ItemDefinition;
import gc.carbon.data.ItemValue;
import gc.carbon.data.ItemValueDefinition;
import gc.carbon.profile.Profile;
import gc.carbon.profile.ProfileItem;

import java.io.Serializable;

public enum ObjectType implements Serializable {

    DC, AL, ID, IVD, DI, PI, IV, PR;

    private String[] names = {
            "DC",
            "AL",
            "ID",
            "IVD",
            "DI",
            "PI",
            "IV",
            "PR"};

    private String[] labels = {
            "DataCategory",
            "Algorithm",
            "ItemDefinition",
            "ItemValueDefinition",
            "DataItem",
            "ProfileItem",
            "ItemValue",
            "Profile"};

    private Class[] clazzes = {
            DataCategory.class,
            Algorithm.class,
            ItemDefinition.class,
            ItemValueDefinition.class,
            DataItem.class,
            ProfileItem.class,
            ItemValue.class,
            Profile.class};

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
