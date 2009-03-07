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
package com.amee.service.data;

import com.amee.core.ValueType;
import com.amee.domain.cache.CacheableFactory;
import com.amee.domain.data.*;
import com.amee.domain.profile.StartEndDate;
import com.amee.domain.sheet.*;
import com.amee.service.ThreadBeanHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Date;

public class DrillDownFactory implements CacheableFactory {

    private DrillDownDAO drillDownDao;
    private DataCategory dataCategory;
    private Date startDate;
    private Date endDate;
    private List<Choice> selections;
    private List<Choice> drillDownChoices;

    private DrillDownFactory() {
        super();
    }

    public DrillDownFactory(
            DrillDownDAO drillDownDao,
            DataCategory dataCategory,
            Date startDate,
            Date endDate,
            List<Choice> selections,
            List<Choice> drillDownChoices) {
        this();
        this.drillDownDao = drillDownDao;
        this.dataCategory = dataCategory;
        this.startDate = startDate;
        this.endDate = endDate;
        this.selections = selections;
        this.drillDownChoices = drillDownChoices;
    }

    // TODO: give choices from itemValueDefinition priority?
    public Object create() {
        // have we reached the end of the choices list?
        if (drillDownChoices.size() > 0) {
            // get DataItem value choice list
            return drillDownDao.getDataItemValueChoices(
                    dataCategory,
                    startDate,
                    endDate,
                    selections,
                    drillDownChoices.get(0).getName());
        } else {
            // get DataItem UID choice list
            return drillDownDao.getDataItemUIDChoices(
                    dataCategory,
                    startDate,
                    endDate,
                    selections);
        }
    }

    public String getKey() {
        StringBuilder key = new StringBuilder();
        key.append("DrillDown_");
        key.append(dataCategory.getUid());
        if (startDate != null) {
            key.append("_SD_");
            key.append(startDate.getTime());
        }
        if (endDate != null) {
            key.append("_ED_");
            key.append(endDate.getTime());
        }
        for (Choice selection : selections) {
            key.append("_SL_");
            key.append(selection.getValue().hashCode());
        }
        return key.toString();
    }

    public String getCacheName() {
        return "DrillDownChoices";
    }
}