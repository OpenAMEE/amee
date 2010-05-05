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


import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.algorithm.AlgorithmContext;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.GroupPrincipal;
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.User;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.data.LocaleName;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;

import java.io.Serializable;

public enum ObjectType implements Serializable {

    DC, AL, ID, IVD, DI, PI, IV, PR, ALC, USR, GRP, ENV, PRM, LN, GP, VD, AV, MD;

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
            "AV",
            "MD"};

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
            "GroupPrincipal",
            "ValueDefinition",
            "APIVersion",
            "Metadata"};

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
        if (DataCategory.class.isAssignableFrom(c)) {
            return DC;
        } else if (Algorithm.class.isAssignableFrom(c)) {
            return AL;
        } else if (ItemDefinition.class.isAssignableFrom(c)) {
            return ID;
        } else if (ItemValueDefinition.class.isAssignableFrom(c)) {
            return IVD;
        } else if (DataItem.class.isAssignableFrom(c)) {
            return DI;
        } else if (ProfileItem.class.isAssignableFrom(c)) {
            return PI;
        } else if (ItemValue.class.isAssignableFrom(c)) {
            return IV;
        } else if (Profile.class.isAssignableFrom(c)) {
            return PR;
        } else if (AlgorithmContext.class.isAssignableFrom(c)) {
            return ALC;
        } else if (User.class.isAssignableFrom(c)) {
            return USR;
        } else if (Group.class.isAssignableFrom(c)) {
            return GRP;
        } else if (Environment.class.isAssignableFrom(c)) {
            return ENV;
        } else if (Permission.class.isAssignableFrom(c)) {
            return PRM;
        } else if (LocaleName.class.isAssignableFrom(c)) {
            return LN;
        } else if (GroupPrincipal.class.isAssignableFrom(c)) {
            return GP;
        } else if (ValueDefinition.class.isAssignableFrom(c)) {
            return VD;
        } else if (APIVersion.class.isAssignableFrom(c)) {
            return AV;
        } else if (Metadata.class.isAssignableFrom(c)) {
            return MD;
        }
        throw new IllegalArgumentException("Class not supported.");
    }

    /**
     * Convert the supplied class into a 'real' class. Useful for classes which have been
     * mangled by Hibernate.
     *
     * @param c class you want to convert to the real class
     * @return the real class, based on the supplied class
     */
    public static Class getClazz(Class c) {
        if (DataCategory.class.isAssignableFrom(c)) {
            return DataCategory.class;
        } else if (Algorithm.class.isAssignableFrom(c)) {
            return Algorithm.class;
        } else if (ItemDefinition.class.isAssignableFrom(c)) {
            return ItemDefinition.class;
        } else if (ItemValueDefinition.class.isAssignableFrom(c)) {
            return ItemValueDefinition.class;
        } else if (DataItem.class.isAssignableFrom(c)) {
            return DataItem.class;
        } else if (ProfileItem.class.isAssignableFrom(c)) {
            return ProfileItem.class;
        } else if (ItemValue.class.isAssignableFrom(c)) {
            return ItemValue.class;
        } else if (Profile.class.isAssignableFrom(c)) {
            return Profile.class;
        } else if (AlgorithmContext.class.isAssignableFrom(c)) {
            return AlgorithmContext.class;
        } else if (User.class.isAssignableFrom(c)) {
            return User.class;
        } else if (Group.class.isAssignableFrom(c)) {
            return Group.class;
        } else if (Environment.class.isAssignableFrom(c)) {
            return Environment.class;
        } else if (Permission.class.isAssignableFrom(c)) {
            return Permission.class;
        } else if (LocaleName.class.isAssignableFrom(c)) {
            return LocaleName.class;
        } else if (GroupPrincipal.class.isAssignableFrom(c)) {
            return GroupPrincipal.class;
        } else if (ValueDefinition.class.isAssignableFrom(c)) {
            return ValueDefinition.class;
        } else if (APIVersion.class.isAssignableFrom(c)) {
            return APIVersion.class;
        } else if (Metadata.class.isAssignableFrom(c)) {
            return Metadata.class;
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
        } else if (this.equals(MD)) {
            return Metadata.class;
        }
        throw new IllegalArgumentException("Class not supported.");
    }
}