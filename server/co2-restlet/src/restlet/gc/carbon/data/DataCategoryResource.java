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

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.APIFault;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.ResourceBuilder;
import gc.carbon.ResourceBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Component("dataCategoryResource")
@Scope("prototype")
public class DataCategoryResource extends BaseDataResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataService dataService;

    private DataCategory dataCategory;
    private List<DataCategory> dataCategories;
    private DataItem dataItem;
    private List<DataItem> dataItems;

    private ResourceBuilder builder;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        dataBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        setPage(request);
        setBuilderStrategy();
    }

    private void setBuilderStrategy() {
        builder = ResourceBuilderFactory.createDataCategoryBuilder(this);
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
        Map<String, Object> values = super.getTemplateValues();
        values.putAll(builder.getTemplateValues());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        return builder.getJSONObject();
    }

    @Override
    public Element getElement(Document document) {
        return builder.getElement(document);
    }

    @Override
    public void handleGet() {
        log.debug("handleGet()");
        if (dataBrowser.getDataCategoryActions().isAllowView()) {
            if (!getVersion().isVersionOne()) {
                Form form = getRequest().getResourceRef().getQueryAsForm();
                String startDate = form.getFirstValue("startDate");
                if (startDate != null) {
                    dataBrowser.setStartDate(form.getFirstValue("startDate"));
                }
                dataBrowser.setEndDate(form.getFirstValue("endDate"));
            }
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
    public void acceptRepresentation(Representation entity) {
        log.debug("acceptRepresentation()");
        acceptOrStore(entity);
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");
        acceptOrStore(entity);
    }

    public void acceptOrStore(Representation entity) {
        log.debug("acceptOrStore()");
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
            dataService.clearCaches(thisDataCategory);
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
        log.debug("acceptJSON()");
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
                        log.warn("acceptJSON() - Data Category not added/modified");
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
                        log.warn("acceptJSON() - Data Item not added/modified");
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            log.warn("acceptJSON() - Caught JSONException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.warn("acceptJSON() - Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected void acceptXML(Representation entity) {
        log.debug("acceptXML()");
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
                            log.warn("acceptXML() - Data Category not added");
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
                            log.warn("acceptXML() - Data Item not added");
                            return;
                        }
                    }
                }
            } else {
                log.warn("acceptXML() - DataCategory not found");
            }
        } catch (DocumentException e) {
            log.warn("acceptXML() - Caught DocumentException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.warn("acceptXML() - Caught IOException: " + e.getMessage(), e);
        }
    }

    protected void acceptFormPost(Form form) {
        log.debug("acceptFormPost()");
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

        log.debug("acceptFormForDataCategory()");

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
                            dataService.getItemDefinition(thisDataCategory.getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                    if (itemDefinition != null) {
                        dataCategory.setItemDefinition(itemDefinition);
                    }
                }
                dataCategory = populateDataCategory(form, dataCategory);

                if (!validDataCategory(dataCategory)) {
                    badRequest();
                } else {
                    dataCategory = acceptDataCategory(form, dataCategory);
                }
            } else {
                notAuthorized();
            }
        } else if (getRequest().getMethod().equals(Method.PUT)) {
            if (dataBrowser.getDataCategoryActions().isAllowModify()) {
                // update DataCategory
                uid = form.getFirstValue("dataCategoryUid");
                if (uid != null) {
                    dataCategory = dataService.getDataCategory(uid);
                    if (dataCategory != null) {
                        dataCategory = populateDataCategory(form, dataCategory);

                        if (!validDataCategory(dataCategory)) {
                            badRequest();
                        } else {
                            dataCategory = acceptDataCategory(form, dataCategory);
                        }
                    }
                }
            } else {
                notAuthorized();
            }
        }
        return dataCategory;
    }

    private boolean validDataCategory(DataCategory dataCategory) {
        if (StringUtils.isBlank(dataCategory.getName()) || StringUtils.isBlank(dataCategory.getPath())) {
            return false;
        }
        return true;
    }

    private DataCategory populateDataCategory(Form form, DataCategory dataCategory) {
        dataCategory.setName(form.getFirstValue("name"));
        dataCategory.setPath(form.getFirstValue("path"));
        return dataCategory;
    }

    private DataCategory acceptDataCategory(Form form, DataCategory dataCategory) {
        dataService.persist(dataCategory);
        return dataCategory;
    }

    protected DataItem acceptFormForDataItem(Form form) {

        log.debug("acceptFormForDataItem()");

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
                    dataItem = dataService.getDataItem(uid);
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


        if (form.getNames().contains("endDate")) {
            dataItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
        } else {
            if (form.getNames().contains("duration")) {
                StartEndDate endDate = startDate.plus(form.getFirstValue("duration"));
                dataItem.setEndDate(endDate);
            }
        }

        if (dataItem.getEndDate() != null && dataItem.getEndDate().before(dataItem.getStartDate())) {
            badRequest(APIFault.INVALID_DATE_RANGE);
            return null;
        }

        dataService.persist(dataItem);

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
        log.debug("acceptFormPut()");
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
                        dataService.getItemDefinition(thisDataCategory.getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                if (itemDefinition != null) {
                    thisDataCategory.setItemDefinition(itemDefinition);
                } else {
                    thisDataCategory.setItemDefinition(null);
                }
            }
            dataService.clearCaches(thisDataCategory);
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
    public void removeRepresentations() {
        log.debug("removeRepresentations()");
        if (dataBrowser.getDataCategoryActions().isAllowDelete()) {
            DataCategory dataCategory = dataBrowser.getDataCategory();
            dataService.clearCaches(dataBrowser.getDataCategory());
            dataService.remove(dataCategory);
            success(pathItem.getParent().getFullPath());
        } else {
            notAuthorized();
        }
    }

    public DataService getDataService() {
        return dataService;
    }

    public DataBrowser getDataBrowser() {
        return dataBrowser;
    }

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    public List<DataCategory> getDataCategories() {
        return dataCategories;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public List<DataItem> getDataItems() {
        return dataItems;
    }
}
