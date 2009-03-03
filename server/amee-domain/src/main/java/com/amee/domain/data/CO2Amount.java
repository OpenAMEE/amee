package com.amee.domain.data;

import java.math.BigDecimal;

/**
 * A CO2 amount calculated by AMEE.
 * 
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
public class CO2Amount extends Decimal {

    public static final CO2Amount ZERO = new CO2Amount(BigDecimal.ZERO);

    /**
     * A CO2Amount representing the supplied decimal and default unit.
     *
     * @param decimal
     *
     * @see com.amee.domain.data.CO2AmountUnit:DEFAULT
     */
    public CO2Amount(String decimal) {
        super(decimal, CO2AmountUnit.DEFAULT);
    }

    /**
     * A CO2Amount representing the supplied decimal and default unit.
     *
     * @param decimal
     *
     * @see com.amee.domain.data.CO2AmountUnit:DEFAULT
     */
    public CO2Amount(BigDecimal decimal) {
        this(decimal, CO2AmountUnit.DEFAULT);
    }

    /**
     * A CO2Amount representing the supplied decimal and unit.
     *
     * @param decimal
     * @param unit
     *
     */
    public CO2Amount(BigDecimal decimal, CO2AmountUnit unit) {
        super(decimal.toString(), unit);
    }
}
