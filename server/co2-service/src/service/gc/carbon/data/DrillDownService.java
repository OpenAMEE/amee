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
package gc.carbon.data;

import com.jellymold.sheet.Choice;
import com.jellymold.sheet.Choices;
import com.jellymold.sheet.Sheet;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemDefinition;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

@Name("drillDownService")
@Scope(ScopeType.EVENT)
public class DrillDownService implements Serializable {

    private final static Logger log = Logger.getLogger(DrillDownService.class);

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private DataSheetService dataSheetService;


    public Choices getChoices(DataCategory dataCategory, List<Choice> selections, Date startDate, Date endDate) {
        String name = "uid";
        List<Choice> choiceList = new ArrayList<Choice>();
        DataBrowser browser = new DataBrowser();
        browser.setDataCategory(dataCategory);
        browser.setStartDate(startDate);
        browser.setEndDate(endDate);
        Sheet sheet = dataSheetService.getSheet(browser);
        ItemDefinition itemDefinition = dataCategory.getItemDefinition();

        // will only have sheet if itemDefinition was avaialable for this dataCategory
        if (sheet != null) {

            // get filtered sheet copy
            sheet = Sheet.getCopy(sheet, selections);

            // produce choices list
            if (itemDefinition.hasDrillDownAvailable()) {
                // obtain drill down choices and selections
                List<Choice> drillDownChoices = itemDefinition.getDrillDownChoices();
                // re-order selection list to match choices
                for (Choice choice : drillDownChoices) {
                    int selectionIndex = selections.indexOf(choice);
                    if (selectionIndex >= 0) {
                        selections.add(selections.remove(selectionIndex));
                    }
                }
                // remove selections not in choices
                for (Choice selection : selections) {
                    if (!drillDownChoices.contains(selection)) {
                        selections.remove(selection);
                    }
                }
                // remove drill down choices that have been selected
                Iterator<Choice> iterator = drillDownChoices.iterator();
                while (iterator.hasNext()) {
                    Choice choice = iterator.next();
                    if (selections.contains(choice)) {
                        iterator.remove();
                    }
                }
                // TODO: choices from itemValueDefinition have priority
                // get distinct choices from sheet based on first column specified in drill down
                if (drillDownChoices.size() > 0) {
                    name = drillDownChoices.get(0).getName();
                    choiceList = sheet.getChoices(name);
                } else {
                    // just return choices list for uid column
                    choiceList = sheet.getChoices("uid");
                }
            } else {
                // just return choices list for uid column
                choiceList = sheet.getChoices("uid");
            }
        }

        // skip ahead if we only have one choice that is not "uid"
        if (!name.equals("uid") && (choiceList.size() == 1)) {
            selections.add(new Choice(name, choiceList.get(0).getValue()));
            return getChoices(dataCategory, selections);
        } else {
            // wrap result in Choices object
            return new Choices(name, choiceList);
        }
    }

    public Choices getChoices(DataCategory dataCategory) {
        return getChoices(dataCategory, new ArrayList<Choice>());
    }

    public Choices getChoices(DataCategory dataCategory, List<Choice> selections) {
        return getChoices(dataCategory, selections, new Date(), null);
    }
}