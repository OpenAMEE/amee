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
import org.dom4j.DocumentException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.XML;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private List<DataCategory> newDataCategories;
    private DataItem newDataItem;
    private List<DataItem> newDataItems;

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

            // Data Categories
            if (newDataCategory != null) {
                obj.put("dataCategory", newDataCategory.getJSONObject());
            } else if (newDataCategories != null) {
                JSONArray dataCategories = new JSONArray();
                obj.put("dataCategories", dataCategories);
                for (DataCategory dc : newDataCategories) {
                    dataCategories.put(dc.getJSONObject(false));
                }
            }

            // Data Items
            if (newDataItem != null) {
                obj.put("dataItem", newDataItem.getJSONObject());
            } else if (newDataItems != null) {
                JSONArray dataItems = new JSONArray();
                obj.put("dataItems", dataItems);
                for (DataItem di : newDataItems) {
                    dataItems.put(di.getJSONObject(false));
                }
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

            // Data Categories
            if (newDataCategory != null) {
                element.appendChild(newDataCategory.getElement(document, false));
            } else if (newDataCategories != null) {
                Element dataItemsElement = document.createElement("DataCategories");
                element.appendChild(dataItemsElement);
                for (DataCategory dc : newDataCategories) {
                    dataItemsElement.appendChild(dc.getElement(document, false));
                }
            }

            // Data Items
            if (newDataItem != null) {
                element.appendChild(newDataItem.getElement(document, false));
            } else if (newDataItems != null) {
                Element dataItemsElement = document.createElement("DataItems");
                element.appendChild(dataItemsElement);
                for (DataItem di : newDataItems) {
                    dataItemsElement.appendChild(di.getElement(document, false));
                }
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
        newDataItems = new ArrayList<DataItem>();
        newDataCategories = new ArrayList<DataCategory>();
        // TODO: may be a more elegant way to handle incoming representations of different media types
        MediaType mediaType = entity.getMediaType();
        if (MediaType.APPLICATION_XML.includes(mediaType)) {
            postXML(entity);
        } else if (MediaType.APPLICATION_JSON.includes(mediaType)) {
            postJSON(entity);
        } else {
            postForm(getForm());
        }
        if ((newDataCategory != null) || (newDataItem != null) || !newDataCategories.isEmpty() || !newDataItems.isEmpty()) {
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
    }

    protected void postJSON(Representation entity) {
        log.debug("postJSON");
        DataCategory dataCategory;
        DataItem dataItem;
        Form form;
        String key;
        JSONObject rootJSON;
        JSONArray dataCategoriesJSON;
        JSONArray dataItemsJSON;
        JSONObject itemJSON;
        try {
            rootJSON = new JSONObject(entity.getText());
            if (rootJSON.has("dataCategories")) {
                dataCategoriesJSON = rootJSON.getJSONArray("dataCategories");
                for (int i = 0; i < dataCategoriesJSON.length(); i++) {
                    itemJSON = dataCategoriesJSON.getJSONObject(i);
                    form = new Form();
                    for (Iterator iterator = itemJSON.keys(); iterator.hasNext();) {
                        key = (String) iterator.next();
                        form.add(key, itemJSON.getString(key));
                    }
                    dataCategory = postFormForNewDataCategory(form);
                    if (dataCategory != null) {
                        newDataCategories.add(dataCategory);
                    } else {
                        log.warn("Data Category not added");
                    }
                }
            }
            if (rootJSON.has("dataItems")) {
                dataItemsJSON = rootJSON.getJSONArray("dataItems");
                for (int i = 0; i < dataItemsJSON.length(); i++) {
                    itemJSON = dataItemsJSON.getJSONObject(i);
                    form = new Form();
                    for (Iterator iterator = itemJSON.keys(); iterator.hasNext();) {
                        key = (String) iterator.next();
                        form.add(key, itemJSON.getString(key));
                    }
                    dataItem = postFormForNewDataItem(form);
                    if (dataItem != null) {
                        newDataItems.add(dataItem);
                    } else {
                        log.warn("Data Item not added");
                    }
                }
            }
        } catch (JSONException e) {
            log.warn("Caught JSONException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected void postXML(Representation entity) {
        log.debug("postXML");
        DataCategory dataCategory;
        DataItem dataItem;
        Form form;
        org.dom4j.Element rootElem;
        org.dom4j.Element dataCategoriesElem;
        org.dom4j.Element dataItemsElem;
        org.dom4j.Element itemElem;
        org.dom4j.Element valueElem;
        try {
            rootElem = XML.getRootElement(entity.getStream());
            if (rootElem.getName().equalsIgnoreCase("DataCategory")) {
                // handle Data Categories
                dataCategoriesElem = rootElem.element("DataCategories");
                if (dataCategoriesElem != null) {
                    for (Object o1 : dataCategoriesElem.elements("DataCategory")) {
                        itemElem = (org.dom4j.Element) o1;
                        form = new Form();
                        for (Object o2 : itemElem.elements()) {
                            valueElem = (org.dom4j.Element) o2;
                            form.add(valueElem.getName(), valueElem.getText());
                        }
                        dataCategory = postFormForNewDataCategory(form);
                        if (dataCategory != null) {
                            newDataCategories.add(dataCategory);
                        } else {
                            log.warn("Data Category not added");
                        }
                    }
                }
                // handle Data Items
                dataItemsElem = rootElem.element("DataItems");
                if (dataItemsElem != null) {
                    for (Object o1 : dataItemsElem.elements("DataItem")) {
                        itemElem = (org.dom4j.Element) o1;
                        form = new Form();
                        for (Object o2 : itemElem.elements()) {
                            valueElem = (org.dom4j.Element) o2;
                            form.add(valueElem.getName(), valueElem.getText());
                        }
                        dataItem = postFormForNewDataItem(form);
                        if (dataItem != null) {
                            newDataItems.add(dataItem);
                        } else {
                            log.warn("Data Item not added");
                        }
                    }
                }
            } else {
                log.warn("DataCategory not found");
            }
        } catch (DocumentException e) {
            log.warn("Caught DocumentException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Caught IOException: " + e.getMessage(), e);
        }
    }

    protected void postForm(Form form) {
        log.debug("postForm");
        String type = form.getFirstValue("newObjectType");
        if (type != null) {
            if (type.equalsIgnoreCase("DC")) {
                if (dataBrowser.getDataCategoryActions().isAllowCreate()) {
                    newDataCategory = postFormForNewDataCategory(form);
                } else {
                    notAuthorized();
                }
            } else if (type.equalsIgnoreCase("DI")) {
                if (dataBrowser.getDataItemActions().isAllowCreate()) {
                    newDataItem = postFormForNewDataItem(form);
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

    protected DataCategory postFormForNewDataCategory(Form form) {
        log.debug("postFormForNewDataCategory");
        DataCategory dataCategory;
        DataCategory parentDataCategory = dataBrowser.getDataCategory();
        dataCategory = new DataCategory(parentDataCategory);
        dataCategory.setName(form.getFirstValue("name"));
        dataCategory.setPath(form.getFirstValue("path"));
        if (form.getNames().contains("itemDefinitionUid")) {
            ItemDefinition itemDefinition =
                    definitionService.getItemDefinition(dataBrowser.getDataCategory().getEnvironment(), form.getFirstValue("itemDefinitionUid"));
            if (itemDefinition != null) {
                dataCategory.setItemDefinition(itemDefinition);
            }
        }
        entityManager.persist(dataCategory);
        return dataCategory;
    }

    protected DataItem postFormForNewDataItem(Form form) {
        log.debug("postFormForNewDataItem");
        DataItem dataItem = null;
        DataCategory dataCategory = dataBrowser.getDataCategory();
        ItemDefinition itemDefinition = dataCategory.getItemDefinition();
        if (itemDefinition != null) {
            dataItem = new DataItem(dataCategory, itemDefinition);
            dataItem.setName(form.getFirstValue("name"));
            dataItem.setPath(form.getFirstValue("path"));
            entityManager.persist(dataItem);
            dataService.checkDataItem(dataItem);
            // update item values if supplied
            Map<String, ItemValue> itemValues = dataItem.getItemValuesMap();
            for (String name : form.getNames()) {
                ItemValue itemValue = itemValues.get(name);
                if (itemValue != null) {
                    itemValue.setValue(form.getFirstValue(name));
                }
            }
        } else {
            badRequest();
        }
        return dataItem;
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