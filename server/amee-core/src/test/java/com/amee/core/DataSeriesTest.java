package com.amee.core;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class DataSeriesTest {

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
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));

        // Add a series and a single point
        sum = new ArrayList<DataPoint>();
        sum.add(new DataPoint(now.plusDays(1), new Decimal("5")));
        sum.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        sum.add(new DataPoint(now.plusDays(3), new Decimal("7")));
        actual = new DataSeries(sum);
        test = lhs.plus(rhp);


        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));

        // Add a series and a primitive
        test = lhs.plus(rhf);
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));

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
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("-3")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("-2")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("-1")));
        actual = new DataSeries(diff);

        test = lhs.subtract(rhp);
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));

        // Subtract a series and a primitive
        test = lhs.subtract(rhf);
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));
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
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));

        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("0.25")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("0.5")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("0.75")));
        actual = new DataSeries(diff);
        test = lhs.divide(rhp);
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));

        // Divide a series and a primitive
        test = lhs.divide(rhf);

        //print(test, actual);
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));
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
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("4")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("8")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("16")));
        actual = new DataSeries(diff);
        test = lhs.multiply(rhp);
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));

        // Divide a series and a primitive
        test = lhs.multiply(rhf);
        assertTrue("Integrate should produce the correct value.", test.integrate().equals(actual.integrate()));
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
        assertTrue("Should have correct time window.", series.getSeriesTimeInMillis().equals(window));
        assertTrue("Should be able to integrate.", series.integrate() != null);
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
        assertTrue("Should have correct time window.", series.getSeriesTimeInMillis().equals(window));
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
        assertTrue("Should have correct time window.", series.getSeriesTimeInMillis().equals(window));
        assertTrue("Should be able to integrate.", series.integrate() != null);
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
        assertTrue("Should have correct time window.", series.getSeriesTimeInMillis().equals(window));
        assertTrue("Should be able to integrate.", series.integrate() != null);
    }

    @Test
    public void queryWithoutStartOrEndDate() {
        DateTime start = now.plusDays(1);
        DateTime end = now.plusDays(4);
        List<DataPoint> points = new ArrayList<DataPoint>();
        points.add(new DataPoint(start, new Decimal("2")));
        points.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        points.add(new DataPoint(now.plusDays(3), new Decimal("12")));
        points.add(new DataPoint(end, new Decimal("12")));
        DataSeries series = new DataSeries(points);
        Decimal window = new Decimal(end.getMillis() - start.getMillis());
        assertTrue("Should have correct time window.", series.getSeriesTimeInMillis().equals(window));
        assertTrue("Should be able to integrate.", series.integrate() != null);
    }

    @Test
    public void queryWithInsideStartAndEndDate() {
        int start = 15;
        int end = 15;
        List<DataPoint> points = new ArrayList<DataPoint>();
        points.add(new DataPoint(now.plusDays(10), new Decimal("2")));
        points.add(new DataPoint(now.plusDays(20), new Decimal("6")));
        points.add(new DataPoint(now.plusDays(30), new Decimal("12")));
        points.add(new DataPoint(now.plusDays(40), new Decimal("12")));
        DataSeries series = new DataSeries(points);
        series.setSeriesStartDate(now.plusDays(start));
        series.setSeriesEndDate(now.plusDays(end));
        Decimal window = new Decimal(now.plusDays(end).getMillis() - now.plusDays(start).getMillis());
        assertTrue("Should have correct time window.", series.getSeriesTimeInMillis().equals(window));
        assertTrue("Should be able to integrate.", series.integrate() != null);
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
