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
package com.amee.calculation.model;

import com.amee.domain.core.Decimal;
import org.joda.time.DateTime;

public class DataPoint implements Comparable<DataPoint> {

    public static final DataPoint NULL = new DataPoint(new DateTime(0),Decimal.ZERO); 

    public DateTime dateTime;
    public Decimal decimal;

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

    public DataPoint add(DataPoint point) {
        return new DataPoint(dateTime, decimal.add(point.getValue()));
    }

    public DataPoint substract(DataPoint point) {
        return null;
    }

    public DataPoint divide(DataPoint point) {
        return null;
    }

    public DataPoint multiply(DataPoint point) {
        return null;
    }

    //TODO - Comment with meaning of compareTo vs Equals
    @Override
    public int compareTo(DataPoint that) {
        return getDateTime().compareTo(that.getDateTime());
    }
}