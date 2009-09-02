/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
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
package com.amee.domain;


import com.amee.domain.auth.Group;
import com.amee.domain.auth.User;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;

import java.io.Serializable;

public enum ObjectType implements Serializable {

    DC, AL, ID, IVD, DI, PI, IV, PR, ALC, USR, GRP, ENV, PRM, LN, GP, VD, AV;

    private String[] names = {
            "DC",
            "AL",
            "ID",
            "IVD",
            "DI",
            "PI",
            "IV",
            "PR",
            "ALC",
            "USR",
            "GRP",
            "ENV",
            "PRM",
            "LN",
            "GP",
            "VD",
            "AV"};

    private String[] labels = {
            "DataCategory",
            "Algorithm",
            "ItemDefinition",
            "ItemValueDefinition",
            "DataItem",
            "ProfileItem",
            "ItemValue",
            "Profile",
            "AlgorithmContext",
            "User",
            "Group",
            "Environment",
            "Permission",
            "LocaleName",
            "GroupPrinciple",
            "ValueDefinition",
            "APIVersion"};

    public String toString() {
        return getName();
    }

    public String getName() {
        return names[this.ordinal()];
    }

    public String getLabel() {
        return labels[this.ordinal()];
    }

    public static ObjectType getType(Class c) {
        if (User.class.isAssignableFrom(c)) {
            return USR;
        } else if (Group.class.isAssignableFrom(c)) {
            return GRP;
        } else if (Environment.class.isAssignableFrom(c)) {
            return ENV;
        } else if (Profile.class.isAssignableFrom(c)) {
            return PR;
        } else if (DataCategory.class.isAssignableFrom(c)) {
            return DC;
        } else if (DataItem.class.isAssignableFrom(c)) {
            return DI;
        } else if (ProfileItem.class.isAssignableFrom(c)) {
            return PI;
        } else if (ItemValue.class.isAssignableFrom(c)) {
            return IV;
        }
        throw new IllegalArgumentException("Class not supported.");
    }

    /**
     * Convert the supplied class into a 'real' class. Useful for classes which have been
     * mangeled by Hibernate.
     *
     * @param c class you want to convert to the real class
     * @return the real class, based on the supplied class
     */
    public static Class getClazz(Class c) {
        if (User.class.isAssignableFrom(c)) {
            return User.class;
        } else if (Group.class.isAssignableFrom(c)) {
            return Group.class;
        } else if (Environment.class.isAssignableFrom(c)) {
            return Environment.class;
        } else if (Profile.class.isAssignableFrom(c)) {
            return Profile.class;
        } else if (DataCategory.class.isAssignableFrom(c)) {
            return DataCategory.class;
        } else if (DataItem.class.isAssignableFrom(c)) {
            return DataItem.class;
        } else if (ProfileItem.class.isAssignableFrom(c)) {
            return ProfileItem.class;
        } else if (ItemValue.class.isAssignableFrom(c)) {
            return ItemValue.class;
        }
        throw new IllegalArgumentException("Class not supported.");
    }

    public Class getClazz() {
        if (this.equals(USR)) {
            return User.class;
        } else if (this.equals(GRP)) {
            return Group.class;
        } else if (this.equals(ENV)) {
            return Environment.class;
        } else if (this.equals(PR)) {
            return Profile.class;
        } else if (this.equals(DC)) {
            return DataCategory.class;
        } else if (this.equals(DI)) {
            return DataItem.class;
        } else if (this.equals(PI)) {
            return ProfileItem.class;
        } else if (this.equals(IV)) {
            return ItemValue.class;
        }
        throw new IllegalArgumentException("Class not supported.");
    }
}