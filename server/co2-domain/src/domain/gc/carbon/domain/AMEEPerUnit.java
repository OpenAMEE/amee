package gc.carbon.domain;

import gc.carbon.domain.profile.ProfileItem;
import org.joda.time.Duration;
import org.joda.time.format.ISOPeriodFormat;

import javax.measure.DecimalMeasure;
import javax.measure.unit.Dimension;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
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

    public static final AMEEPerUnit ONE = new AMEEPerUnit(javax.measure.unit.Unit.ONE);

    private String string;

    public static AMEEPerUnit valueOf(String unit) {
        return new AMEEPerUnit(javax.measure.unit.Unit.valueOf(unit));
    }

    public static AMEEPerUnit valueOf(Duration duration) {
        return new AMEEPerUnit(duration);
    }

    public AMEEPerUnit(javax.measure.unit.Unit unit) {
        super(unit);
        this.string = unit.toString();
    }

    private AMEEPerUnit(Duration duration) {
        super(MILLI(SECOND).times(duration.getMillis()));
        this.string = ISOPeriodFormat.standard().print(duration.toPeriod());
    }

    /**
     * Convert a decimal value from X/PerT1 to X/PerT2.
     *
     * @param decimal - the BigDecimal value to be converted
     * @param targetPerUnit - the target PerUnit of the converted decimal
     * @return the decimal value in the targetPerUnit
     */
    public BigDecimal convert(BigDecimal decimal, AMEEPerUnit targetPerUnit) {
        DecimalMeasure dm = DecimalMeasure.valueOf(decimal, toUnit().inverse());
        BigDecimal converted = dm.to(targetPerUnit.toUnit().inverse(), ProfileItem.CONTEXT).getValue();
        return converted.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
    }

    public boolean isCompatibleWith(String unit) {
        return "none".equals(unit) || this.unit.isCompatible(javax.measure.unit.Unit.valueOf(unit));
    }

    public boolean isTime() {
        return toUnit().getDimension().equals(Dimension.TIME);    
    }

    public javax.measure.unit.Unit toUnit() {
        return unit;
    }

    public String toString() {
        return string;
    }
}
