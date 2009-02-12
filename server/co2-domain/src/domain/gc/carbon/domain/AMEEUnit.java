package gc.carbon.domain;

import gc.carbon.domain.profile.ProfileItem;

import javax.measure.DecimalMeasure;
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
public class AMEEUnit {

    public static final AMEEUnit ONE = new AMEEUnit(Unit.ONE);
    protected Unit unit = Unit.ONE;

    public static AMEEUnit valueOf(String unit) {
        return new AMEEUnit(Unit.valueOf(unit));
    }

    public AMEEUnit(Unit unit) {
        this.unit = unit;
    }

    public AMEEUnit with(AMEEPerUnit perUnit) {
        return AMEECompoundUnit.valueOf(this, perUnit);
    }

    public BigDecimal convert(BigDecimal decimal, AMEEUnit targetUnit) {
        DecimalMeasure dm = DecimalMeasure.valueOf(decimal, toUnit());
        BigDecimal converted = dm.to(targetUnit.toUnit(), ProfileItem.CONTEXT).getValue();
        return converted.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
    }

    public boolean isCompatibleWith(String unit) {
        return this.unit.isCompatible(Unit.valueOf(unit));
    }

    public boolean equals(AMEEUnit that) {
        return toUnit().equals(that.toUnit());
    }

    public Unit toUnit() {
        return unit;
    }

    public String toString() {
        return toUnit().toString();
        // return UnitFormat.getUCUMInstance().format(toUnit());
    }
}
