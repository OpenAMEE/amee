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

import com.jellymold.sheet.Sheet;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.definition.DefinitionService;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemService;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Map;

@Name("dataCategoryResource")
@Scope(ScopeType.EVENT)
public class DataCategoryResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(DataCategoryResource.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private DefinitionService definitionService;

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private DataBrowser dataBrowser;

    @In(create = true)
    private DataSheetService dataSheetService;

    @In(create = true)
    private PathItemService pathItemService;

    @In
    private PathItem pathItem;

    private DataCategory newDataCategory;
    private DataItem newDataItem;

    public DataCategoryResource() {
        super();
    }

    public DataCategoryResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        dataBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (dataBrowser.getDataCategory() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_DATA_CATEGORY;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        DataCategory dataCategory = dataBrowser.getDataCategory();
        Sheet sheet = dataSheetService.getSheet(dataCategory);
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataCategory", dataCategory);
        values.put("itemDefinition", dataCategory.getItemDefinition());
        values.put("node", dataCategory);
        if (sheet != null) {
            Pager pager = getPager(dataBrowser.getItemsPerPage(getRequest()));
            sheet = Sheet.getCopy(sheet, pager);
            pager.setCurrentPage(getPage());
            values.put("sheet", sheet);
            values.put("pager", pager);
        }
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {

        DataCategory dataCategory = dataBrowser.getDataCategory();

        // create JSON object
        JSONObject obj = new JSONObject();
        obj.put("path", pathItem.getFullPath());

        if (isGet()) {

            // add DataCategory
            obj.put("dataCategory", dataCategory.getJSONObject());

            // list child Data Categories and child Data Items
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (PathItem pi : pathItem.getChildrenByType("DC")) {
                dataCategories.put(pi.getJSONObject());
            }
            children.put("dataCategories", dataCategories);

            // add Sheet containing Data Items
            Sheet sheet = dataSheetService.getSheet(dataCategory);
            if (sheet != null) {
                Pager pager = getPager(dataBrowser.getItemsPerPage(getRequest()));
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(getPage());
                children.put("dataItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
            } else {
                children.put("dataItems", new JSONObject());
                children.put("pager", new JSONObject());
            }

            // add children
            obj.put("children", children);

        } else if (isPost()) {

            if (newDataCategory != null) {
                obj.put("dataCategory", newDataCategory.getJSONObject());
            }

            if (newDataItem != null) {
                obj.put("dataItem", newDataItem.getJSONObject());
            }
        }

        return obj;
    }

    @Override
    public Element getElement(Document document) {
        DataCategory dataCategory = dataBrowser.getDataCategory();

        // create Element
        Element element = document.createElement("DataCategoryResource");
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));

        if (isGet()) {

            // add DataCategory
            element.appendChild(dataCategory.getElement(document));

            // list child Data Categories and child Data Items
            Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Data Categories
            Element dataCategoriesElement = document.createElement("DataCategories");
            for (PathItem pi : pathItem.getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(pi.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);

            // list child Data Items via sheet
            Sheet sheet = dataSheetService.getSheet(dataCategory);
            if (sheet != null) {
                Pager pager = getPager(dataBrowser.getItemsPerPage(getRequest()));
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(getPage());
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
            }

        } else if (isPost()) {

            if (newDataCategory != null) {
                element.appendChild(newDataCategory.getElement(document));
            }

            if (newDataItem != null) {
                element.appendChild(newDataItem.getElement(document));
            }
        }

        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (dataBrowser.getDataCategoryActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void post(Representation entity) {
        log.debug("post");
        DataCategory dataCategory = dataBrowser.getDataCategory();
        Form form = getForm();
        String type = form.getFirstValue("newObjectType");
        if (type != null) {
            if (type.equalsIgnoreCase("DC")) {
                if (dataBrowser.getDataCategoryActions().isAllowCreate()) {
                    newDataCategory = new DataCategory(dataCategory);
                    newDataCategory.setName(form.getFirstValue("name"));
                    newDataCategory.setPath(form.getFirstValue("path"));
                    if (form.getNames().contains("itemDefinitionUid")) {
                        ItemDefinition itemDefinition =
                                definitionService.getItemDefinition(dataBrowser.getDataCategory().getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                        if (itemDefinition != null) {
                            newDataCategory.setItemDefinition(itemDefinition);
                        }
                    }
                    entityManager.persist(newDataCategory);
                    pathItemService.removePathItemGroup(dataCategory.getEnvironment());
                    dataSheetService.removeSheet(dataCategory);
                    if (isStandardWebBrowser()) {
                        success(dataBrowser.getFullPath());
                    } else {
                        // return a response for API calls
                        super.handleGet();
                    }
                } else {
                    notAuthorized();
                }
            } else if (type.equalsIgnoreCase("DI")) {
                if (dataBrowser.getDataItemActions().isAllowCreate()) {
                    ItemDefinition itemDefinition = dataCategory.getItemDefinition();
                    if (itemDefinition != null) {
                        newDataItem = new DataItem(dataCategory, itemDefinition);
                        newDataItem.setName(form.getFirstValue("name"));
                        newDataItem.setPath(form.getFirstValue("path"));
                        entityManager.persist(newDataItem);
                        dataService.checkDataItem(newDataItem);
                        // update item values if supplied
                        Map<String, ItemValue> itemValues = newDataItem.getItemValuesMap();
                        for (String name : form.getNames()) {
                            ItemValue itemValue = itemValues.get(name);
                            if (itemValue != null) {
                                itemValue.setValue(form.getFirstValue(name));
                            }
                        }
                        // clear caches
                        pathItemService.removePathItemGroup(dataCategory.getEnvironment());
                        dataSheetService.removeSheet(dataCategory);
                        if (isStandardWebBrowser()) {
                            success(dataBrowser.getFullPath());
                        } else {
                            // return a response for API calls
                            super.handleGet();
                        }
                    } else {
                        badRequest();
                    }
                } else {
                    notAuthorized();
                }
            } else {
                badRequest();
            }
        } else {
            badRequest();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (dataBrowser.getDataCategoryActions().isAllowModify()) {
            Form form = getForm();
            DataCategory dataCategory = dataBrowser.getDataCategory();
            if (form.getNames().contains("name")) {
                dataCategory.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("path")) {
                dataCategory.setPath(form.getFirstValue("path"));
            }
            if (form.getNames().contains("itemDefinitionUid")) {
                ItemDefinition itemDefinition =
                        definitionService.getItemDefinition(dataBrowser.getDataCategory().getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                if (itemDefinition != null) {
                    dataCategory.setItemDefinition(itemDefinition);
                } else {
                    dataCategory.setItemDefinition(null);
                }
            }
            pathItemService.removePathItemGroup(dataCategory.getEnvironment());
            dataSheetService.removeSheet(dataCategory);
            success(dataBrowser.getFullPath());
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void delete() {
        log.debug("delete");
        if (dataBrowser.getDataCategoryActions().isAllowDelete()) {
            DataCategory dataCategory = dataBrowser.getDataCategory();
            pathItemService.removePathItemGroup(dataCategory.getEnvironment());
            dataSheetService.removeSheet(dataCategory);
            dataService.remove(dataCategory);
            success(pathItem.getParent().getFullPath());
        } else {
            notAuthorized();
        }
    }
}