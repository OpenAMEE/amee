package com.amee.core;

import org.joda.time.Duration;
import org.joda.time.format.ISOPeriodFormat;

import javax.measure.unit.Dimension;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

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
public class DecimalPerUnit extends DecimalUnit {

    public static final DecimalPerUnit ONE = new DecimalPerUnit(Unit.ONE);
    public static final DecimalPerUnit MONTH = DecimalPerUnit.valueOf("month");

    private String string;

    public DecimalPerUnit(Unit unit) {
        super(unit);
        this.string = unit.toString();
    }

    private DecimalPerUnit(Duration duration) {
        super(SI.MILLI(SI.SECOND).times(duration.getMillis()));
        this.string = ISOPeriodFormat.standard().print(duration.toPeriod());
    }

    public static DecimalPerUnit valueOf(String unit) {
        return new DecimalPerUnit(internalValueOf(unit));
    }

    public static DecimalPerUnit valueOf(Duration duration) {
        return new DecimalPerUnit(duration);
    }

    public boolean isCompatibleWith(String unit) {
        return "none".equals(unit) || this.unit.isCompatible(internalValueOf(unit));
    }

    public boolean isTime() {
        return toUnit().getDimension().equals(Dimension.TIME);    
    }

    public Unit toUnit() {
        return unit;
    }

    public String toString() {
        return string;
    }
}
