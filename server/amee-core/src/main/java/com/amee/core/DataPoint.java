/*
 *
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

/**
 * Represents a single-valued decimal data point at a single instance of time.
 */
public class DataPoint implements Comparable<DataPoint> {

    private static final DateTime EPOCH = new DateTime(0);

    /**
     * Represents the concept of a zero-valued DataPoint occuring at the EPOCH (<code>DateTime(0)</code>)
     */
    public static final DataPoint NULL = new DataPoint(EPOCH,Decimal.ZERO);

    private DateTime dateTime;
    private Decimal decimal;

    /**
     * Construct a DataPoint with a decimal value occurring at the epoch.
     *
     * @param decimal - the decimal value
     */
    public DataPoint(float decimal) {
        this(new Decimal(decimal));
    }

    /**
     * Construct a DataPoint with a decimal value occurring at the epoch.
     *
     * @param decimal - the decimal value
     */
    public DataPoint(Decimal decimal) {
        this(EPOCH, decimal);
    }


    /**
     * Construct a DataPoint with a decimal value occurring at a specific point in time.
     *
     * @param decimal - the decimal value
     * @param dateTime - the point in time at which this data point occurs.
     */
    public DataPoint(DateTime dateTime, Decimal decimal) {
        this.dateTime = dateTime;
        this.decimal = decimal;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public Decimal getValue() {
        return decimal;
    }

    /**
     * Add a DataPoint to this DataPoint.
     *
     * @param point - the DataPoint to add
     * @return a new DataPoint representing the addition of the two DataPoint values
     */
    public DataPoint add(DataPoint point) {
        return new DataPoint(dateTime, decimal.add(point.getValue()));
    }

    /**
     * Add a float value to this DataPoint.
     *
     * @param f - the float to add
     * @return a new DataPoint representing the addition of the DataPoint and float values
     */
    public DataPoint add(float f) {
        return new DataPoint(dateTime, decimal.add(new Decimal(f)));
    }

    /**
     * Subtract a DataPoint from this DataPoint.
     *
     * @param point - the DataPoint to subtract
     * @return a new DataPoint representing the subtraction of the DataPoint from this DataPoint
     */
    public DataPoint substract(DataPoint point) {
        return new DataPoint(dateTime, decimal.subtract(point.getValue()));
    }

    /**
     * Subtract a float value from this DataPoint.
     *
     * @param f - the float to subtract
     * @return a new DataPoint representing the subtraction of the float value from this DataPoint
     */
    public DataPoint substract(float f) {
        return new DataPoint(dateTime, decimal.subtract(new Decimal(f)));
    }

    /**
     * Divide this DataPoint by another DataPoint.
     *
     * @param point - the DataPoint by which to divide this DataPoint
     * @return a new DataPoint representing the division of this DataPoint by the DataPoint
     */
    public DataPoint divide(DataPoint point) {
        return new DataPoint(dateTime, decimal.divide(point.getValue()));
    }

    /**
     * Divide this DataPoint by a float value.
     *
     * @param f - the float value by which to divide this DataPoint
     * @return a new DataPoint representing the division of this DataPoint by the float value
     */
    public DataPoint divide(float f) {
        return new DataPoint(dateTime, decimal.divide(new Decimal(f)));
    }

    /**
      * Multiply this DataPoint by another DataPoint.
      *
      * @param point - the DataPoint to multiply this DataPoint
      * @return a new DataPoint representing the multiplication of the two DataPoints
      */
    public DataPoint multiply(DataPoint point) {
        return new DataPoint(dateTime, decimal.multiply(point.getValue()));
    }

    /**
     * Multiply this DataPoint by a float value.
     *
     * @param f - the float value to multiply this DataPoint
     * @return a new DataPoint representing the multiplication of this DataPoint by the float value.
     */
    public DataPoint multiply(float f) {
        return new DataPoint(dateTime, decimal.multiply(new Decimal(f)));
    }

    @Override
    public int compareTo(DataPoint that) {
        //Collections of DataPoint values will be ordered by ??
        return getDateTime().compareTo(that.getDateTime());
    }
}