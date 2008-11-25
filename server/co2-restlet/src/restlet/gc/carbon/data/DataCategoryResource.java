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
import com.jellymold.utils.Pager;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.BaseResource;
import gc.carbon.definition.DefinitionService;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TODO: may be a more elegant way to handle incoming representations of different media types
 * TODO: may be better to break this class down into components that handle post/put and json/xml individually
 */
@Component("dataCategoryResource")
@Scope("prototype")
public class DataCategoryResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private PathItemService pathItemService;

    private PathItem pathItem;
    private DataBrowser dataBrowser;
    private DataCategory dataCategory;
    private List<DataCategory> dataCategories;
    private DataItem dataItem;
    private List<DataItem> dataItems;

    public DataCategoryResource() {
        super();
    }

    public DataCategoryResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        Form form = request.getResourceRef().getQueryAsForm();
        dataBrowser = getDataBrowser();
        pathItem = getPathItem();
        dataBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        dataBrowser.setStartDate(form.getFirstValue("startDate"));
        dataBrowser.setEndDate(form.getFirstValue("endDate"));
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
        Sheet sheet = dataSheetService.getSheet(dataBrowser);
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataCategory", dataCategory);
        values.put("itemDefinition", dataCategory.getItemDefinition());
        values.put("node", dataCategory);
        if (sheet != null) {
            Pager pager = getPager(getItemsPerPage());
            sheet = Sheet.getCopy(sheet, pager);
            pager.setCurrentPage(getPage());
            values.put("sheet", sheet);
            values.put("pager", pager);
        }
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {

        // create JSON object
        JSONObject obj = new JSONObject();
        obj.put("path", pathItem.getFullPath());

        if (isGet()) {

            obj.put("startDate", dataBrowser.getStartDate());
            if (dataBrowser.getEndDate() != null) {
                obj.put("endDate", dataBrowser.getEndDate());
            } else {
                obj.put("endDate", "");
            }

            // add DataCategory
            obj.put("dataCategory", dataBrowser.getDataCategory().getJSONObject());

            // list child Data Categories and child Data Items
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (PathItem pi : pathItem.getChildrenByType("DC")) {
                dataCategories.put(pi.getJSONObject());
            }
            children.put("dataCategories", dataCategories);

            // add Sheet containing Data Items
            Sheet sheet = dataSheetService.getSheet(dataBrowser);
            if (sheet != null) {
                Pager pager = getPager(getItemsPerPage());
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

        } else if (getRequest().getMethod().equals(Method.POST) || getRequest().getMethod().equals(Method.PUT)) {

            // DataCategories
            if (dataCategory != null) {
                obj.put("dataCategory", dataCategory.getJSONObject());
            } else if (dataCategories != null) {
                JSONArray dataCategories = new JSONArray();
                obj.put("dataCategories", dataCategories);
                for (DataCategory dc : this.dataCategories) {
                    dataCategories.put(dc.getJSONObject(false));
                }
            }

            // DataItems
            if (dataItem != null) {
                obj.put("dataItem", dataItem.getJSONObject());
            } else if (dataItems != null) {
                JSONArray dataItems = new JSONArray();
                obj.put("dataItems", dataItems);
                for (DataItem di : this.dataItems) {
                    dataItems.put(di.getJSONObject(false));
                }
            }
        }

        return obj;
    }

    @Override
    public Element getElement(Document document) {

        Element element = document.createElement("DataCategoryResource");
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));

        if (isGet()) {

            element.appendChild(APIUtils.getElement(document, "StartDate", dataBrowser.getStartDate().toString()));
            if (dataBrowser.getEndDate() != null) {
                element.appendChild(APIUtils.getElement(document, "EndDate", dataBrowser.getEndDate().toString()));
            } else {
                element.appendChild(APIUtils.getElement(document, "EndDate", ""));
            }

            // add DataCategory
            element.appendChild(dataBrowser.getDataCategory().getElement(document));

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
            Sheet sheet = dataSheetService.getSheet(dataBrowser);
            if (sheet != null) {
                Pager pager = getPager(getItemsPerPage());
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(getPage());
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
            }

        } else if (getRequest().getMethod().equals(Method.POST) || getRequest().getMethod().equals(Method.PUT)) {

            // DataCategories
            if (dataCategory != null) {
                element.appendChild(dataCategory.getElement(document, false));
            } else if (dataCategories != null) {
                Element dataItemsElement = document.createElement("DataCategories");
                element.appendChild(dataItemsElement);
                for (DataCategory dc : dataCategories) {
                    dataItemsElement.appendChild(dc.getElement(document, false));
                }
            }

            // DataItems
            if (dataItem != null) {
                element.appendChild(dataItem.getElement(document, false));
            } else if (dataItems != null) {
                Element dataItemsElement = document.createElement("DataItems");
                element.appendChild(dataItemsElement);
                for (DataItem di : dataItems) {
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
    public boolean allowPut() {
        return true;
    }

    @Override
    public void post(Representation entity) {
        log.debug("post");
        postOrPut(entity);
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        postOrPut(entity);
    }

    // TODO: may be a more elegant way to handle incoming representations of different media types
    public void postOrPut(Representation entity) {
        log.debug("postOrPut");
        DataCategory thisDataCategory = dataBrowser.getDataCategory();
        dataItems = new ArrayList<DataItem>();
        dataCategories = new ArrayList<DataCategory>();
        MediaType mediaType = entity.getMediaType();
        if (MediaType.APPLICATION_XML.includes(mediaType)) {
            acceptXML(entity);
        } else if (MediaType.APPLICATION_JSON.includes(mediaType)) {
            acceptJSON(entity);
        } else {
            if (getRequest().getMethod().equals(Method.POST)) {
                acceptFormPost(getForm());
            } else if (getRequest().getMethod().equals(Method.PUT)) {
                acceptFormPut(getForm());
            }
        }
        if ((dataCategory != null) || (dataItem != null) || !dataCategories.isEmpty() || !dataItems.isEmpty()) {
            // clear caches
            pathItemService.removePathItemGroup(thisDataCategory.getEnvironment());
            dataSheetService.removeSheet(thisDataCategory);
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

    protected void acceptJSON(Representation entity) {
        log.debug("acceptJSON");
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
                    dataCategory = acceptFormForDataCategory(form);
                    if (dataCategory != null) {
                        dataCategories.add(dataCategory);
                    } else {
                        log.warn("Data Category not added/modified");
                        return;
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
                    dataItem = acceptFormForDataItem(form);
                    if (dataItem != null) {
                        dataItems.add(dataItem);
                    } else {
                        log.warn("Data Item not added/modified");
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            log.warn("Caught JSONException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected void acceptXML(Representation entity) {
        log.debug("acceptXML");
        DataCategory dataCategory;
        DataItem dataItem;
        Form form;
        org.dom4j.Element rootElem;
        org.dom4j.Element dataCategoriesElem;
        org.dom4j.Element dataItemsElem;
        org.dom4j.Element itemElem;
        org.dom4j.Element valueElem;
        try {
            rootElem = APIUtils.getRootElement(entity.getStream());
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
                        dataCategory = acceptFormForDataCategory(form);
                        if (dataCategory != null) {
                            dataCategories.add(dataCategory);
                        } else {
                            log.warn("Data Category not added");
                            return;
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
                        dataItem = acceptFormForDataItem(form);
                        if (dataItem != null) {
                            dataItems.add(dataItem);
                        } else {
                            log.warn("Data Item not added");
                            return;
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

    protected void acceptFormPost(Form form) {
        log.debug("acceptFormPost");
        String type = form.getFirstValue("newObjectType");
        if (type != null) {
            if (type.equalsIgnoreCase("DC")) {
                if (dataBrowser.getDataCategoryActions().isAllowCreate()) {
                    dataCategory = acceptFormForDataCategory(form);
                } else {
                    notAuthorized();
                }
            } else if (type.equalsIgnoreCase("DI")) {
                if (dataBrowser.getDataItemActions().isAllowCreate()) {
                    dataItem = acceptFormForDataItem(form);
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

    protected DataCategory acceptFormForDataCategory(Form form) {

        log.debug("acceptFormForDataCategory");

        String uid;
        DataCategory dataCategory = null;
        DataCategory thisDataCategory;

        thisDataCategory = dataBrowser.getDataCategory();
        if (getRequest().getMethod().equals(Method.POST)) {
            if (dataBrowser.getDataCategoryActions().isAllowCreate()) {
                // new DataCategory
                dataCategory = new DataCategory(thisDataCategory);
                if (form.getNames().contains("itemDefinitionUid")) {
                    ItemDefinition itemDefinition =
                            definitionService.getItemDefinition(thisDataCategory.getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                    if (itemDefinition != null) {
                        dataCategory.setItemDefinition(itemDefinition);
                    }
                }
                dataCategory = acceptDataCategory(form, dataCategory);
            } else {
                notAuthorized();
            }
        } else if (getRequest().getMethod().equals(Method.PUT)) {
            if (dataBrowser.getDataCategoryActions().isAllowModify()) {
                // update DataCategory
                uid = form.getFirstValue("dataCategoryUid");
                if (uid != null) {
                    dataCategory = dataService.getDataCategory(thisDataCategory, uid);
                    if (dataCategory != null) {
                        dataCategory = acceptDataCategory(form, dataCategory);
                    }
                }
            } else {
                notAuthorized();
            }
        }
        return dataCategory;
    }

    private DataCategory acceptDataCategory(Form form, DataCategory dataCategory) {
        dataCategory.setName(form.getFirstValue("name"));
        dataCategory.setPath(form.getFirstValue("path"));
        entityManager.persist(dataCategory);
        return dataCategory;
    }

    protected DataItem acceptFormForDataItem(Form form) {

        log.debug("acceptFormForDataItem");

        String uid;
        DataItem dataItem = null;
        ItemDefinition itemDefinition;
        DataCategory thisDataCategory;

        thisDataCategory = dataBrowser.getDataCategory();
        if (getRequest().getMethod().equals(Method.POST)) {
            if (dataBrowser.getDataItemActions().isAllowCreate()) {
                // new DataItem
                itemDefinition = thisDataCategory.getItemDefinition();
                if (itemDefinition != null) {
                    dataItem = new DataItem(thisDataCategory, itemDefinition);
                    dataItem = acceptDataItem(form, dataItem);
                } else {
                    badRequest();
                }
            } else {
                notAuthorized();
            }
        } else if (getRequest().getMethod().equals(Method.PUT)) {
            if (dataBrowser.getDataItemActions().isAllowCreate()) {
                // update DataItem
                uid = form.getFirstValue("dataItemUid");
                if (uid != null) {
                    dataItem = dataService.getDataItem(thisDataCategory, uid);
                    if (dataItem != null) {
                        dataItem = acceptDataItem(form, dataItem);
                    }
                }
            } else {
                notAuthorized();
            }
        }
        return dataItem;
    }

    protected DataItem acceptDataItem(Form form, DataItem dataItem) {
        dataItem.setName(form.getFirstValue("name"));
        dataItem.setPath(form.getFirstValue("path"));


        // determine startdate for new DataItem
        StartEndDate startDate = new StartEndDate(form.getFirstValue("startDate"));
        dataItem.setStartDate(startDate);


        if (form.getNames().contains("endDate"))
            dataItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));

        if (form.getNames().contains("duration")) {
            StartEndDate endDate = startDate.plus(form.getFirstValue("duration"));
            dataItem.setEndDate(endDate);
        }

        if (dataItem.getEndDate() != null && dataItem.getEndDate().before(dataItem.getStartDate())) {
            badRequest();
            return null;
        }

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
        return dataItem;
    }

    public void acceptFormPut(Form form) {
        log.debug("put");
        DataCategory thisDataCategory;
        if (dataBrowser.getDataCategoryActions().isAllowModify()) {
            thisDataCategory = dataBrowser.getDataCategory();
            if (form.getNames().contains("name")) {
                thisDataCategory.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("path")) {
                thisDataCategory.setPath(form.getFirstValue("path"));
            }
            if (form.getNames().contains("itemDefinitionUid")) {
                ItemDefinition itemDefinition =
                        definitionService.getItemDefinition(thisDataCategory.getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                if (itemDefinition != null) {
                    thisDataCategory.setItemDefinition(itemDefinition);
                } else {
                    thisDataCategory.setItemDefinition(null);
                }
            }
            pathItemService.removePathItemGroup(thisDataCategory.getEnvironment());
            dataSheetService.removeSheet(thisDataCategory);
            success(dataBrowser.getFullPath());
            dataCategory = thisDataCategory;
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
