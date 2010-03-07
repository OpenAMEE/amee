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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A class representing a series of {@link DataPoint} values. Provides various mathematical operations
 * such as plus, subtract and multiply along with the crucial integrate method.
 */
public class DataSeries {

    private List<DataPoint> dataPoints = new ArrayList<DataPoint>();

    /// These dates will be used to define a query window on the series.
    private DateTime seriesStartDate;
    private DateTime seriesEndDate;

    /**
     * Construct an empty series.
     */
    public DataSeries() {
        this(new ArrayList<DataPoint>());
    }

    /**
     * Construct a series from the list of {@link DataPoint} values.
     *
     * @param dataPoints - the list of {@link DataPoint} values
     */
    public DataSeries(List<DataPoint> dataPoints) {
        this.dataPoints = new ArrayList<DataPoint>(dataPoints);
    }

    /**
     * A copy constructor.
     *
     * @param dataSeries to copy
     */
    protected DataSeries(DataSeries dataSeries) {
        for (DataPoint dataPoint : dataSeries.dataPoints) {
            this.addDataPoint(new DataPoint(dataPoint));
        }
    }

    /**
     * Return a copy of this object.
     *
     * @return a copy
     */
    public DataSeries copy() {
        return new DataSeries(this);
    }

    public String toString() {
        try {
            return getJSONObject().toString();
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        for (DataPoint dataPoint : dataPoints) {
            arr.put(dataPoint.getJSONArray());
        }
        obj.put("dataPoints", arr);
        if (seriesStartDate != null) {
            obj.put("seriesStartDate", seriesStartDate.toString());
        }
        if (seriesEndDate != null) {
            obj.put("seriesEndDate", seriesEndDate.toString());
        }
        return obj;
    }

    protected Decimal getSeriesTimeInMillis() {
        if (dataPoints.isEmpty()) {
            return Decimal.ZERO;
        }
        DateTime first = dataPoints.get(0).getDateTime();
        DateTime last = dataPoints.get(dataPoints.size() - 1).getDateTime();
        DateTime seriesStart = (seriesStartDate != null) && seriesStartDate.isAfter(first) ? seriesStartDate : first;
        DateTime seriesEnd = (seriesEndDate != null) && last.isAfter(seriesEndDate) ? seriesEndDate : last;
        return new Decimal(seriesEnd.getMillis() - seriesStart.getMillis());
    }

    // Combine this DataSeries with another DataSeries using the given Operation.

    @SuppressWarnings("unchecked")
    private DataSeries combine(DataSeries series, Operation operation) {

        // Create a union of all DateTime points in the two DataSeries and sort the resultant collection (DESC).
        List<DateTime> dateTimePoints = (List) CollectionUtils.union(getDateTimePoints(), series.getDateTimePoints());
        Collections.sort(dateTimePoints);

        List<DataPoint> combinedSeries = new ArrayList<DataPoint>();
        // For each DateTime point, find the nearest corresponding DataPoint in each series and apply the desired
        // Operation.
        for (DateTime dateTimePoint : dateTimePoints) {
            DataPoint lhs = getDataPoint(dateTimePoint);
            DataPoint rhs = series.getDataPoint(dateTimePoint);
            operation.setOperands(lhs, rhs);
            combinedSeries.add(new DataPoint(dateTimePoint, operation.operate().getValue()));
        }
        return new DataSeries(combinedSeries);
    }

    /**
     * Add a DataSeries to this DataSeries.
     *
     * @param series - the DataSeries to add
     * @return a new DataSeries representing the addition of the two DataSeries
     */
    public DataSeries plus(DataSeries series) {
        return combine(series, new PlusOperation());
    }

    /**
     * Add a DataPoint to this DataSeries.
     *
     * @param dataPoint - the DataPoint to add
     * @return a new DataSeries representing the addition of the DataSeries and the DataPoint
     */
    public DataSeries plus(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return plus(series);
    }

    /**
     * Add a float value to this DataSeries.
     *
     * @param f - the float value to add
     * @return a new DataSeries representing the addition of the float value and the DataSeries
     */
    public DataSeries plus(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.plus(f));
        }
        return new DataSeries(combinedDataPoints);
    }

    /**
     * Subtract a DataSeries from this DataSeries.
     *
     * @param series - the DataSeries to subtract
     * @return a new DataSeries representing the subtraction of the DataSeries from this DataSeries
     */
    public DataSeries subtract(DataSeries series) {
        return combine(series, new SubtractOperation());
    }

    /**
     * Subtract a DataPoint from this DataSeries.
     *
     * @param dataPoint - the DataPoint to subtract
     * @return a new DataSeries representing the subtraction of the DataPoint from this DataSeries
     */
    public DataSeries subtract(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return subtract(series);
    }

    /**
     * Subtract a float value from this DataSeries.
     *
     * @param f - the float value to subtract
     * @return a new DataSeries representing the subtraction of the float value from this DataSeries
     */
    public DataSeries subtract(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.subtract(f));
        }
        return new DataSeries(combinedDataPoints);
    }

    /**
     * Divide this DataSeries by another DataSeries.
     *
     * @param series - the DataSeries value by which to divide this DataSeries
     * @return a new DataSeries representing the division of this DataSeries by the DataSeries
     */
    public DataSeries divide(DataSeries series) {
        return combine(series, new DivideOperation());
    }

    /**
     * Divide this DataSeries by a DataPoint.
     *
     * @param dataPoint - the DataPoint value by which to divide this DataSeries
     * @return a new DataSeries representing the division of this DataSeries by the DataPoint
     */
    public DataSeries divide(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return divide(series);
    }

    /**
     * Divide this DataSeries by a float value.
     *
     * @param f - the float value by which to divide this DataSeries
     * @return a new DataSeries representing the division of this DataSeries by the float value
     */
    public DataSeries divide(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.divide(f));
        }
        return new DataSeries(combinedDataPoints);
    }

    /**
     * Multiply this DataSeries by another DataSeries.
     *
     * @param series - the DataSeries to multiply this DataSeries
     * @return a new DataSeries representing the multiplication of the two DataSeries
     */
    public DataSeries multiply(DataSeries series) {
        return combine(series, new MultiplyOperation());
    }

    /**
     * Multiply this DataSeries by a DataPoint.
     *
     * @param dataPoint - the DataPoint value to multiply this DataPoint
     * @return a new DataSeries representing the multiplication of the DataSeries and the DataPoint
     */
    public DataSeries multiply(DataPoint dataPoint) {
        DataSeries series = new DataSeries();
        series.addDataPoint(dataPoint);
        return multiply(series);
    }

    /**
     * Multiply this DataSeries by a float value.
     *
     * @param f - the float value to multiply this DataSeries
     * @return a new DataSeries representing the multiplication of the DataSeries and the float value
     */
    public DataSeries multiply(float f) {
        List<DataPoint> combinedDataPoints = new ArrayList<DataPoint>();
        for (DataPoint dp : dataPoints) {
            combinedDataPoints.add(dp.multiply(f));
        }
        return new DataSeries(combinedDataPoints);
    }

    /**
     * Get the single-valued average of the DataPoints within the DataSeries that occur during the
     * specified query time-period.
     * <p/>
     * If there is no time-period (the query time-period is zero) then the result will be zero.
     *
     * @return - the average as a {@link Decimal} value
     */
    public Decimal integrate() {
        Decimal integral = Decimal.ZERO;
        Decimal seriesTimeInMillis = getSeriesTimeInMillis();
        if (!seriesTimeInMillis.equals(Decimal.ZERO)) {
            Collections.sort(dataPoints);
            for (int i = 0; i < dataPoints.size() - 1; i++) {
                // Work out segment time series.
                DataPoint current = dataPoints.get(i);
                DataPoint next = dataPoints.get(i + 1);
                Decimal segmentInMillis = new Decimal(next.getDateTime().getMillis() - current.getDateTime().getMillis());
                // Add weighted average value.
                Decimal weightedAverage = current.getValue().multiply(segmentInMillis.divide(seriesTimeInMillis));
                integral = integral.add(weightedAverage);
            }
        }
        return integral;
    }

    /**
     * Get the Collection of {@link org.joda.time.DateTime} points in the DataSeries.
     *
     * @return the Collection of {@link org.joda.time.DateTime} points in the DataSeries
     */
    @SuppressWarnings("unchecked")
    public Collection<DateTime> getDateTimePoints() {
        return (Collection<DateTime>) CollectionUtils.collect(dataPoints, new Transformer() {
            public Object transform(Object input) {
                DataPoint dataPoint = (DataPoint) input;
                return dataPoint.getDateTime();
            }
        });
    }

    /**
     * Get the active {@link DataPoint} at a specific point in time.
     *
     * @param dateTime - the point in time for which to return the {@link DataPoint}
     * @return the {@link DataPoint} at dateTime
     */
    public DataPoint getDataPoint(DateTime dateTime) {
        DataPoint selected = DataPoint.NULL;
        for (DataPoint dataPoint : dataPoints) {
            if (!dataPoint.getDateTime().isAfter(dateTime)) {
                selected = dataPoint;
            } else {
                break;
            }
        }
        return selected;
    }

    /**
     * Add a {@link DataPoint} to this series.
     *
     * @param dataPoint - the {@link DataPoint} to add to this series.
     */
    public void addDataPoint(DataPoint dataPoint) {
        dataPoints.add(dataPoint);
    }

    /**
     * Set the start of the query window.
     *
     * @param seriesStartDate - the start of the query window
     */
    public void setSeriesStartDate(DateTime seriesStartDate) {
        if (seriesStartDate == null)
            return;
        this.seriesStartDate = seriesStartDate;
    }

    /**
     * Set the end of the query window.
     *
     * @param seriesEndDate - the end of the query window
     */
    public void setSeriesEndDate(DateTime seriesEndDate) {
        if (seriesEndDate == null)
            return;
        this.seriesEndDate = seriesEndDate;
    }
}

/**
 * Represents an abstract mathematical operation
 * one would want to perform on a pair of {@link DataPoint} values.
 */
abstract class Operation {

    protected DataPoint lhs;
    protected DataPoint rhs;

    void setOperands(DataPoint lhs, DataPoint rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    abstract DataPoint operate();
}

class PlusOperation extends Operation {
    public DataPoint operate() {
        return lhs.plus(rhs);
    }
}

class SubtractOperation extends Operation {
    public DataPoint operate() {
        return lhs.subtract(rhs);
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
