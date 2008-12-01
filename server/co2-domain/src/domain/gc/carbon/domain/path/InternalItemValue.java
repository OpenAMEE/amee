package gc.carbon.domain.path;

import com.jellymold.utils.ValueType;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.profile.ProfileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Duration;

import javax.measure.DecimalMeasure;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
import javax.measure.unit.Unit;
import java.math.BigDecimal;
import java.math.MathContext;

/**
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

@SuppressWarnings("unchecked")
public class InternalItemValue {

    private final Log log = LogFactory.getLog(getClass());

    private static final MathContext CONTEXT = new MathContext(ProfileItem.PRECISION, ProfileItem.ROUNDING_MODE);

    private Object value;
    private Unit unit;
    private Unit perUnit;

    public InternalItemValue(String value) {
        this.value = value;
    }

    public InternalItemValue(ItemValueDefinition itemValueDefinition) {
        if (isDecimal(itemValueDefinition)) {
            if (itemValueDefinition.hasUnits())
                unit = Unit.valueOf(itemValueDefinition.getInternalUnit());
            if (itemValueDefinition.hasPerUnits())
                perUnit = Unit.valueOf(itemValueDefinition.getInternalPerUnit());
            value = convertStringToDecimal(itemValueDefinition.getUsableValue());
        } else {
            value = itemValueDefinition.getUsableValue();
        }
    }

    public InternalItemValue(ItemValue itemValue) {
        if (isDecimal(itemValue.getItemValueDefinition())) {
            if (itemValue.hasUnits()) {
                unit = Unit.valueOf(itemValue.getUnit());
            }
            if (itemValue.hasPerUnits()) {
                if (itemValue.getPerUnit().equals("none")) {
                    Duration duration = itemValue.getItem().getDuration();
                    long ms = duration.getMillis();
                    perUnit = MILLI(SECOND);
                    value = new BigDecimal(value.toString()).divide(new BigDecimal(ms)).toString();
                } else {
                    perUnit = Unit.valueOf(itemValue.getPerUnit());
                }
            }
            value = asInternalValue(convertStringToDecimal(itemValue.getUsableValue()), itemValue.getItemValueDefinition());
        } else {
            value = itemValue.getUsableValue();
        }
    }

    public Object getValue() {
        return value;
    }

    private boolean isDecimal(ItemValueDefinition ivd) {
        return ivd.getValueDefinition().getValueType().equals(ValueType.DECIMAL);
    }

    private BigDecimal convertStringToDecimal(String decimalString) {
        BigDecimal decimal;
        try {
            decimal = new BigDecimal(decimalString);
            decimal = decimal.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
            if (decimal.precision() > ProfileItem.PRECISION) {
                log.warn("precision is too big: " + decimal);
                //TODO - do something?
            }
        } catch (Exception e) {
            log.warn("caught Exception: " + e);
            decimal = ProfileItem.ZERO;
        }
        return decimal;
    }

    private BigDecimal asInternalValue(BigDecimal decimal, ItemValueDefinition ivd) {
        if (unit == null && perUnit == null) {
            return decimal;
        }

        Unit internalUnit = Unit.valueOf(ivd.getInternalUnit()).divide(Unit.valueOf(ivd.getInternalPerUnit()));
        Unit externalUnit;
        BigDecimal internalDecimal;

        if (unit != null && perUnit != null) {
            externalUnit = unit.divide(perUnit);
        } else {
            if (unit == null)
                externalUnit = Unit.valueOf(ivd.getInternalUnit()).divide(unit);
            else
                externalUnit = unit.divide(Unit.valueOf(ivd.getInternalPerUnit()));
        }

        if (!externalUnit.equals(internalUnit)) {
            DecimalMeasure dm = DecimalMeasure.valueOf(decimal, externalUnit);
            internalDecimal = dm.to(internalUnit, CONTEXT).getValue();
        } else {
            internalDecimal = decimal;
        }

        return internalDecimal;
    }
}