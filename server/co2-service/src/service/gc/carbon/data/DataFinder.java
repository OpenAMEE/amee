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

import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.sheet.Choice;
import com.jellymold.sheet.Choices;
import gc.carbon.domain.ObjectType;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.path.PathItemGroup;
import gc.carbon.path.PathItemService;
import gc.carbon.data.DataServiceDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Date;

@Service
@Scope("prototype")
public class DataFinder implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private DataServiceDAO dataServiceDAO;

    @Autowired
    private DrillDownService drillDownService;

    private Date startDate;
    private Date endDate;

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
            choices = drillDownService.getChoices(dataCategory, Choice.parseChoices(drillDown), startDate, endDate);
            if (choices.getName().equals("uid") && (choices.getChoices().size() > 0)) {
                dataItem = dataServiceDAO.getDataItem(choices.getChoices().get(0).getValue());
            }
        }
        return dataItem;
    }

    public DataCategory getDataCategory(String path) {
        DataCategory dataCategory = null;
        PathItemGroup pig;
        PathItem pi;
        pig = pathItemService.getPathItemGroup();
        if (pig != null) {
            pi = pig.findByPath(path);
            if ((pi != null) && pi.getObjectType().equals(ObjectType.DC)) {
                dataCategory = dataServiceDAO.getDataCategory(pi.getUid());
            }
        }
        return dataCategory;
    }


    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}