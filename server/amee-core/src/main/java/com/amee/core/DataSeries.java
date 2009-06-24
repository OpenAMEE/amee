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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DataSeries {

    private List<DataPoint> dataPoints = new ArrayList<DataPoint>();
    private DateTime queryStart;
    private DateTime queryEnd;
    private Decimal queryInMillis;

    public DataSeries() {
        this(new ArrayList<DataPoint>());
    }

    //TODO - Checks  - Null / Empty
    public DataSeries(List<DataPoint> dataPoints) {
        this.dataPoints = new ArrayList<DataPoint>(dataPoints);
        if (!dataPoints.isEmpty()) {
            this.queryStart = dataPoints.get(0).getDateTime();
            this.queryEnd = dataPoints.get(dataPoints.size() -1).getDateTime();
            setQueryInMillis();
        }
    }

    public void setQueryStart(DateTime queryStart) {
        if (queryStart == null)
            return;

        if (queryStart.isAfter(this.queryStart)) {
            this.queryStart = queryStart;
            setQueryInMillis();
        }
    }

    public void setQueryEnd(DateTime queryEnd) {
        if (queryEnd == null)
            return;

        if (queryEnd.isBefore(this.queryEnd)) {
            this.queryEnd = queryEnd;
            setQueryInMillis();
        }
    }

    private void setQueryInMillis() {
        this.queryInMillis = new Decimal(queryEnd.getMillis() - queryStart.getMillis());
    }

    @SuppressWarnings("unchecked")
    private DataSeries combine(DataSeries series, Operation operation) {

        List<DateTime> dateTimePoints = (List) CollectionUtils.union(getDateTimePoints(), series.getDateTimePoints());
        Collections.sort(dateTimePoints);

        List<DataPoint> combinedSeries = new ArrayList<DataPoint>();
        for(DateTime dateTimePoint : dateTimePoints) {
            DataPoint lhs = getDataPoint(dateTimePoint);
            DataPoint rhs = series.getDataPoint(dateTimePoint);
            operation.setOperands(lhs, rhs);
            combinedSeries.add(operation.operate());
        }

        return new DataSeries(combinedSeries);

    }

    public DataSeries add(DataSeries series) {
        return combine(series, new AddOperation());
    }

    public DataSeries add(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return add(series);
    }

    public DataSeries add(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.add(f));
        }
        return new DataSeries(combinedDataPoints); 
    }

    @SuppressWarnings("unchecked")
    public DataSeries subtract(DataSeries series) {
        return combine(series, new SubtractOperation());
    }

    public DataSeries subtract(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return subtract(series);
    }

    public DataSeries subtract(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.substract(f));
        }
        return new DataSeries(combinedDataPoints);
    }
    
    public DataSeries divide(DataSeries series) {
        return combine(series, new DivideOperation());
    }

    public DataSeries divide(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return divide(series);
    }

    public DataSeries divide(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.divide(f));
        }
        return new DataSeries(combinedDataPoints);
    }

    public DataSeries multiply(DataSeries series) {
        return combine(series, new MultiplyOperation());
    }

    public DataSeries multiply(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return multiply(series);
    }

    public DataSeries multiply(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.multiply(f));
        }
        return new DataSeries(combinedDataPoints);
    }

    public Decimal integrate() {
        Decimal integral = Decimal.ZERO;
        Collections.sort(dataPoints);
        for (int i = 0; i < dataPoints.size()-1; i++) {
            DataPoint current = dataPoints.get(i);
            DataPoint next = dataPoints.get(i+1);
            Decimal segmentInMillis = new Decimal(next.getDateTime().getMillis() - current.getDateTime().getMillis());
            Decimal weightedAverage = current.getValue().multiply(segmentInMillis.divide(queryInMillis));
            integral = integral.add(weightedAverage);
        }
        return integral;
    }


    @SuppressWarnings("unchecked")
    public Collection<DateTime> getDateTimePoints() {
        return (Collection<DateTime>) CollectionUtils.collect(dataPoints, new Transformer() {
            @Override
            public Object transform(Object input) {
                DataPoint dataPoint = (DataPoint) input;
                return dataPoint.getDateTime();
            }
        });
    }

    //TODO - Optimise the reverse
    public DataPoint getDataPoint(DateTime selector) {
        DataPoint selected = DataPoint.NULL;
        for (DataPoint dataPoint : dataPoints) {
            if (!dataPoint.getDateTime().isAfter(selector)) {
                selected = dataPoint;
            } else {
                break;
            }
        }
        return selected;
    }


    public void addDataPoint(DataPoint dataPoint) {
        dataPoints.add(dataPoint);
        //TODO - A having added a new DP to a DS
    }

}

abstract class Operation {

    protected DataPoint lhs;
    protected DataPoint rhs;

    void setOperands(DataPoint lhs, DataPoint rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    abstract DataPoint operate();
}

class AddOperation extends Operation {
    public DataPoint operate() {
        return lhs.add(rhs);
    }
}

class SubtractOperation extends Operation {
    public DataPoint operate() {
        return lhs.substract(rhs);
    }
}

class DivideOperation extends Operation {
    public DataPoint operate() {
        return lhs.divide(rhs);
    }
}

class MultiplyOperation extends Operation {
    public DataPoint operate() {
        return lhs.multiply(rhs);
    }
}
