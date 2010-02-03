package com.amee.core;

import org.apache.commons.lang.StringUtils;

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
public class DecimalUnit {

    protected final static UnitFormat UNIT_FORMAT = UnitFormat.getInstance();

    // Define various watt based units.
    private final static Unit<Power> KILOWATT = SI.WATT.times(1000);
    private final static Unit<Power> MEGAWATT = KILOWATT.times(1000);
    private final static Unit<Power> GIGAWATT = MEGAWATT.times(1000);
    private final static Unit<Power> TERAWATT = GIGAWATT.times(1000);
    private final static Unit<? extends Quantity> KILOWATT_HOUR = KILOWATT.times(NonSI.HOUR);
    private final static Unit<? extends Quantity> MEGAWATT_HOUR = MEGAWATT.times(NonSI.HOUR);
    private final static Unit<? extends Quantity> GIGAWATT_HOUR = GIGAWATT.times(NonSI.HOUR);
    private final static Unit<? extends Quantity> TERAWATT_HOUR = TERAWATT.times(NonSI.HOUR);

    // Define BTUs.
    // Based on: http://en.wikipedia.org/wiki/British_thermal_unit (21/12/2009)
    private final static Unit<? extends Quantity> BTU_39F = SI.JOULE.times(1059.67);
    private final static Unit<? extends Quantity> BTU_MEAN = SI.JOULE.times(1055.87);
    private final static Unit<? extends Quantity> BTU_IT = SI.JOULE.times(1055.05585262);
    private final static Unit<? extends Quantity> BTU_ISO = SI.JOULE.times(1055.056);
    private final static Unit<? extends Quantity> BTU_59F = SI.JOULE.times(1054.804);
    private final static Unit<? extends Quantity> BTU_60F = SI.JOULE.times(1054.68);
    private final static Unit<? extends Quantity> BTU_63F = SI.JOULE.times(1054.6);
    private final static Unit<? extends Quantity> BTU_THERMOCHEMICAL = SI.JOULE.times(1054.35026444);

    // Define THERMs.
    private final static Unit<? extends Quantity> THERM_39F = BTU_39F.times(100000);
    private final static Unit<? extends Quantity> THERM_MEAN = BTU_MEAN.times(100000);
    private final static Unit<? extends Quantity> THERM_IT = BTU_IT.times(100000);
    private final static Unit<? extends Quantity> THERM_ISO = BTU_ISO.times(100000);
    private final static Unit<? extends Quantity> THERM_59F = BTU_59F.times(100000);
    private final static Unit<? extends Quantity> THERM_60F = BTU_60F.times(100000);
    private final static Unit<? extends Quantity> THERM_63F = BTU_63F.times(100000);
    private final static Unit<? extends Quantity> THERM_THERMOCHEMICAL = BTU_THERMOCHEMICAL.times(100000);

    {
        // Create usable ASCII representations. JScience will use non-ASCII characters by default.
        UNIT_FORMAT.label(KILOWATT_HOUR, "kWh");
        UNIT_FORMAT.label(MEGAWATT_HOUR, "MWh");
        UNIT_FORMAT.label(GIGAWATT_HOUR, "GWh");
        UNIT_FORMAT.label(TERAWATT_HOUR, "TWh");

        // BTUs.
        UNIT_FORMAT.label(BTU_39F, "BTU_ThirtyNineF");
        UNIT_FORMAT.label(BTU_MEAN, "BTU_Mean");
        UNIT_FORMAT.label(BTU_IT, "BTU_IT");
        UNIT_FORMAT.label(BTU_ISO, "BTU_ISO");
        UNIT_FORMAT.label(BTU_59F, "BTU_FiftyNineF");
        UNIT_FORMAT.label(BTU_60F, "BTU_SixtyF");
        UNIT_FORMAT.label(BTU_63F, "BTU_SixtyThreeF");
        UNIT_FORMAT.label(BTU_THERMOCHEMICAL, "BTU_Thermochemical");

        // THERMs.
        UNIT_FORMAT.label(THERM_39F, "thm_ThirtyNineF");
        UNIT_FORMAT.label(THERM_MEAN, "thm_Mean");
        UNIT_FORMAT.label(THERM_IT, "thm_IT");
        UNIT_FORMAT.alias(THERM_IT, "thm_ec");
        UNIT_FORMAT.label(THERM_ISO, "thm_ISO");
        UNIT_FORMAT.label(THERM_59F, "thm_FiftyNineF");
        UNIT_FORMAT.alias(THERM_59F, "thm_us");
        UNIT_FORMAT.label(THERM_60F, "thm_SixtyF");
        UNIT_FORMAT.label(THERM_63F, "thm_SixtyThreeF");
        UNIT_FORMAT.label(THERM_THERMOCHEMICAL, "thm_Thermochemical");

        // Ensure that "gal" and "oz" are sensible for AMEE.
        // JScience will bizarrely default "gal" and "oz" to UK units for UK Locale.
        UNIT_FORMAT.label(NonSI.GALLON_LIQUID_US, "gal");
        UNIT_FORMAT.label(NonSI.OUNCE, "oz");

        // For GALLON_UK, explicitly declare gal_uk as canonical and gallon_uk as the alias.
        UNIT_FORMAT.label(NonSI.GALLON_UK, "gal_uk");
        UNIT_FORMAT.alias(NonSI.GALLON_UK, "gallon_uk");

        // Need to explicitly declare these otherwise we get a parse error.
        UNIT_FORMAT.label(NonSI.OUNCE_LIQUID_US, "oz_fl");
        UNIT_FORMAT.label(NonSI.OUNCE_LIQUID_UK, "oz_fl_uk");
    }

    public static final DecimalUnit ONE = new DecimalUnit(Unit.ONE);
    protected Unit unit = Unit.ONE;

    public DecimalUnit(Unit unit) {
        this.unit = unit;
    }

    public static DecimalUnit valueOf(String unit) {
        return new DecimalUnit(internalValueOf(unit));
    }

    public DecimalCompoundUnit with(DecimalPerUnit perUnit) {
        return DecimalCompoundUnit.valueOf(this, perUnit);
    }

    public boolean isCompatibleWith(String unit) {
        return StringUtils.isNotBlank(unit) && this.unit.isCompatible(internalValueOf(unit));
    }

    // This is like Unit.valueOf but forces use of UNIT_FORMAT instead.
    protected static Unit<? extends Quantity> internalValueOf(CharSequence unit) {
        if ((unit == null) || (unit.length() == 0)) {
            throw new IllegalArgumentException("The unit argument is blank.");
        }
        try {
            return UNIT_FORMAT.parseProductUnit(unit, new ParsePosition(0));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean equals(DecimalUnit that) {
        return toUnit().equals(that.toUnit());
    }

    public Unit toUnit() {
        return unit;
    }

    public String toString() {
        return UNIT_FORMAT.format(toUnit());
    }
}
