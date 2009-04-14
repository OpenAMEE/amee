package com.amee.domain.path;

import com.amee.domain.core.DecimalCompoundUnit;
import com.amee.domain.core.Decimal;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
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

    private Object value;

    public InternalValue(String value) {
        this.value = value;
    }

    public InternalValue(ItemValueDefinition itemValueDefinition) {
        if (itemValueDefinition.isDecimal()) {
            value = new Decimal(itemValueDefinition.getValue(), itemValueDefinition.getCompoundUnit()).getValue();
        } else {
            value = itemValueDefinition.getValue();
        }
    }

    public InternalValue(ItemValue itemValue) {
        if (itemValue.getItemValueDefinition().isDecimal()) {
            value = asInternalValue(itemValue);
        } else {
            value = itemValue.getUsableValue();
        }
    }

    public Object getValue() {
        return value;
    }

    private BigDecimal asInternalValue(ItemValue iv) {

        if (!iv.hasUnit() && !iv.hasPerUnit())
            return new Decimal(iv.getUsableValue()).getValue();

        Decimal decimal = new Decimal(iv.getUsableValue(), iv.getCompoundUnit());

        DecimalCompoundUnit internalUnit = iv.getItemValueDefinition().getCanonicalCompoundUnit();

        if (decimal.hasDifferentUnits(internalUnit)) {
            if (log.isDebugEnabled()) {
                log.debug("asInternalValue() - path: " + iv.getPath() + " (aliasedTo: " + iv.getItemValueDefinition().getCannonicalPath()
                    + ") external: " + decimal + " " + decimal.getUnit()
                        + ", internal: " + decimal.convert(internalUnit) + " " + internalUnit);
            }
            decimal = decimal.convert(internalUnit);
        }

        return decimal.getValue();
    }
}