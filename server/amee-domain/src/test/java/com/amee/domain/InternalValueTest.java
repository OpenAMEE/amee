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
package com.amee.domain;

import com.amee.core.DataSeries;
import com.amee.core.ValueType;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import static junit.framework.Assert.assertTrue;
import org.joda.time.DateTime;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class InternalValueTest {

    DateTime now;
    DateTime one;
    DateTime two;
    DateTime three;
    DateTime max;

    @Before
    public void init() {
        now = new DateTime();
        three = now.plusMinutes(3);
        three = now.plusMinutes(3);
        two = now.plusMinutes(2);
        one = now.plusMinutes(1);
        max = new DateTime(Long.MAX_VALUE);
    }

    @Test
    public void unfilteredTimeSeries() {
        List<ItemValue> values = new ArrayList<ItemValue>();

        ItemValue iv3 = createItemValue(three.toDate());
        values.add(iv3);
        ItemValue iv2 = createItemValue(two.toDate());
        values.add(iv2);
        ItemValue iv1 = createItemValue(one.toDate());
        values.add(iv1);

        InternalValue internal = new InternalValue(values, now.toDate(), max.toDate());
        DataSeries filteredValues = (DataSeries) internal.getValue();
        Collection<DateTime> filteredDates = filteredValues.getDateTimePoints();

        assertTrue("Should contain expected ItemValues",filteredDates.contains(two));
        assertTrue("Should not contain filtered ItemValues",filteredDates.contains(one));
        assertTrue("Should not contain filtered ItemValues",filteredDates.contains(three));
    }

    @Test
    public void filterTimeSeriesWithStartAndEndDates() {
        List<ItemValue> values = new ArrayList<ItemValue>();

        ItemValue iv3 = createItemValue(three.toDate());
        values.add(iv3);
        ItemValue iv2 = createItemValue(two.toDate());
        values.add(iv2);
        ItemValue iv1 = createItemValue(one.toDate());
        values.add(iv1);

        Date start = now.plusMinutes(1).plusSeconds(30).toDate();
        Date end = now.plusMinutes(3).toDate();

        InternalValue internal = new InternalValue(values, start, end);
        DataSeries filteredValues = (DataSeries) internal.getValue();
        Collection<DateTime> filteredDates = filteredValues.getDateTimePoints();

        assertTrue("Should contain expected ItemValues",filteredDates.contains(two));
        assertTrue("Should not contain filtered ItemValues",filteredDates.contains(one));
        assertFalse("Should not contain filtered ItemValues",filteredDates.contains(three));
    }

    @Test
    public void filterTimeSeriesWithStartDate() {
        List<ItemValue> values = new ArrayList<ItemValue>();

        ItemValue iv3 = createItemValue(three.toDate());
        values.add(iv3);
        ItemValue iv2 = createItemValue(two.toDate());
        values.add(iv2);
        ItemValue iv1 = createItemValue(one.toDate());
        values.add(iv1);

        InternalValue internal = new InternalValue(values, two.toDate(), max.toDate());
        DataSeries filteredValues = (DataSeries) internal.getValue();
        Collection<DateTime> filteredDates = filteredValues.getDateTimePoints();

        assertTrue("Should contain expected ItemValues",filteredDates.contains(two));
        assertTrue("Should not contain filtered ItemValues",filteredDates.contains(three));
        assertFalse("Should not contain filtered ItemValues",filteredDates.contains(one));
    }

    private ItemValue createItemValue(Date start) {
        ItemValueDefinition ivd = new ItemValueDefinition();
        ValueDefinition vd = new ValueDefinition();
        vd.setValueType(ValueType.DECIMAL);
        ivd.setValueDefinition(vd);
        ItemValue iv = new ItemValue();
        iv.setValue(start.getTime() + "");
        iv.setItemValueDefinition(ivd);
        iv.setStartDate(start);
        return iv;
    }

}
