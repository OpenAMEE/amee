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
package com.amee.googleds;

import com.amee.domain.StartEndDate;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.engine.Engine;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import com.amee.service.profile.ProfileService;
import com.amee.service.transaction.TransactionController;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import org.joda.time.DateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ProfileCategoryServlet extends DataSourceServlet {

    private TransactionController transactionController;
    private ProfileService profileService;
    private PathItemService pathItemService;
    private DataService dataService;

    @Override
    public void init() throws ServletException {
        super.init();
        transactionController = (TransactionController) Engine.getAppContext().getBean("transactionController");
        dataService = (DataService) Engine.getAppContext().getBean("dataService");
        profileService = (ProfileService) Engine.getAppContext().getBean("profileService");
        pathItemService = (PathItemService) Engine.getAppContext().getBean("pathItemService");
    }

	public DataTable generateDataTable(Query query, HttpServletRequest request) {

        try {

            transactionController.begin(false);

            // Create a data-table,
            DataTable table = new DataTable();

            List<ProfileItem> profileItems = getProfileItems(request.getPathInfo());

            for (int i=0; i<profileItems.size(); i++) {

                if (i==0) {
                    addColumnHeadings(profileItems.get(i), table);
                }

                try {
                    addRow(profileItems.get(i), table);

                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }

            return table;

        } finally {
            transactionController.end();
        }

	}

	private void addRow(ProfileItem item, DataTable table) throws TypeMismatchException {

		TableRow row = new TableRow();

        for (ColumnDescription col : table.getColumnDescriptions()) {
            if (col.getId().equals("co2")) {
                row.addCell(item.getAmount().getValue().floatValue());
            } else if (col.getId().equals("startDate"))  {
                DateTimeValue dateTime = getDateTime(item);
                row.addCell(dateTime);
            } else {
                ItemValue itemValue = item.getItemValue(col.getId());
                if (itemValue != null) {
                    addValue(col, row, itemValue);
                } else {
                    addValue(col, row, item.getDataItem().getItemValue(col.getId()));    
                }
            }
        }
        table.addRow(row);
	}

    private void addValue(ColumnDescription col, TableRow row, ItemValue itemValue) {
        if (col.getType().equals(ValueType.NUMBER)) {
            row.addCell(Float.parseFloat(itemValue.getValue()));
        } else {
            row.addCell(itemValue.getValue());
        }
    }

    private DateTimeValue getDateTime(ProfileItem item) {
        DateTime sdate = item.getStartDate().toDateTime();
        return new DateTimeValue(sdate.getYear(), sdate.getMonthOfYear(),
                sdate.getDayOfMonth(), sdate.getHourOfDay(), sdate.getMinuteOfHour(),
                sdate.getSecondOfMinute(), sdate.getMillisOfSecond());
    }

    private void addColumnHeadings(ProfileItem profileItem, DataTable table) {

		ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();

        cd.add(new ColumnDescription("co2",ValueType.NUMBER, "co2"));
        cd.add(new ColumnDescription("startDate",ValueType.DATETIME, "Start Date"));

        addItemValues(profileItem.getItemValues(), cd);
        addItemValues(profileItem.getDataItem().getItemValues(), cd);

        table.addColumns(cd);
	}

    private void addItemValues(List<ItemValue> itemValues, ArrayList<ColumnDescription> cd) {
        for (ItemValue itemValue : itemValues) {
            String name = itemValue.getName();
            String path = itemValue.getPath();
            ItemValueDefinition itemValueDefinition = itemValue.getItemValueDefinition();
            ValueType type;
            if (itemValueDefinition.isDecimal()) {
                type = ValueType.NUMBER;
            } else if (itemValueDefinition.isDate()) {
                type = ValueType.DATETIME;
            } else {
                type = ValueType.TEXT;
            }
            cd.add(new ColumnDescription(path, type, name));
        }
    }

    private List<ProfileItem> getProfileItems(String path) {

        Environment environment = ((EnvironmentService)
                Engine.getAppContext().getBean("environmentService")).getEnvironmentByName("AMEE");

        String[] segmentArray = path.substring(1).split("\\.")[0].split("/");

        List<String> segments = new ArrayList<String>(segmentArray.length);
        for (String s : segmentArray) {
            segments.add(s);
        }
        String profileUid = segments.remove(0);
        PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environment);
        PathItem pathItem = pathItemGroup.findBySegments(segments, false);
        DataCategory category = dataService.getDataCategoryByUid(pathItem.getUid());
        if (category.getItemDefinition() != null) {
            Profile profile = profileService.getProfile(environment, profileUid);
            return profileService.getProfileItems(profile, category, StartEndDate.getStartOfMonthDate(), null);
        } else {
            return new ArrayList<ProfileItem>(0);
        }
    }

	/**
	 * NOTE: By default, this function returns true, which means that cross
	 * domain requests are rejected. This check is disabled here so examples can
	 * be used directly from the address bar of the browser. Bear in mind that
	 * this exposes your data source to xsrf attacks. If the only use of the
	 * data source url is from your application, that runs on the same domain,
	 * it is better to remain in restricted mode.
	 */
	protected boolean isRestrictedAccessMode() {
		return false;
	}

}