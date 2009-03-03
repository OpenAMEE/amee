package com.amee.domain;

import javax.measure.quantity.Power;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/*
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
public class AMEEUnit {

    protected final static UnitFormat UNIT_FORMAT = UnitFormat.getInstance();
    public final static Unit<Power> KILOWATT = SI.WATT.times(1000);
    public final static Unit<? extends Quantity> KILOWATT_PER_HOUR = KILOWATT.times(NonSI.HOUR);
    {
        // Create a usable ASCII representation for kWh. JScience will use non-ASCII characters by default.
        UNIT_FORMAT.label(KILOWATT_PER_HOUR, "kWh");
        UNIT_FORMAT.alias(KILOWATT_PER_HOUR, "kWh");

        // Ensure that "gal" and "oz" are always the US versions.
        // JScience will bizarely default "gal" and "oz" to UK units for UK Locale.
        UNIT_FORMAT.label(NonSI.GALLON_LIQUID_US, "gal");
        UNIT_FORMAT.label(NonSI.OUNCE_LIQUID_US, "oz");
    }

    public static final AMEEUnit ONE = new AMEEUnit(Unit.ONE);
    protected Unit unit = Unit.ONE;

    public AMEEUnit(Unit unit) {
        this.unit = unit;
    }

    public static AMEEUnit valueOf(String unit) {
        return new AMEEUnit(internalValueOf(unit));
    }

    public AMEECompoundUnit with(AMEEPerUnit perUnit) {
        return AMEECompoundUnit.valueOf(this, perUnit);
    }

    public boolean isCompatibleWith(String unit) {
        return this.unit.isCompatible(internalValueOf(unit));
    }

    // This is like Unit.valueOf but forces use of UNIT_FORMAT instead.
    protected static Unit<? extends Quantity> internalValueOf(CharSequence csq) {
        try {
            return UNIT_FORMAT.parseProductUnit(csq, new ParsePosition(0));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean equals(AMEEUnit that) {
        return toUnit().equals(that.toUnit());
    }

    public Unit toUnit() {
        return unit;
    }

    public String toString() {
        return UNIT_FORMAT.format(toUnit());
    }
}
