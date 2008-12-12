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
package gc.carbon.profile.builder.v2;

import com.jellymold.sheet.Cell;
import com.jellymold.sheet.Column;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.ValueType;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.domain.data.builder.BuildableItemDefinition;
import gc.carbon.domain.data.builder.BuildableItemValue;
import gc.carbon.domain.data.builder.BuildableItemValueDefinition;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.profile.builder.BuildableProfileItem;
import gc.carbon.profile.*;
import gc.carbon.profile.ProRataProfileService;
import gc.carbon.profile.SelectByProfileService;
import gc.carbon.profile.OnlyActiveProfileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class ProfileSheetBuilder implements CacheableFactory {

    private final Log log = LogFactory.getLog(getClass());

    private ProfileService profileService;

    ProfileSheetBuilder(ProfileService profileService) {
        super();
        this.profileService = profileService;
    }

    public Object create() {

        List<Column> columns;
        Row row;
        Map<String, ? extends BuildableItemValue> itemValuesMap;
        BuildableItemValue itemValue;
        BuildableItemDefinition itemDefinition;
        Sheet sheet = null;
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");

        // must have ItemDefinition
        itemDefinition = profileBrowser.getDataCategory().getItemDefinition();
        if (itemDefinition != null) {

            List<? extends BuildableProfileItem> profileItems;
            ProfileService decoratedProfileServiceDAO = new OnlyActiveProfileService(profileService);

            if (profileBrowser.isProRataRequest()) {
                decoratedProfileServiceDAO = new ProRataProfileService(profileService);
            }

            if (profileBrowser.isSelectByRequest()) {
                decoratedProfileServiceDAO = new SelectByProfileService(decoratedProfileServiceDAO, profileBrowser.getSelectBy());
            }

            profileItems = decoratedProfileServiceDAO.getProfileItems(profileBrowser);

            // create sheet and columns
            sheet = new Sheet();
            sheet.setKey(getKey());
            sheet.setLabel("ProfileItems");
            for (BuildableItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                if (itemValueDefinition.isFromProfile()) {
                    new Column(sheet, itemValueDefinition.getPath(), itemValueDefinition.getName());
                }
            }

            new Column(sheet, "name");
            new Column(sheet, "amount");
            new Column(sheet, "startDate");
            new Column(sheet, "endDate");
            new Column(sheet, "path");
            new Column(sheet, "uid", true);
            new Column(sheet, "created", true);
            new Column(sheet, "modified", true);
            new Column(sheet, "dataItemLabel");
            new Column(sheet, "dataItemUid");

            // create rows and cells
            columns = sheet.getColumns();
            for (BuildableProfileItem profileItem : profileItems) {
                itemValuesMap = profileItem.getItemValuesMap();
                row = new Row(sheet, profileItem.getUid());
                row.setLabel("ProfileItem");
                for (Column column : columns) {
                    itemValue = itemValuesMap.get(column.getName());
                    if (itemValue != null) {
                        new Cell(column, row, itemValue.getValue(), itemValue.getUid(), itemValue.getItemValueDefinition().getValueDefinition().getValueType());
                    } else if ("name".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getName(), ValueType.TEXT);
                    } else if ("amount".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getAmount(), ValueType.DECIMAL);
                    } else if ("startDate".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, new StartEndDate(profileItem.getStartDate()).toString(), ValueType.TEXT);
                    } else if ("endDate".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, (profileItem.getEndDate() != null) ? profileItem.getEndDate().toString() : "", ValueType.TEXT);
                    } else if ("end".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.isEnd(), ValueType.BOOLEAN);
                    } else if ("path".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getDisplayPath(), ValueType.TEXT);
                    } else if ("uid".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getUid(), ValueType.TEXT);
                    } else if ("created".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, new StartEndDate(profileItem.getCreated()).toString(), ValueType.TEXT);
                    } else if ("modified".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, new StartEndDate(profileItem.getModified()).toString(), ValueType.TEXT);
                    } else if ("dataItemUid".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getDataItem().getUid(), profileItem.getDataItem().getUid(), ValueType.TEXT);
                    } else if ("dataItemLabel".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getDataItem().getLabel(), ValueType.TEXT);
                    } else {
                        // add empty cell
                        new Cell(column, row);
                    }
                }
            }


            // sort columns and rows in sheet
            sheet.addDisplayBy("dataItemLabel");
            sheet.addDisplayBy("amount");
            sheet.sortColumns();
            sheet.addSortBy("dataItemLabel");
            sheet.addSortBy("amount");
            sheet.sortRows();
        }

        return sheet;
    }

    public String getKey() {
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");

        return "ProfileSheet_" + profileBrowser.getProfile().getUid() + "_" + profileBrowser.getDataCategory().getUid() + "_" +
                profileBrowser.getStartDate().getTime() +
                ((profileBrowser.getEndDate() != null) ? "_" + profileBrowser.getEndDate().getTime() : "");
    }

    public String getCacheName() {
        return "ProfileSheets";
    }
}