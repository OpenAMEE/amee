package gc.carbon.profile.builder.v1;

import com.jellymold.sheet.Cell;
import com.jellymold.sheet.Column;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.ValueType;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.APIVersion;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileBrowser;
import gc.carbon.profile.ProfileService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ProfileSheetBuilder implements CacheableFactory {

    private static final String DAY_DATE = "yyyyMMdd";
    private static DateFormat DAY_DATE_FMT = new SimpleDateFormat(DAY_DATE);

    private ProfileService profileService;

    ProfileSheetBuilder(ProfileService profileService) {
        super();
        this.profileService = profileService;
    }

    public Object create() {

        List<Column> columns;
        Row row;
        Map<String, ItemValue> itemValuesMap;
        ItemValue itemValue;
        ItemDefinition itemDefinition;
        Sheet sheet = null;
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");
        DataCategory dataCategory = (DataCategory) ThreadBeanHolder.get("dataCategoryForFactory");
        if (dataCategory == null) {
            dataCategory = profileBrowser.getDataCategory();
        }

        // must have ItemDefinition
        itemDefinition = dataCategory.getItemDefinition();
        if (itemDefinition != null) {

            List<ProfileItem> profileItems = profileService.getProfileItems(profileBrowser.getProfile(), dataCategory, profileBrowser.getProfileDate());

            // create sheet and columns
            sheet = new Sheet();
            sheet.setKey(getKey());
            sheet.setLabel("ProfileItems");
            for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                if (itemValueDefinition.isFromProfile() && itemValueDefinition.includedInAPIVersion(APIVersion.ONE)) {
                    new Column(sheet, itemValueDefinition.getPath(), itemValueDefinition.getName());
                }
            }

            new Column(sheet, "name");
            new Column(sheet, "amountPerMonth");
            new Column(sheet, "validFrom");
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
                    } else if ("amountPerMonth".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, profileItem.getAmount(), ValueType.DECIMAL);
                    } else if ("validFrom".equalsIgnoreCase(column.getName())) {
                        new Cell(column, row, DAY_DATE_FMT.format(profileItem.getStartDate()), ValueType.TEXT);
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
            sheet.addDisplayBy("amountPerMonth");
            sheet.sortColumns();
            sheet.addSortBy("dataItemLabel");
            sheet.addSortBy("amountPerMonth");
            sheet.sortRows();
        }

        return sheet;
    }

    public String getKey() {
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");
        DataCategory dataCategory = (DataCategory) ThreadBeanHolder.get("dataCategoryForFactory");
        if (dataCategory == null) {
            dataCategory = profileBrowser.getDataCategory();
        }
        return "ProfileSheet_" + profileBrowser.getProfile().getUid() + "_" + dataCategory.getUid() + "_" + profileBrowser.getProfileDate().getTime();
    }

    public String getCacheName() {
        return "ProfileSheets";
    }
}