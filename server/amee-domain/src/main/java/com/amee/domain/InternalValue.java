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
package com.amee.domain;

import com.amee.core.DataPoint;
import com.amee.core.DataSeries;
import com.amee.core.Decimal;
import com.amee.core.DecimalCompoundUnit;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
/**
 * Provides a wrapper around external representations of values.
 *
 * Decimal values will be converted to AMEE internal units if neccessary. All other value types will be wrapped unchanged.
 */
public class InternalValue {

    private final Log log = LogFactory.getLog(getClass());

    private Object value;

    /**
     * Instantiate an InternalValue representation of the supplied value.
     *
     * @param value - the String representation of the value.
     */
    public InternalValue(String value) {
        this.value = value;
    }

    /**
     * Instantiate an InternalValue representation of the supplied value.
     *
     * @param value - the {@link ItemValueDefinition} representation of the value.
     */
    public InternalValue(ItemValueDefinition value) {
        if (value.isDecimal()) {
            this.value = new Decimal(value.getValue(), value.getCompoundUnit()).getValue();
        } else {
            this.value = value.getValue();
        }
    }

    /**
     * Instantiate an InternalValue representation of the supplied value.
     *
     * @param value - the {@link ItemValue} representation of the value.
     */
    public InternalValue(ItemValue value) {
        if (value.getItemValueDefinition().isDecimal()) {
            this.value = asInternalDecimal(value).getValue();
        } else {
            this.value = value.getUsableValue();
        }
    }

    /**
     * Instantiate an InternalValue representation of the supplied value.
     *
     * @param values - the Set of {@link ItemValue} representing an historical sequence of values.
     */
    public InternalValue(List<ItemValue> values) {
        Iterator<ItemValue> itr = values.iterator();
        ItemValue itemValue = itr.next();
        if (itemValue.getItemValueDefinition().isDecimal()) {
            DataSeries ds = new DataSeries();
            do {
                ds.addDataPoint(new DataPoint(itemValue.getStartDate().toDateTime(), asInternalDecimal(itemValue)));
                itemValue = itr.next();
            } while (itr.hasNext());
            this.value = ds;
        } else {
            this.value = values;
        }
    }

    /**
     * Get the wrapped internal value.
     *
     * @return - the wrapped internal value.
     */
    public Object getValue() {
        return value;
    }

    private Decimal asInternalDecimal(ItemValue iv) {

        if (!iv.hasUnit() && !iv.hasPerUnit())
            return new Decimal(iv.getUsableValue());

        Decimal decimal = new Decimal(iv.getUsableValue(), iv.getCompoundUnit());

        DecimalCompoundUnit internalUnit = iv.getItemValueDefinition().getCanonicalCompoundUnit();

        if (decimal.hasDifferentUnits(internalUnit)) {
            if (log.isDebugEnabled()) {
                log.debug("asInternalDecimal() - path: " + iv.getPath() + " (aliasedTo: " + iv.getItemValueDefinition().getCannonicalPath()
                    + ") external: " + decimal + " " + decimal.getUnit()
                        + ", internal: " + decimal.convert(internalUnit) + " " + internalUnit);
            }
            decimal = decimal.convert(internalUnit);
        }

        return decimal;
    }
}