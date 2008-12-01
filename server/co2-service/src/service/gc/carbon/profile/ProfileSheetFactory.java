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
package gc.carbon.profile;

import com.jellymold.sheet.Cell;
import com.jellymold.sheet.Column;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.ValueType;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.StartEndDate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Service
public class ProfileSheetFactory implements CacheableFactory {

    private final Log log = LogFactory.getLog(getClass());

    private static final String DAY_DATE = "yyyyMMdd";
    private static DateFormat DAY_DATE_FMT = new SimpleDateFormat(DAY_DATE);

    @Autowired
    private ProfileService profileService;

    public ProfileSheetFactory() {
        super();
    }

    public Object create() {

        List<Column> columns;
        Row row;
        Map<String, ItemValue> itemValuesMap;
        ItemValue itemValue;
        ItemDefinition itemDefinition;
        Sheet sheet = null;
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");

        // must have ItemDefinition
        itemDefinition = profileBrowser.getDataCategory().getItemDefinition();
        if (itemDefinition != null) {

            List<ProfileItem> profileItems;
            // TODO: Rework once we are on Spring
            boolean isV2 = profileBrowser.getAPIVersion().isVersionTwo();
            if (isV2) {

                ProfileService decoratedProfileService = new OnlyActiveProfileService(profileService);

                if (profileBrowser.isProRataRequest()) {
                    decoratedProfileService = new ProRataProfileService(profileService);
                }

                if (profileBrowser.isSelectByRequest()) {
                    decoratedProfileService = new SelectByProfileService(decoratedProfileService, profileBrowser.getSelectBy());
                }

                profileItems = decoratedProfileService.getProfileItems(profileBrowser);


            } else {
                profileItems = profileService.getProfileItems(profileBrowser.getProfile(), profileBrowser.getDataCategory(), profileBrowser.getProfileDate());
            }

            // create sheet and columns
            sheet = new Sheet();
            sheet.setKey(getKey());
            sheet.setLabel("ProfileItems");
            for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                if (itemValueDefinition.isFromProfile()) {
                    new Column(sheet, itemValueDefinition.getPath(), itemValueDefinition.getName());
                }
            }

            new Column(sheet, "name");
            new Column(sheet, isV2 ? "amount" : "amountPerMonth");
            new Column(sheet, isV2 ? "startDate" : "validFrom");
            if (isV2)
                new Column(sheet, "endDate");
            new Column(sheet, "end");
            new Column(sheet, "path");
            new Column(sheet, "uid", true);
            new Column(sheet, "created", true);
            new Column(sheet, "modified", true);
            new Column(sheet, "dataItemLabel");
            new Column(sheet, "dataItemUid");

            // create rows and cells
            columns = sheet.getColumns();
            for (ProfileItem profileItem : profileItems) {
                itemValuesMap = profileItem.getItemValuesMap();
                row = new Row(sheet, profileItem.getUid());
                row.setLabel("ProfileItem");
                for (Column column : columns) {
                    itemValue = itemValuesMap.get(column.getName());
                    if (itemValue != null) {
                        new Cell(column, row, itemValue.getValue(), itemValue.getUid(), itemValue.getItemValueDefinition().getValueDefinition().getValueType());
                    } else if ("name".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getName(), ValueType.TEXT);
                    } else if ((isV2 ? "amount" : "amountPerMonth").equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getAmount(), ValueType.DECIMAL);
                    } else if ((isV2 ? "startDate" : "validFrom").equalsIgnoreCase(column.getName())) {
                        if (isV2)
                            new Cell(column, row, new StartEndDate(profileItem.getStartDate()).toString(), ValueType.TEXT);
                        else
                            new Cell(column, row, DAY_DATE_FMT.format(profileItem.getStartDate()), ValueType.TEXT);
                    } else if ("endDate".equalsIgnoreCase(column.getName())) {
                        if (isV2)
                            new Cell(column, row, (profileItem.getEndDate() != null) ? new StartEndDate(profileItem.getEndDate()).toString() : "", ValueType.TEXT);
                        else
                            new Cell(column, row, DAY_DATE_FMT.format(profileItem.getEndDate()), ValueType.TEXT);
                    } else if ("end".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.isEnd(), ValueType.BOOLEAN);
                    } else if ("path".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getDisplayPath(), ValueType.TEXT);
                    } else if ("uid".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getUid(), ValueType.TEXT);
                    } else if ("created".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getCreated(), ValueType.DATE);
                    } else if ("modified".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getModified(), ValueType.DATE);
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
            sheet.addDisplayBy(isV2 ? "amount" : "amountPerMonth");
            sheet.sortColumns();
            sheet.addSortBy("dataItemLabel");
            sheet.addSortBy(isV2 ? "amount" : "amountPerMonth");
            sheet.sortRows();
        }

        return sheet;
    }

    public String getKey() {
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");
        return "ProfileSheet_" + profileBrowser.getProfile().getUid() + "_" + profileBrowser.getDataCategory().getUid() + "_" +
                ((profileBrowser.getProfileDate() != null) ? profileBrowser.getProfileDate().getTime() : profileBrowser.getStartDate().getTime());

/*
        StringBuffer key = new StringBuffer("ProfileSheet_");
        key.append(profileBrowser.getProfile().getUid()).append("_");
        key.append(profileBrowser.getDataCategory().getUid()).append("_");
        if (profileBrowser.getProfileDate() != null) {
            key.append(profileBrowser.getProfileDate().getTime());
        } else {
            key.append(profileBrowser.getStartDate().getTime());
            if (profileBrowser.getEndDate() != null) {
                key.append("_" ).append(profileBrowser.getEndDate().getTime());
            }
        }
        return key.toString();
*/
    }

    public String getCacheName() {
        return "ProfileSheets";
    }
}
