package com.amee.restlet.profile.builder.v1;

import com.amee.core.ValueType;
import com.amee.domain.core.DecimalPerUnit;
import com.amee.domain.APIVersion;
import com.amee.domain.cache.CacheableFactory;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.sheet.Cell;
import com.amee.domain.sheet.Column;
import com.amee.domain.sheet.Row;
import com.amee.domain.sheet.Sheet;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.profile.ProfileService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ProfileSheetBuilder implements CacheableFactory {

    private static final String DAY_DATE = "yyyyMMdd";
    private static DateFormat DAY_DATE_FMT = new SimpleDateFormat(DAY_DATE);

    private ProfileCategoryResource resource;
    private ProfileService profileService;

    public ProfileSheetBuilder(ProfileCategoryResource resource, ProfileService profileService) {
        super();
        this.resource = resource;
        this.profileService = profileService;
    }

    public Object create() {

        List<Column> columns;
        Row row;
        Map<String, ItemValue> itemValuesMap;
        ItemValue itemValue;
        ItemDefinition itemDefinition;
        Sheet sheet = null;

        DataCategory dataCategory = resource.getDataCategory();

        if (ThreadBeanHolder.get("dataCategoryForFactory") != null) {
            dataCategory = (DataCategory) ThreadBeanHolder.get("dataCategoryForFactory");
        }

        itemDefinition = dataCategory.getItemDefinition();
        if (itemDefinition != null) {

            List<ProfileItem> profileItems = profileService.getProfileItems(resource.getProfile(),
                    dataCategory, resource.getProfileBrowser().getProfileDate());

            // create sheet and columns
            sheet = new Sheet();
            sheet.setKey(getKey());
            sheet.setLabel("ProfileItems");
            for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                if (itemValueDefinition.isFromProfile() && itemValueDefinition.isValidInAPIVersion(APIVersion.ONE)) {
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
                        if (!profileItem.isSingleFlight()) {
                            new Cell(column, row, profileItem.getAmount().convert(DecimalPerUnit.MONTH), ValueType.DECIMAL);
                        } else {
                            new Cell(column, row, profileItem.getAmount().getValue(), ValueType.DECIMAL);
                        }
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
                        // addItemValue empty cell
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

        DataCategory dataCategory = resource.getDataCategory();
        if (ThreadBeanHolder.get("dataCategoryForFactory") != null) {
            dataCategory = (DataCategory) ThreadBeanHolder.get("dataCategoryForFactory");
        }
        return "ProfileSheet_" + resource.getProfile().getUid() + "_" + dataCategory.getUid() + "_" +
                resource.getProfileBrowser().getProfileDate().getTime();
    }

    public String getCacheName() {
        return "ProfileSheets";
    }
}