package com.amee.domain.data;

import com.amee.domain.AMEECompoundUnit;
import com.amee.domain.AMEEPerUnit;
import com.amee.domain.AMEEUnit;
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
public class CO2AmountUnit extends AMEECompoundUnit {

    // The default unit
    private static final AMEEUnit UNIT = new AMEEUnit(SI.KILOGRAM);

    // The default perUnit
    private static final AMEEPerUnit PER_UNIT = new AMEEPerUnit(NonSI.YEAR);

    // The default compound unit (i.e. unit/perUnit)
    public static final CO2AmountUnit DEFAULT = new CO2AmountUnit(UNIT, PER_UNIT);

    public CO2AmountUnit(AMEEUnit unit, AMEEPerUnit perUnit) {
        super(unit, perUnit);
    }

    public CO2AmountUnit(String unit, String perUnit) {
        super(parseUnit(unit), parsePerUnit(perUnit));
    }

    private static AMEEUnit parseUnit(String unit) {
        return StringUtils.isNotBlank(unit) ? valueOf(unit) : UNIT;
    }

    private static AMEEPerUnit parsePerUnit(String perUnit) {
        return StringUtils.isNotBlank(perUnit) ? AMEEPerUnit.valueOf(perUnit) : PER_UNIT;
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
     * Does the supplied AMEEPerUnit represent an external unit.
     * @param perUnit
     * @return true if the current instance represents the default AMEEPerUnit for a CO2 amount calculated by AMEE
     */
    public static boolean isExternal(AMEEPerUnit perUnit) {
        return !PER_UNIT.equals(perUnit);
    }

    /**
     * Does the supplied AMEEUnit represent an external unit.
     * @param unit
     * @return true if the current instance represents the default AMEEUnit for a CO2 amount calculated by AMEE
     */
    public static boolean isExternal(AMEEUnit unit) {
        return !UNIT.equals(unit);
    }
}

