package gc.carbon.domain.path;

import com.jellymold.utils.ValueType;
import gc.carbon.domain.AMEEUnit;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.profile.ProfileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

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

    private String name;
    private Object value;

    public InternalValue(String value) {
        this.value = value;
    }

    public InternalValue(ItemValueDefinition itemValueDefinition) {
        name = itemValueDefinition.getName();
        if (itemValueDefinition.isDecimal()) {
            value = convertStringToDecimal(itemValueDefinition.getUsableValue());
        } else {
            value = itemValueDefinition.getUsableValue();
        }
    }

    public InternalValue(ItemValue itemValue) {
        name = itemValue.getName();
        if (itemValue.getItemValueDefinition().isDecimal()) {
            value = asInternalValue(itemValue);
        } else {
            value = itemValue.getUsableValue();
        }
    }

    public Object getValue() {
        return value;
    }

    private BigDecimal convertStringToDecimal(String decimalString) {
        BigDecimal decimal;
        try {
            decimal = new BigDecimal(decimalString);
            decimal = decimal.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
            if (decimal.precision() > ProfileItem.PRECISION) {
                log.warn("convertStringToDecimal() - precision is too big: " + decimal);
            }
        } catch (NumberFormatException e) {
            log.warn("convertStringToDecimal() - NumberFormatException: name=" + name + ", value=" + value);
            decimal = ProfileItem.ZERO;
        }
        return decimal;
    }

    private BigDecimal asInternalValue(ItemValue iv) {

        BigDecimal decimal = convertStringToDecimal(iv.getUsableValue());

        if (!iv.hasUnit() && !iv.hasPerUnit())
            return decimal;

        AMEEUnit internalUnit = iv.getItemValueDefinition().getCanonicalCompoundUnit();
        AMEEUnit externalUnit = iv.getCompoundUnit();
        if (!externalUnit.equals(internalUnit)) {
            if (log.isDebugEnabled()) {
                log.debug("asInternalValue() - path: " + iv.getPath() + " (aliasedTo: " + iv.getItemValueDefinition().getCannonicalPath()
                    + ") external: " + decimal + " " + externalUnit
                        + ", internal: " + externalUnit.convert(decimal, internalUnit) + " " + internalUnit);
            }
            decimal = externalUnit.convert(decimal, internalUnit);
        }

        return decimal;
    }
}