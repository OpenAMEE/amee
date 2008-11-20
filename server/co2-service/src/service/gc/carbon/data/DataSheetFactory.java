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

import com.jellymold.sheet.*;
import com.jellymold.utils.cache.Cacheable;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.domain.data.*;
import gc.carbon.domain.profile.StartEndDate;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.List;
import java.util.Map;
import java.util.Date;

@Name("dataSheetFactory")
@Scope(ScopeType.EVENT)
public class DataSheetFactory implements CacheableFactory {

    private final static Logger log = Logger.getLogger(DataSheetFactory.class);

    @In(create = true)
    private DataService dataService;

    private DataCategory dataCategory;
    private StartEndDate startDate = new StartEndDate(new Date());
    private StartEndDate endDate;
    
    public DataSheetFactory() {
        super();
    }

    public Cacheable createCacheable() {

        List<Column> columns;
        Row row;
        Map<String, ItemValue> itemValuesMap;
        ItemValue itemValue;
        Sheet sheet = null;
        ItemDefinition itemDefinition;

        // must have an ItemDefinition
        itemDefinition = dataCategory.getItemDefinition();
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
            for (DataItem dataItem : dataService.getDataItems(dataCategory, startDate, endDate)) {
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
/*
        StringBuffer key = new StringBuffer("DataSheet_");
        key.append(dataCategory.getUid()).append("_");
        key.append(startDate.getTime());
        if (endDate != null) {
            key.append("_" ).append(endDate.getTime());
        }
        return key.toString();
*/
        return "DataSheet_" + dataCategory.getUid();
    }

    public String getCacheName() {
        return "DataSheets";
    }

    public void setDataCategory(DataCategory dataCategory) {
        this.dataCategory = dataCategory;
    }

    public void setDataBrowser(DataBrowser dataBrowser) {
        this.dataCategory = dataBrowser.getDataCategory();
        this.startDate = dataBrowser.getStartDate();
        this.endDate = dataBrowser.getEndDate();
    }
}
