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
package com.amee.calculation;

import com.amee.calculation.model.DataPoint;
import com.amee.calculation.model.DataSeries;
import com.amee.domain.core.Decimal;
import org.joda.time.DateTime;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DecimalTest {

    @Test
    public void testAdd() {

        DateTime now = new DateTime();

        List<DataPoint> a = new ArrayList<DataPoint>();
        a.add(new DataPoint(now.plusDays(1), new Decimal("1")));
        a.add(new DataPoint(now.plusDays(2), new Decimal("2")));
        a.add(new DataPoint(now.plusDays(3), new Decimal("3")));

        List<DataPoint> b = new ArrayList<DataPoint>();
        b.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        b.add(new DataPoint(now.plusDays(2), new Decimal("3")));
        b.add(new DataPoint(now.plusDays(3), new Decimal("4")));


        List<DataPoint> ab = new ArrayList<DataPoint>();
        ab.add(new DataPoint(now.plusDays(1), new Decimal("3")));
        ab.add(new DataPoint(now.plusDays(2), new Decimal("5")));
        ab.add(new DataPoint(now.plusDays(3), new Decimal("7")));

        DataSeries s1 = new DataSeries(a);
        DataSeries s2 = new DataSeries(b);

        DataSeries s1s2 = s1.add(s2);

        DataSeries s3 = new DataSeries(ab);

        assertTrue("Integrate should produce the correct value",s1s2.integrate().equals(s3.integrate()));

        //TODO - Add add(DataPoint)
    }

}
