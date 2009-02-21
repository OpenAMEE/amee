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

import com.jellymold.sheet.Cell;
import com.jellymold.sheet.Column;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.ValueType;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.profile.StartEndDate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataSheetFactory implements CacheableFactory {

    @Autowired
    private DataService dataService;

    public DataSheetFactory() {
        super();
    }

    public Object create() {

        List<Column> columns;
        Row row;
        Map<String, ItemValue> itemValuesMap;
        ItemValue itemValue;
        Sheet sheet = null;
        ItemDefinition itemDefinition;
        DataBrowser dataBrowser = (DataBrowser) ThreadBeanHolder.get("dataBrowserForFactory");

        // must have an ItemDefinition
        itemDefinition = dataBrowser.getDataCategory().getItemDefinition();
        if (itemDefinition != null) {

            // create sheet and columns     
            sheet = new Sheet();
            sheet.setKey(getKey());
            sheet.setLabel("DataItems");
            for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                if (itemValueDefinition.isFromData()) {
                    new Column(sheet, itemValueDefinition.getPath(), itemValueDefinition.getName());
                }
            }
            new Column(sheet, "label");
            new Column(sheet, "path");
            new Column(sheet, "uid", true);
            new Column(sheet, "created", true);
            new Column(sheet, "modified", true);
            new Column(sheet, "startDate");
            new Column(sheet, "endDate");

            // create rows and cells
            columns = sheet.getColumns();
            StartEndDate startDate = dataBrowser.getStartDate();
            StartEndDate endDate = dataBrowser.getEndDate();
            // TODO - Will need to switch between OnlyActive - for DC GET and EarliestActive for Finder - tho need to ask AC this
            for (DataItem dataItem : new OnlyActiveDataService(dataService).getDataItems(dataBrowser.getDataCategory(), startDate, endDate)) {
                itemValuesMap = dataItem.getItemValuesMap();
                row = new Row(sheet, dataItem.getUid());
                row.setLabel("DataItem");
                for (Column column : columns) {
                    itemValue = itemValuesMap.get(column.getName());
                    if (itemValue != null) {
                        new Cell(column, row, itemValue.getValue(), itemValue.getUid(), itemValue.getItemValueDefinition().getValueDefinition().getValueType());
                    } else if ("label".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, dataItem.getLabel(), ValueType.TEXT);
                    } else if ("path".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, dataItem.getDisplayPath(), ValueType.TEXT);
                    } else if ("uid".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, dataItem.getUid(), ValueType.TEXT);
                    } else if ("created".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, dataItem.getCreated(), ValueType.DATE);
                    } else if ("modified".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, dataItem.getModified(), ValueType.DATE);
                    } else if ("startDate".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, dataItem.getStartDate(), ValueType.DATE);
                    } else if ("endDate".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, dataItem.getEndDate(), ValueType.DATE);
                    } else {
                        // add empty cell
                        new Cell(column, row);
                    }
                }
            }

            // sort columns and rows in sheet
            sheet.setDisplayBy(itemDefinition.getDrillDown());
            sheet.sortColumns();
            sheet.setSortBy(itemDefinition.getDrillDown());
            sheet.sortRows();
        }

        return sheet;
    }

    public String getKey() {
        DataBrowser browser = (DataBrowser) ThreadBeanHolder.get("dataBrowserForFactory");
        return "DataSheet_" + browser.getDataCategory().getUid() + "_" + browser.getStartDate().getTime() +
                ((browser.getEndDate() != null) ? "_" + browser.getEndDate().getTime() : "");
    }

    public String getCacheName() {
        return "DataSheets";
    }
}
