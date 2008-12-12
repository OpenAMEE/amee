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
public class InternalValue {

    private final Log log = LogFactory.getLog(getClass());

    private static final MathContext CONTEXT = new MathContext(ProfileItem.PRECISION, ProfileItem.ROUNDING_MODE);

    private Object value;

    public InternalValue(String value) {
        this.value = value;
    }

    public InternalValue(ItemValueDefinition itemValueDefinition) {
        if (isDecimal(itemValueDefinition)) {
            value = convertStringToDecimal(itemValueDefinition.getUsableValue());
        } else {
            value = itemValueDefinition.getUsableValue();
        }
    }

    public InternalValue(ItemValue itemValue) {
        if (isDecimal(itemValue.getItemValueDefinition())) {
            value = asInternalValue(itemValue);
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
            }
        } catch (Exception e) {
            log.warn("caught Exception: " + e);
            decimal = ProfileItem.ZERO;
        }
        return decimal;
    }

    private BigDecimal asInternalValue(ItemValue iv) {

        BigDecimal decimal = convertStringToDecimal(iv.getUsableValue());

        if (!iv.hasUnits() && !iv.hasPerUnits())
            return decimal;

        ItemValueDefinition ivd = iv.getItemValueDefinition();
        Unit internalUnit = getInternalUnit(ivd);
        Unit externalUnit = getExternalUnit(iv);

        if (externalUnit.equals(internalUnit))
            return decimal;

        DecimalMeasure dm = DecimalMeasure.valueOf(decimal, externalUnit);
        return dm.to(internalUnit, CONTEXT).getValue();

    }

    private Unit getInternalUnit(ItemValueDefinition ivd) {
        Unit internalUnit;
        if (ivd.hasUnits() && ivd.hasPerUnits()) {
            internalUnit = Unit.valueOf(ivd.getInternalUnit()).divide(Unit.valueOf(ivd.getInternalPerUnit()));
        } else {
            if (ivd.hasUnits())
                internalUnit = Unit.valueOf(ivd.getInternalUnit());
            else
                internalUnit = Unit.valueOf(ivd.getInternalPerUnit()).inverse();
        }
        return internalUnit;
    }

    //TODO - Become overly complex - refactor
    private Unit getExternalUnit(ItemValue iv) {
        Unit externalUnit;
        if ( (iv.getUnit() != null) && (iv.getPerUnit() != null) ) {
            externalUnit = Unit.valueOf(iv.getUnit()).divide(getPerUnit(iv));
        } else {
            if (iv.getUnit() != null) {
                if (iv.hasPerUnits()) {
                    externalUnit = Unit.valueOf(iv.getUnit()).divide(Unit.valueOf(iv.getItemValueDefinition().getInternalPerUnit()));
                } else {
                    externalUnit = Unit.valueOf(iv.getUnit());
                }
            } else {
                if (iv.hasUnits()) {
                    externalUnit = Unit.valueOf(iv.getItemValueDefinition().getInternalUnit()).divide(getPerUnit(iv));
                } else {
                    externalUnit = getPerUnit(iv).inverse();
                }
            }
        }
        return externalUnit;
    }

    private Unit getPerUnit(ItemValue itemValue) {
        if (!itemValue.getPerUnit().equals("none")) {
            return Unit.valueOf(itemValue.getPerUnit());
        } else {
            Duration duration = itemValue.getItem().getDuration();
            return MILLI(SECOND).times(duration.getMillis());
        }
    }

}