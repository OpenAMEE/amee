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

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class DecimalTest {

    DateTime now;
    DataSeries lhs;
    DataSeries rhs;
    DataPoint rhp;
    float rhf;

    @Before
    public void init() {

        now = new DateTime();

        // Test adding two series
        List<DataPoint> a = new ArrayList<DataPoint>();
        a.add(new DataPoint(now.plusDays(1), new Decimal("1")));
        a.add(new DataPoint(now.plusDays(2), new Decimal("2")));
        a.add(new DataPoint(now.plusDays(3), new Decimal("3")));
        lhs = new DataSeries(a);

        List<DataPoint> b = new ArrayList<DataPoint>();
        b.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        b.add(new DataPoint(now.plusDays(2), new Decimal("3")));
        b.add(new DataPoint(now.plusDays(3), new Decimal("4")));
        rhs = new DataSeries(b);

        rhp = new DataPoint(now.plusDays(1), new Decimal("4"));

        rhf = 4.0f;
    }

    @Test
    public void add() {

        DataSeries test;
        DataSeries actual;

        // Add two data series
        List<DataPoint> sum = new ArrayList<DataPoint>();
        sum.add(new DataPoint(now.plusDays(1), new Decimal("3")));
        sum.add(new DataPoint(now.plusDays(2), new Decimal("5")));
        sum.add(new DataPoint(now.plusDays(3), new Decimal("7")));
        actual = new DataSeries(sum);
        test = lhs.plus(rhs);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));

        // Add a series and a single point
        sum = new ArrayList<DataPoint>();
        sum.add(new DataPoint(now.plusDays(1), new Decimal("5")));
        sum.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        sum.add(new DataPoint(now.plusDays(3), new Decimal("7")));
        actual = new DataSeries(sum);
        test = lhs.plus(rhp);


        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));

        // Add a series and a primitive
        test = lhs.plus(rhf);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));

    }

    @Test
    public void subtract() {

        DataSeries test;
        DataSeries actual;

        List<DataPoint> diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("-1")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("-1")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("-1")));
        actual = new DataSeries(diff);
        test = lhs.subtract(rhs);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("-3")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("-2")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("-1")));
        actual = new DataSeries(diff);

        test = lhs.subtract(rhp);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));

        // Subtract a series and a primitive
        test = lhs.subtract(rhf);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));
    }

    @Test
    public void divide() {

        DataSeries test;
        DataSeries actual;

        List<DataPoint> diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("0.5")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("2").divide(new Decimal("3"))));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("0.75")));
        actual = new DataSeries(diff);
        test = lhs.divide(rhs);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("0.25")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("0.5")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("0.75")));
        actual = new DataSeries(diff);
        test = lhs.divide(rhp);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));

        // Divide a series and a primitive
        test = lhs.divide(rhf);

        //print(test, actual);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));
    }

    @Test
    public void multiply() {

        DataSeries test;
        DataSeries actual;

        List<DataPoint> diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("12")));
        actual = new DataSeries(diff);
        test = lhs.multiply(rhs);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("4")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("8")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("16")));
        actual = new DataSeries(diff);
        test = lhs.multiply(rhp);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));

        // Divide a series and a primitive
        test = lhs.multiply(rhf);
        assertTrue("Integrate should produce the correct value", test.integrate().equals(actual.integrate()));
    }

    @Test
    public void queryWithNarrowerStartAndEndDate() {
        List<DataPoint> points = new ArrayList<DataPoint>();
        points.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        points.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        points.add(new DataPoint(now.plusDays(3), new Decimal("12")));
        points.add(new DataPoint(now.plusDays(4), new Decimal("12")));
        DataSeries series = new DataSeries(points);
        series.setSeriesStartDate(now.plusDays(1));
        series.setSeriesEndDate(now.plusDays(3));
        Decimal window = new Decimal(now.plusDays(3).getMillis() - now.plusDays(1).getMillis());
        assertTrue("Should have correct time window", series.getSeriesTimeInMillis().equals(window));
    }

    @Test
    public void queryWithWiderStartAndEndDate() {
        List<DataPoint> points = new ArrayList<DataPoint>();
        points.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        points.add(new DataPoint(now.plusDays(3), new Decimal("12")));
        points.add(new DataPoint(now.plusDays(4), new Decimal("12")));
        DataSeries series = new DataSeries(points);
        series.setSeriesStartDate(now.plusDays(1));
        series.setSeriesEndDate(now.plusDays(5));
        Decimal window = new Decimal(now.plusDays(4).getMillis() - now.plusDays(2).getMillis());
        assertTrue("Should have correct time window", series.getSeriesTimeInMillis().equals(window));
    }

    @Test
    public void queryWithStartDate() {
        List<DataPoint> points = new ArrayList<DataPoint>();
        points.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        points.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        points.add(new DataPoint(now.plusDays(3), new Decimal("12")));
        points.add(new DataPoint(now.plusDays(4), new Decimal("12")));
        DataSeries series = new DataSeries(points);
        series.setSeriesStartDate(now.plusDays(2));
        Decimal window = new Decimal(now.plusDays(4).getMillis() - now.plusDays(2).getMillis());
        assertTrue("Should have correct time window", series.getSeriesTimeInMillis().equals(window));
    }

    @Test
    public void queryWithEndDate() {
        List<DataPoint> points = new ArrayList<DataPoint>();
        points.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        points.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        points.add(new DataPoint(now.plusDays(3), new Decimal("12")));
        points.add(new DataPoint(now.plusDays(4), new Decimal("12")));
        DataSeries series = new DataSeries(points);
        series.setSeriesEndDate(now.plusDays(2));
        Decimal window = new Decimal(now.plusDays(2).getMillis() - now.plusDays(1).getMillis());
        assertTrue("Should have correct time window", series.getSeriesTimeInMillis().equals(window));
    }

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

    private void print(DataSeries test, DataSeries actual) {

        for (DateTime dt : test.getDateTimePoints()) {
            DataPoint dp = test.getDataPoint(dt);
            System.out.println("test: " + dt.toString("yyyy-dd-MM") + " => " + dp.getValue());
        }
        System.out.println("test: " + test.integrate());

        for (DateTime dt : actual.getDateTimePoints()) {
            DataPoint dp = actual.getDataPoint(dt);
            System.out.println("actual: " + dt.toString("yyyy-dd-MM") + " => " + dp.getValue());
        }
        System.out.println("actual: " + actual.integrate());
    }
}
