/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *                                                                      
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.core;

import org.junit.Test;

import static junit.framework.Assert.fail;

public class DecimalTest {

    @Test
    public void shouldAllowDecimalUpToLimit() {
        try {
            // precision 21, scale 6
            printWithPrecisionAndScale(new Decimal("123456789012345"));
            printWithPrecisionAndScale(new Decimal("123456789012345.123456"));
            printWithPrecisionAndScale(new Decimal("12345678901234.1234567")); // round up .123457
            printWithPrecisionAndScale(new Decimal("-999999999999999.999999")); // min
            printWithPrecisionAndScale(new Decimal("999999999999999.999999")); // max
        } catch (IllegalArgumentException e) {
            fail("Value should be OK.");
        }
    }

    @Test
    public void shouldNotAllowDecimalOverLimit() {
        try {
            // precision 22, scale 6
            new Decimal("1234567890123456");
            fail("Value should NOT be OK.");
        } catch (IllegalArgumentException e) {
            // swallow
        }
        try {
            // precision 22, scale 6
            new Decimal("1234567890123456.123456");
            fail("Value should NOT be OK.");
        } catch (IllegalArgumentException e) {
            // swallow
        }
        try {
            // precision 22, scale 6
            printWithPrecisionAndScale(new Decimal("-9999999999999999.999999")); // min + 1 extra digit on the left
            fail("Value should NOT be OK.");
        } catch (IllegalArgumentException e) {
            // swallow
        }
        try {
            // precision 22, scale 6
            printWithPrecisionAndScale(new Decimal("9999999999999999.999999")); // max + 1 extra digit on the left
            fail("Value should NOT be OK.");
        } catch (IllegalArgumentException e) {
            // swallow
        }
    }

    private void printWithPrecisionAndScale(Decimal decimal) {
        System.out.println(
                "decimal: " + decimal.toString() +
                        " precision: " + decimal.getValue().precision() +
                        " scale: " + decimal.getValue().scale());
    }
}
