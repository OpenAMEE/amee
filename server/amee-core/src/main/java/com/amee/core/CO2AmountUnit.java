package com.amee.core;

import com.amee.platform.science.*;
import org.apache.commons.lang.StringUtils;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

/**
 * The unit of a CO2 amount calculated by AMEE.
 *
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
public class CO2AmountUnit extends AmountCompoundUnit {

    // The default unit
    private static final AmountUnit UNIT = new AmountUnit(SI.KILOGRAM);

    // The default perUnit
    private static final AmountPerUnit PER_UNIT = new AmountPerUnit(NonSI.YEAR);

    // The default compound unit (i.e. unit/perUnit)
    public static final CO2AmountUnit DEFAULT = new CO2AmountUnit(UNIT, PER_UNIT);

    public CO2AmountUnit(AmountUnit unit, AmountPerUnit perUnit) {
        super(unit, perUnit);
    }

    public CO2AmountUnit(String unit, String perUnit) {
        super(parseUnit(unit), parsePerUnit(perUnit));
    }

    private static AmountUnit parseUnit(String unit) {
        return StringUtils.isNotBlank(unit) ? AmountUnit.valueOf(unit) : UNIT;
    }

    private static AmountPerUnit parsePerUnit(String perUnit) {
        return StringUtils.isNotBlank(perUnit) ? AmountPerUnit.valueOf(perUnit) : PER_UNIT;
    }

    /**
     * Does this instance represent an external unit.
     *
     * @return true if the current instance represents the default unit for a CO2 amount calculated by AMEE
     */
    public boolean isExternal() {
        return !this.equals(DEFAULT);
    }

    /**
     * Does the supplied AmountPerUnit represent an external unit.
     * @param perUnit
     * @return true if the current instance represents the default AmountPerUnit for a CO2 amount calculated by AMEE
     */
    public static boolean isExternal(AmountPerUnit perUnit) {
        return !PER_UNIT.equals(perUnit);
    }

    /**
     * Does the supplied AmountUnit represent an external unit.
     * @param unit
     * @return true if the current instance represents the default AmountUnit for a CO2 amount calculated by AMEE
     */
    public static boolean isExternal(AmountUnit unit) {
        return !UNIT.equals(unit);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

