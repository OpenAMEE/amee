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

import com.jellymold.kiwi.Environment;
import com.jellymold.sheet.Choice;
import com.jellymold.sheet.Choices;
import gc.carbon.ObjectType;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemGroup;
import gc.carbon.path.PathItemService;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;

@Name("dataFinder")
@Scope(ScopeType.EVENT)
public class DataFinder implements Serializable {

    private final static Logger log = Logger.getLogger(DataFinder.class);

    @In(create = true)
    private PathItemService pathItemService;

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private DrillDownService drillDownService;

    @In
    private Environment environment;

    public DataFinder() {
        super();
    }

    public String getDataItemValue(String path, String drillDown, String name) {
        String value = null;
        ItemValue itemValue;
        DataItem dataItem = getDataItem(path, drillDown);
        if (dataItem != null) {
            itemValue = dataItem.getItemValuesMap().get(name);
            if (itemValue != null) {
                value = itemValue.getValue();
            }
        }
        return value;
    }

    public DataItem getDataItem(String path, String drillDown) {
        DataItem dataItem = null;
        Choices choices;
        DataCategory dataCategory = getDataCategory(path);
        if (dataCategory != null) {
            choices = drillDownService.getChoices(dataCategory, Choice.parseChoices(drillDown));
            if (choices.getName().equals("uid") && (choices.getChoices().size() > 0)) {
                dataItem = dataService.getDataItem(dataCategory, choices.getChoices().get(0).getValue());
            }
        }
        return dataItem;
    }

    public DataCategory getDataCategory(String path) {
        DataCategory dataCategory = null;
        PathItemGroup pig;
        PathItem pi;
        pig = pathItemService.getPathItemGroup(environment);
        if (pig != null) {
            pi = pig.findByPath(path);
            if ((pi != null) && pi.getObjectType().equals(ObjectType.DC)) {
                dataCategory = dataService.getDataCategory(environment, pi.getUid());
            }
        }
        return dataCategory;
    }
}
