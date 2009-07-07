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

import com.amee.core.ValueType;
import com.amee.domain.StartEndDate;
import com.amee.domain.cache.CacheableFactory;
import com.amee.domain.data.*;
import com.amee.domain.sheet.Cell;
import com.amee.domain.sheet.Column;
import com.amee.domain.sheet.Row;
import com.amee.domain.sheet.Sheet;

import java.util.List;

public class DataSheetFactory implements CacheableFactory {

    private DataService dataService;
    private DataBrowser dataBrowser;

    private DataSheetFactory() {
        super();
    }

    public DataSheetFactory(DataService dataService, DataBrowser dataBrowser) {
        this();
        this.dataService = dataService;
        this.dataBrowser = dataBrowser;
    }

    public Object create() {

        List<Column> columns;
        Row row;
        ItemValueMap itemValuesMap;
        ItemValue itemValue;
        Sheet sheet = null;
        ItemDefinition itemDefinition;

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
            StartEndDate startDate = dataBrowser.getQueryStartDate();
            StartEndDate endDate = dataBrowser.getQueryEndDate();
            for (DataItem dataItem : new OnlyActiveDataService(dataService).getDataItems(dataBrowser.getDataCategory(), startDate, endDate)) {
                row = new Row(sheet, dataItem.getUid());
                row.setLabel("DataItem");
                for (Column column : columns) {
                    itemValue = dataItem.matchItemValue(column.getName(),startDate);
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
                        // addItemValue empty cell
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
        return "DataSheet_" + dataBrowser.getDataCategory().getUid() +
                "_" +
                dataBrowser.getQueryStartDate() +
                "_" +
                ((dataBrowser.getQueryEndDate() != null) ? dataBrowser.getQueryEndDate() : "");
    }

    public String getCacheName() {
        return "DataSheets";
    }
}
