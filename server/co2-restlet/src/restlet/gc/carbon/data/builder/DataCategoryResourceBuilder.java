package gc.carbon.data.builder;

import gc.carbon.ResourceBuilder;
import gc.carbon.data.DataCategoryResource;
import gc.carbon.data.DataService;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.path.PathItem;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.restlet.data.Method;

import java.util.Map;
import java.util.HashMap;

import com.jellymold.sheet.Sheet;
import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIUtils;

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
public class DataCategoryResourceBuilder implements ResourceBuilder {

    private DataCategoryResource resource;
    private DataService dataService;

    public DataCategoryResourceBuilder(DataCategoryResource resource) {
        this.resource = resource;
        this.dataService = resource.getDataService();
    }

    public JSONObject getJSONObject() throws JSONException {

        // create JSON object
        JSONObject obj = new JSONObject();
        obj.put("path", resource.getPathItem().getFullPath());

        if (resource.isGet()) {

            // add DataCategory
            obj.put("dataCategory", resource.getDataBrowser().getDataCategory().getJSONObject(true));
            obj.put("actions", resource.getActions(resource.getDataBrowser().getDataCategoryActions()));
            obj.put("dataItemActions", resource.getActions(resource.getDataBrowser().getDataItemActions()));

            // add ItemDefinition list
            JSONArray itemDefinitions = new JSONArray();
            for (ItemDefinition iDefinition : resource.getDataBrowser().getItemDefinitions()) {
                itemDefinitions.put(iDefinition.getJSONObject(false));
            }
            obj.put("itemDefinitions", itemDefinitions);

            // list child Data Categories and child Data Items
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (PathItem pi : resource.getPathItem().getChildrenByType("DC")) {
                dataCategories.put(pi.getJSONObject());
            }
            children.put("dataCategories", dataCategories);

            // add Sheet containing Data Items
            Sheet sheet = dataService.getSheet(resource.getDataBrowser());
            if (sheet != null) {
                Pager pager = resource.getPager(resource.getItemsPerPage());
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
            if (resource.getDataCategory() != null) {
                obj.put("dataCategory", resource.getDataCategory().getJSONObject(true));
            } else if (resource.getDataCategories() != null) {
                JSONArray dataCategories = new JSONArray();
                obj.put("dataCategories", dataCategories);
                for (DataCategory dc : resource.getDataCategories()) {
                    dataCategories.put(dc.getJSONObject(false));
                }
            }

            // DataItems
            if (resource.getDataItem() != null) {
                obj.put("dataItem", resource.getDataItem().getJSONObject(true));
            } else if (resource.getDataItems() != null) {
                JSONArray dataItems = new JSONArray();
                obj.put("dataItems", dataItems);
                for (DataItem di : resource.getDataItems()) {
                    dataItems.put(di.getJSONObject(false));
                }
            }
        }

        return obj;
    }

    public Element getElement(Document document) {

        Element element = document.createElement("DataCategoryResource");
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));

        if (resource.isGet()) {

            // add DataCategory
            element.appendChild(resource.getDataBrowser().getDataCategory().getElement(document, true));

            // list child Data Categories and child Data Items
            Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Data Categories
            Element dataCategoriesElement = document.createElement("DataCategories");
            for (PathItem pi : resource.getPathItem().getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(pi.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);

            // list child Data Items via sheet
            Sheet sheet = dataService.getSheet(resource.getDataBrowser());
            if (sheet != null) {
                Pager pager = resource.getPager(resource.getItemsPerPage());
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
            }

        } else if (resource.isPostOrPut()) {

            // DataCategories
            if (resource.getDataCategory() != null) {
                element.appendChild(resource.getDataCategory().getElement(document, false));
            } else if (resource.getDataCategories() != null) {
                Element dataItemsElement = document.createElement("DataCategories");
                element.appendChild(dataItemsElement);
                for (DataCategory dc : resource.getDataCategories()) {
                    dataItemsElement.appendChild(dc.getElement(document, false));
                }
            }

            // DataItems
            if (resource.getDataItem() != null) {
                element.appendChild(resource.getDataItem().getElement(document, false));
            } else if (resource.getDataItems() != null) {
                Element dataItemsElement = document.createElement("DataItems");
                element.appendChild(dataItemsElement);
                for (DataItem di : resource.getDataItems()) {
                    dataItemsElement.appendChild(di.getElement(document, false));
                }
            }
        }

        return element;
    }

    public Map<String, Object> getTemplateValues() {
        DataCategory dataCategory = resource.getDataBrowser().getDataCategory();
        Sheet sheet = dataService.getSheet(resource.getDataBrowser());
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getDataBrowser());
        values.put("dataCategory", dataCategory);
        values.put("itemDefinition", dataCategory.getItemDefinition());
        values.put("node", dataCategory);
        if (sheet != null) {
            Pager pager = resource.getPager(resource.getItemsPerPage());
            sheet = Sheet.getCopy(sheet, pager);
            pager.setCurrentPage(resource.getPage());
            values.put("sheet", sheet);
            values.put("pager", pager);
        }
        return values;
    }

    public org.apache.abdera.model.Element getAtomElement() {
        return null;
    }

}
