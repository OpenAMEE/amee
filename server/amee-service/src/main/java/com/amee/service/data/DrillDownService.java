/**
 * This file is part of AMEE.
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

import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.domain.profile.StartEndDate;
import com.amee.domain.cache.CacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class DrillDownService implements Serializable {

    @Autowired
    private DrillDownDAO drillDownDao;

    private CacheHelper cacheHelper = CacheHelper.getInstance();

    public Choices getValueChoices(DataCategory dataCategory, List<Choice> selections, Date startDate, Date endDate) {

        ItemDefinition itemDefinition;
        List<Choice> drillDownChoices = null;
        String name;
        List<Choice> values = new ArrayList<Choice>();

        // must have a startDate
        // default to start of month
        startDate = (startDate != null ? startDate : StartEndDate.getStartOfMonthDate());

        // we can do a drill down if an itemDefinition is attached to current dataCategory
        itemDefinition = dataCategory.getItemDefinition();
        if (itemDefinition != null) {

            // obtain drill down choices
            drillDownChoices = itemDefinition.getDrillDownChoices();

            // fix-up selections and drill downs
            matchSelectionOrderToDrillDownChoices(drillDownChoices, selections);
            removeSelectionsNotInDrillDownChoices(drillDownChoices, selections);
            removeDrillDownChoicesThatHaveBeenSelected(drillDownChoices, selections);

            // get drill down values
            values = getDataItemChoices(dataCategory, startDate, endDate, selections, drillDownChoices);
        }

        // work out name
        if ((drillDownChoices != null) && (drillDownChoices.size() > 0)) {
            name = drillDownChoices.get(0).getName();
        } else {
            name = "uid";
        }

        // skip ahead if we only have one value that is not "uid"
        if (!name.equals("uid") && (values.size() == 1)) {
            selections.add(new Choice(name, values.get(0).getValue()));
            return getValueChoices(dataCategory, selections, startDate, endDate);
        } else {
            // wrap result in Choices object
            return new Choices(name, values);
        }
    }

    private List<Choice> getDataItemChoices(
            DataCategory dataCategory,
            Date startDate,
            Date endDate,
            List<Choice> selections,
            List<Choice> drillDownChoices) {
        return (List<Choice>) cacheHelper.getCacheable(
                new DrillDownFactory(
                        drillDownDao,
                        dataCategory,
                        startDate,
                        endDate,
                        selections,
                        drillDownChoices));
    }

    protected void matchSelectionOrderToDrillDownChoices(List<Choice> drillDownChoices, List<Choice> selections) {
        for (Choice c : drillDownChoices) {
            int selectionIndex = selections.indexOf(c);
            if (selectionIndex >= 0) {
                selections.add(selections.remove(selectionIndex));
            }
        }
    }

    protected void removeDrillDownChoicesThatHaveBeenSelected(List<Choice> drillDownChoices, List<Choice> selections) {
        Iterator<Choice> iterator;
        Choice choice;
        iterator = drillDownChoices.iterator();
        while (iterator.hasNext()) {
            choice = iterator.next();
            if (selections.contains(choice)) {
                iterator.remove();
            }
        }
    }

    protected void removeSelectionsNotInDrillDownChoices(List<Choice> drillDownChoices, List<Choice> selections) {
        Iterator<Choice> iterator;
        Choice choice;
        iterator = selections.iterator();
        while (iterator.hasNext()) {
            choice = iterator.next();
            if (!drillDownChoices.contains(choice)) {
                iterator.remove();
            }
        }
    }

    public Choices getChoices(DataCategory dataCategory, List<Choice> selections) {
        return getValueChoices(dataCategory, selections, null, null);
    }
}