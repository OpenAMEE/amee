package gc.carbon.domain;

import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.data.Decimal;
import org.joda.time.Duration;
import org.joda.time.format.ISOPeriodFormat;

import javax.measure.DecimalMeasure;
import javax.measure.unit.Dimension;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
import javax.measure.unit.Unit;
import java.math.BigDecimal;

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
public class AMEEPerUnit extends AMEEUnit {

    public static final AMEEPerUnit ONE = new AMEEPerUnit(Unit.ONE);
    public static final AMEEPerUnit MONTH = AMEEPerUnit.valueOf("month");

    private String string;

    public AMEEPerUnit(Unit unit) {
        super(unit);
        this.string = unit.toString();
    }

    private AMEEPerUnit(Duration duration) {
        super(MILLI(SECOND).times(duration.getMillis()));
        this.string = ISOPeriodFormat.standard().print(duration.toPeriod());
    }

    public static AMEEPerUnit valueOf(String unit) {
        return new AMEEPerUnit(internalValueOf(unit));
    }

    public static AMEEPerUnit valueOf(Duration duration) {
        return new AMEEPerUnit(duration);
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
