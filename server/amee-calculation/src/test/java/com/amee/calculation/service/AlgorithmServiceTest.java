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
package com.amee.calculation.service;

import com.amee.core.DataPoint;
import com.amee.core.DataSeries;
import com.amee.core.Decimal;
import com.amee.domain.algorithm.Algorithm;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Implements various Algorithm related test cases.
 * <p/>
 * Some test cases are described here: https://docs.google.com/a/amee.cc/Doc?docid=0AVPTOpeCYkq1ZGZxOXE1Y3JfMTFkZDk5eHdjZA&hl=en_GB
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AlgorithmServiceTest {

    DataSeries seriesA;
    DataSeries seriesB;
    DataSeries seriesC;

    @Autowired
    private AlgorithmService algorithmService;

    @Before
    public void init() {
        // Create DataSeries A.
        seriesA = new DataSeries();
        seriesA.addDataPoint(new DataPoint(new DateTime(2010, 1, 1, 0, 0, 0, 0), new Decimal("1")));
        seriesA.addDataPoint(new DataPoint(new DateTime(2010, 1, 3, 0, 0, 0, 0), new Decimal("0")));
        seriesA.addDataPoint(new DataPoint(new DateTime(2010, 1, 4, 0, 0, 0, 0), new Decimal("0.5")));
        // Create DataSeries B.
        seriesB = new DataSeries();
        seriesB.addDataPoint(new DataPoint(new DateTime(2010, 1, 1, 0, 0, 0, 0), new Decimal("0")));
        seriesB.addDataPoint(new DataPoint(new DateTime(2010, 1, 3, 0, 0, 0, 0), new Decimal("1")));
        seriesB.addDataPoint(new DataPoint(new DateTime(2010, 1, 4, 0, 0, 0, 0), new Decimal("2")));
        // Create DataSeries C.
        seriesC = new DataSeries();
        seriesC.addDataPoint(new DataPoint(new DateTime(2010, 1, 1, 0, 0, 0, 0), new Decimal("0")));
        seriesC.addDataPoint(new DataPoint(new DateTime(2010, 1, 2, 0, 0, 0, 0), new Decimal("1")));
        seriesC.addDataPoint(new DataPoint(new DateTime(2010, 1, 4, 0, 0, 0, 0), new Decimal("3")));
    }

    @Test
    public void reallySimpleAlgorithmOK() throws ScriptException {
        Algorithm algorithm = new Algorithm();
        algorithm.setContent("1");
        Map<String, Object> values = new HashMap<String, Object>();
        assertTrue("Really simple algorithm should be OK.", algorithmService.evaluate(algorithm, values) != null);
    }

    @Test
    public void emptyAlgorithmNotOK() throws ScriptException {
        Algorithm algorithm = new Algorithm();
        algorithm.setContent("");
        Map<String, Object> values = new HashMap<String, Object>();
        try {
            algorithmService.evaluate(algorithm, values);
            fail("Empty algorithm should NOT be OK.");
        } catch (Throwable t) {
            // swallow
        }
    }

//    @Test
//    public void algorithmCanHandleMissingObject() throws ScriptException {
//        Algorithm algorithm = new Algorithm();
//        algorithm.setContent("if (testObj) { 1; } else { 0; }");
//        Map<String, Object> values = new HashMap<String, Object>();
//        BigDecimal result = new BigDecimal(algorithmService.evaluate(algorithm, values));
//        assertTrue("Algorithm should detect object is missing.", result.equals(new BigDecimal("0")));
//    }
//
//    @Test
//    public void algorithmCanHandlePresentObject() throws ScriptException {
//        Algorithm algorithm = new Algorithm();
//        algorithm.setContent("if (testObj) { 1; } else { 0; }");
//        Map<String, Object> values = new HashMap<String, Object>();
//        values.put("testObj", new Boolean(true));
//        BigDecimal result = new BigDecimal(algorithmService.evaluate(algorithm, values));
//        assertTrue("Algorithm should detect object is present.", result.equals(new BigDecimal("1")));
//    }

    @Test
    public void algorithmCanThrowIllegalArgumentException() throws ScriptException {
        Algorithm algorithm = new Algorithm();
        algorithm.setContent("throw new java.lang.IllegalArgumentException('Bang!');");
        Map<String, Object> values = new HashMap<String, Object>();
        try {
            algorithmService.evaluate(algorithm, values);
            fail("Algorithm should throw IllegalArgumentException.");
        } catch (ScriptException e) {
            IllegalArgumentException iae = AlgorithmService.getIllegalArgumentException(e);
            if ((iae == null) || !iae.getMessage().equals("Bang!")) {
                fail("Algorithm should throw IllegalArgumentException with correct message.");
            }
        }
    }

    /**
     * A by B.
     */
    @Test
    public void shouldMultiplyDataSeriesAAndB() {
        String expectedSeriesAMultipliedByB = "{\"dataPoints\":[[\"2010-01-01T00:00:00.000Z\",\"0.000000\"],[\"2010-01-03T00:00:00.000Z\",\"0.000000\"],[\"2010-01-04T00:00:00.000Z\",\"1.000000\"]]}";
        DataSeries seriesAMultipliedByB = seriesA.multiply(seriesB);
        System.out.println(expectedSeriesAMultipliedByB);
        System.out.println(seriesAMultipliedByB.toString());
        assertTrue("Should be able to multiply two DataSeries objects.", seriesAMultipliedByB.toString().equals(expectedSeriesAMultipliedByB));
    }

    /**
     * Add B to A.
     */
    @Test
    public void shouldAddDataSeriesBToA() {
        String expectedSeriesAPlusB = "{\"dataPoints\":[[\"2010-01-01T00:00:00.000Z\",\"1.000000\"],[\"2010-01-03T00:00:00.000Z\",\"1.000000\"],[\"2010-01-04T00:00:00.000Z\",\"2.500000\"]]}";
        DataSeries seriesAPlusB = seriesA.plus(seriesB);
        assertTrue("Should be able to add together two DataSeries objects.", seriesAPlusB.toString().equals(expectedSeriesAPlusB));
    }

    /**
     * Multiply B by C.
     */
    @Test
    public void shouldMultiplyDataSeriesBAndC() {
        String expectedSeriesBMultipliedByC = "{\"dataPoints\":[[\"2010-01-01T00:00:00.000Z\",\"0.000000\"],[\"2010-01-02T00:00:00.000Z\",\"0.000000\"],[\"2010-01-03T00:00:00.000Z\",\"1.000000\"],[\"2010-01-04T00:00:00.000Z\",\"6.000000\"]]}";
        DataSeries seriesBMultipliedByC = seriesB.multiply(seriesC);
        System.out.println(expectedSeriesBMultipliedByC);
        System.out.println(seriesBMultipliedByC.toString());
        assertTrue("Should be able to multiply two DataSeries objects.", seriesBMultipliedByC.toString().equals(expectedSeriesBMultipliedByC));
    }

    /**
     * Add C to B.
     */
    @Test
    public void shouldAddDataSeriesCToB() {
        String expectedSeriesBPlusC = "{\"dataPoints\":[[\"2010-01-01T00:00:00.000Z\",\"0.000000\"],[\"2010-01-02T00:00:00.000Z\",\"1.000000\"],[\"2010-01-03T00:00:00.000Z\",\"2.000000\"],[\"2010-01-04T00:00:00.000Z\",\"5.000000\"]]}";
        DataSeries seriesBPlusC = seriesB.plus(seriesC);
        assertTrue("Should be able to add together two DataSeries objects.", seriesBPlusC.toString().equals(expectedSeriesBPlusC));
    }
}