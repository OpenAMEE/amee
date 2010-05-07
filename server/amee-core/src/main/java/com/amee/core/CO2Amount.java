package com.amee.core;

import com.amee.platform.science.Amount;

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
public class CO2Amount extends Amount {

    public static final CO2Amount ZERO = new CO2Amount(0.0);

    /**
     * A CO2Amount representing the supplied value and default unit.
     *
     * @param amount
     *
     */
    public CO2Amount(String amount) {
        super(amount, CO2AmountUnit.DEFAULT);
    }

    /**
     * A CO2Amount representing the supplied value and default unit.
     *
     * @param value
     *
     */
    public CO2Amount(double value) {
        this(value, CO2AmountUnit.DEFAULT);
    }


    /**
     * A CO2Amount representing the supplied value and unit.
     *
     * @param value
     * @param unit
     *
     */
    public CO2Amount(double value, CO2AmountUnit unit) {
        super(value, unit);
    }
}
