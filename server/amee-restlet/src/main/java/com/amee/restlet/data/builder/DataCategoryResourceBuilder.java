package com.amee.restlet.data.builder;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.LocaleConstants;
import com.amee.domain.Pager;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.builder.DataItemBuilder;
import com.amee.domain.sheet.Column;
import com.amee.domain.sheet.Sheet;
import com.amee.domain.sheet.SortOrder;
import com.amee.restlet.data.DataCategoryResource;
import com.amee.service.data.DataService;
import com.amee.service.data.DataSheetService;
import com.amee.service.definition.DefinitionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
@Service
public class DataCategoryResourceBuilder {

    @Autowired
    private DataService dataService;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private DefinitionService definitionService;

    public JSONObject getJSONObject(DataCategoryResource resource) throws JSONException {

        // create JSON object
        JSONObject obj = new JSONObject();
        obj.put("path", resource.getDataCategory().getFullPath());

        if (resource.isGet()) {

            DataCategory dataCategory = resource.getDataCategory();

            // add DataCategory
            obj.put("dataCategory", dataCategory.getJSONObject(true));

            // list child Data Categories and child Data Items
            JSONObject children = new JSONObject();

            // add Data Categories to children
            JSONArray dataCategories = new JSONArray();
            for (DataCategory dc : dataService.getDataCategories(dataCategory)) {
                JSONObject dcObj = new JSONObject();
                dcObj.put("uid", dc.getUid());
                dcObj.put("name", dc.getName());
                dcObj.put("path", dc.getPath());
                dataCategories.put(dcObj);
            }
            children.put("dataCategories", dataCategories);

            // addItemValue Sheet containing Data Items
            Sheet sheet = dataSheetService.getSheet(resource.getDataBrowser(), dataCategory.getFullPath());
            if (sheet != null) {
                String sortBy = resource.getRequest().getResourceRef().getQueryAsForm().getFirstValue("sortBy");
                if (sortBy != null) {
                    Column c = sheet.getColumn(sortBy);
                    if (c != null) {
                        try {
                            c.setSortOrder(SortOrder.valueOf(resource.getRequest().getResourceRef().getQueryAsForm().getFirstValue("sortOrder", "")));
                        } catch (IllegalArgumentException e) {
                            // swallow
                        }
                        sheet = Sheet.getCopy(sheet);
                        sheet.getSortBy().getChoices().clear();
                        sheet.addSortBy(sortBy);
                        sheet.sortRows();
                    }
                }
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                children.put("dataItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
            } else {
                children.put("dataItems", new JSONObject());
                children.put("pager", new JSONObject());
            }

            // add children
            obj.put("children", children);

        } else if (resource.isPostOrPut()) {

            // DataCategories
            if (resource.getModDataCategory() != null) {
                obj.put("dataCategory", resource.getModDataCategory().getJSONObject(true));
            } else if (resource.getNewDataCategories() != null) {
                JSONArray dataCategories = new JSONArray();
                obj.put("dataCategories", dataCategories);
                for (DataCategory dc : resource.getNewDataCategories()) {
                    dataCategories.put(dc.getJSONObject(false));
                }
            }

            // DataItems
            if (resource.getModDataItem() != null) {
                obj.put("dataItem", new DataItemBuilder(resource.getModDataItem()).getJSONObject(true));
            } else if (resource.getNewDataItems() != null) {
                JSONArray dataItems = new JSONArray();
                obj.put("dataItems", dataItems);
                for (DataItem di : resource.getNewDataItems()) {
                    dataItems.put(new DataItemBuilder(di).getJSONObject(false));
                }
            }
        }

        return obj;
    }

    public Element getElement(DataCategoryResource resource, Document document) {

        Element element = document.createElement("DataCategoryResource");
        element.appendChild(XMLUtils.getElement(document, "Path", resource.getDataCategory().getFullPath()));

        if (resource.isGet()) {

            DataCategory dataCategory = resource.getDataCategory();

            // add DataCategory
            element.appendChild(dataCategory.getElement(document, true));

            // list child Data Categories and child Data Items
            Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Data Categories
            Element dataCategoriesElement = document.createElement("DataCategories");
            for (DataCategory dc : dataService.getDataCategories(dataCategory)) {
                Element dcElement = document.createElement("DataCategory");
                dcElement.setAttribute("uid", dc.getUid());
                dcElement.appendChild(XMLUtils.getElement(document, "Name", dc.getName()));
                dcElement.appendChild(XMLUtils.getElement(document, "Path", dc.getPath()));
                dataCategoriesElement.appendChild(dcElement);
            }
            childrenElement.appendChild(dataCategoriesElement);

            // list child Data Items via sheet
            Sheet sheet = dataSheetService.getSheet(resource.getDataBrowser(), dataCategory.getFullPath());
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
            }

        } else if (resource.isPostOrPut()) {

            // DataCategories
            if (resource.getModDataCategory() != null) {
                element.appendChild(resource.getModDataCategory().getElement(document, false));
            } else if (resource.getNewDataCategories() != null) {
                Element dataItemsElement = document.createElement("DataCategories");
                element.appendChild(dataItemsElement);
                for (DataCategory dc : resource.getNewDataCategories()) {
                    dataItemsElement.appendChild(dc.getElement(document, false));
                }
            }

            // DataItems
            if (resource.getModDataItem() != null) {
                element.appendChild(new DataItemBuilder(resource.getModDataItem()).getElement(document, false));
            } else if (resource.getNewDataItems() != null) {
                Element dataItemsElement = document.createElement("DataItems");
                element.appendChild(dataItemsElement);
                for (DataItem di : resource.getNewDataItems()) {
                    dataItemsElement.appendChild(new DataItemBuilder(di).getElement(document, false));
                }
            }
        }

        return element;
    }

    public Map<String, Object> getTemplateValues(DataCategoryResource resource) {
        DataCategory dataCategory = resource.getDataCategory();
        Sheet sheet = dataSheetService.getSheet(resource.getDataBrowser(), dataCategory.getFullPath());
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getDataBrowser());
        values.put("dataCategory", dataCategory);
        values.put("dataCategories", dataService.getDataCategories(dataCategory));
        values.put("itemDefinition", dataCategory.getItemDefinition());
        values.put("user", resource.getActiveUser());
        values.put("itemDefinitions", definitionService.getItemDefinitions());
        values.put("node", dataCategory);
        values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
        if (sheet != null) {
            Pager pager = resource.getPager();
            sheet = Sheet.getCopy(sheet, pager);
            pager.setCurrentPage(resource.getPage());
            values.put("sheet", sheet);
            values.put("pager", pager);
        }
        values.put("fullPath", "/data" + dataCategory.getFullPath());
        return values;
    }

    public org.apache.abdera.model.Element getAtomElement() {
        return null;
    }
}
