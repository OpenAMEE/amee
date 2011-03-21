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
package com.amee.restlet.data;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.*;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.item.data.DataItem;
import com.amee.restlet.AMEEResource;
import com.amee.restlet.RequestContext;
import com.amee.restlet.data.builder.DataCategoryResourceBuilder;
import com.amee.restlet.utils.APIFault;
import com.amee.restlet.utils.APIUtils;
import com.amee.service.data.DataBrowser;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DataService;
import com.amee.service.definition.DefinitionService;
import com.amee.service.invalidation.InvalidationService;
import com.amee.service.item.DataItemService;
import com.amee.service.profile.ProfileService;
import org.apache.commons.lang.StringUtils;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Component("dataCategoryResource")
@Scope("prototype")
public class DataCategoryResource extends AMEEResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LocaleService localeService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataItemService dataItemService;

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DataBrowser dataBrowser;

    @Autowired
    private DataCategoryResourceBuilder builder;

    private DataCategory dataCategory;
    private DataCategory modDataCategory;
    private List<DataCategory> newDataCategories;
    private DataItem modDataItem;
    private List<DataItem> newDataItems;
    private String newObjectType = "";
    @Autowired
    protected ProfileService profileService;

    @Autowired
    private InvalidationService invalidationService;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);

        // Obtain DataCategory.
        dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
        dataBrowser.setDataCategory(dataCategory);
        (ThreadBeanHolder.get(RequestContext.class)).setDataCategory(dataCategory);
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (dataCategory != null) &&
                !dataCategory.isTrash();
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        DataCategory dc = dataCategory;
        while (dc != null) {
            entities.add(dc);
            dc = dc.getDataCategory();
        }
        Collections.reverse(entities);
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + DataConstants.VIEW_DATA_CATEGORY;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.putAll(builder.getTemplateValues(this));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        return builder.getJSONObject(this);
    }

    @Override
    public Element getElement(Document document) {
        return builder.getElement(this, document);
    }

    @Override
    public void doGet() {
        log.debug("doGet()");
        if (getAPIVersion().isNotVersionOne()) {
            Form form = getRequest().getResourceRef().getQueryAsForm();
            dataBrowser.setQueryStartDate(form.getFirstValue("startDate"));
            dataBrowser.setQueryEndDate(form.getFirstValue("endDate"));
        }
        super.doGet();
    }

    @Override
    public void doAcceptOrStore(Representation entity) {

        log.debug("doAcceptOrStore()");

        newDataItems = new ArrayList<DataItem>();
        newDataCategories = new ArrayList<DataCategory>();

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

        if ((modDataCategory != null) || (modDataItem != null) || !newDataCategories.isEmpty() || !newDataItems.isEmpty()) {
            if (isPost()) {
                if (isBatchPost()) {
                    invalidationService.invalidate(dataCategory);
                    successfulBatchPost();
                } else if ((modDataCategory != null) && newObjectType.equalsIgnoreCase(ObjectType.DC.getName())) {
                    invalidationService.invalidate(modDataCategory);
                    invalidationService.invalidate(dataCategory);
                    successfulPost(modDataCategory.getPath());
                } else if ((modDataItem != null) && newObjectType.equalsIgnoreCase(ObjectType.DI.getName())) {
                    invalidationService.invalidate(dataCategory);
                    successfulPost(modDataItem.getUid());
                } else {
                    badRequest();
                }
            } else {
                successfulPut(getFullPath());
            }
        } else {
            badRequest();
        }
    }

    protected void acceptJSON(Representation entity) {
        log.debug("acceptJSON()");
        setIsBatchPost(true);
        DataCategory newDataCategory;
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
                    newDataCategory = acceptFormForDataCategory(form);
                    if (newDataCategory != null) {
                        newDataCategories.add(newDataCategory);
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
                        newDataItems.add(dataItem);
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
        setIsBatchPost(true);
        DataCategory newDataCategory;
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
                        newDataCategory = acceptFormForDataCategory(form);
                        if (newDataCategory != null) {
                            newDataCategories.add(newDataCategory);
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
                            newDataItems.add(dataItem);
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
        newObjectType = form.getFirstValue("newObjectType");
        if (newObjectType != null) {
            if (newObjectType.equalsIgnoreCase(ObjectType.DC.getName())) {
                modDataCategory = acceptFormForDataCategory(form);
            } else if (newObjectType.equalsIgnoreCase(ObjectType.DI.getName())) {
                modDataItem = acceptFormForDataItem(form);
            } else {
                badRequest();
            }
        } else {
            badRequest();
        }
    }

    protected DataCategory acceptFormForDataCategory(Form form) {
        log.debug("acceptFormForDataCategory()");
        DataCategory dataCategory = null;
        if (getRequest().getMethod().equals(Method.POST)) {
            dataCategory = acceptFormForDataCategoryPost(form);
        } else if (getRequest().getMethod().equals(Method.PUT)) {
            dataCategory = acceptFormForDataCategoryPut(form);
        }
        return dataCategory;
    }

    /**
     * Creates a new DataCategory based on a POST.
     *
     * @param form
     * @return
     */
    protected DataCategory acceptFormForDataCategoryPost(Form form) {

        // Create new DataCategory with the current DataCategory as a parent. 
        DataCategory newDataCategory = new DataCategory(dataCategory);

        // Set Item Definition.
        if (form.getNames().contains("itemDefinitionUid")) {
            ItemDefinition itemDefinition =
                    definitionService.getItemDefinitionByUid(form.getFirstValue("itemDefinitionUid"));
            if (itemDefinition != null) {
                newDataCategory.setItemDefinition(itemDefinition);
            }
        }

        // Populate Data Category fields.
        newDataCategory = populateDataCategory(form, newDataCategory);

        // Validate.
        if (!validDataCategory(newDataCategory)) {
            badRequest();
            // TODO: Should we set dataCategory to null here?
        } else if (!dataService.isDataCategoryUniqueByPath(newDataCategory)) {
            badRequest(APIFault.DUPLICATE_ITEM);
            newDataCategory = null;
        } else {
            newDataCategory = acceptDataCategory(form, newDataCategory);
        }

        return newDataCategory;
    }

    /**
     * Updates a DataCategory based on a PUT.
     *
     * @param form
     * @return
     */
    protected DataCategory acceptFormForDataCategoryPut(Form form) {
        DataCategory updatedDataCategory = null;
        String uid = form.getFirstValue("dataCategoryUid");
        if (uid != null) {
            updatedDataCategory = dataService.getDataCategoryByUid(uid);
            if (updatedDataCategory != null) {
                updatedDataCategory = populateDataCategory(form, updatedDataCategory);
                if (!validDataCategory(updatedDataCategory)) {
                    badRequest();
                } else {
                    updatedDataCategory = acceptDataCategory(form, updatedDataCategory);
                }
            }
        }
        return updatedDataCategory;
    }

    private boolean validDataCategory(DataCategory dataCategory) {
        return !(StringUtils.isBlank(dataCategory.getName()) || StringUtils.isBlank(dataCategory.getPath()));
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

        if (getRequest().getMethod().equals(Method.POST)) {
            // New DataItem.
            itemDefinition = dataCategory.getItemDefinition();
            if (itemDefinition != null) {
                // Create new DataItem, persist it and populate it.
                dataItem = new DataItem(dataCategory, itemDefinition);
                dataItemService.persist(dataItem);
                acceptDataItem(form, dataItem);
            } else {
                badRequest();
            }
        } else if (getRequest().getMethod().equals(Method.PUT)) {
            // Update DataItem.
            uid = form.getFirstValue("dataItemUid");
            if (uid != null) {
                dataItem = dataItemService.getDataItemByUid(dataCategory, uid);
                if (dataItem != null) {
                    acceptDataItem(form, dataItem);
                }
            }
        }

        // Ensure Item Value cache is cleared.
        dataItemService.clearItemValues();

        return dataItem;
    }

    /**
     * Update the DataItem and contained ItemValues based on POST or PUT parameters. ItemValues can be identified
     * by their ItemValueDefinition path or, for existing DataItems, their specific UID. The 'name' and 'path'
     * properties of the DataItem can be set / updated.
     * <p/>
     * When updating ItemValues using the ItemValueDefinition path the appropriate instance will
     * be selected based the current time, this will always be the last ItemValue in chronological order. This
     * is only relevant for non drill-down ItemValues, as only once instance of these is allowed.
     *
     * @param form     to handle
     * @param dataItem to update
     */
    protected void acceptDataItem(Form form, DataItem dataItem) {

        boolean modified = false;
        Set<String> names = form.getNames();

        // Set 'name' value.
        if (names.contains("name")) {
            dataItem.setName(form.getFirstValue("name"));
            names.remove("name");
            modified = true;
        }

        // Set 'path' value.
        if (names.contains("path")) {
            dataItem.setPath(form.getFirstValue("path"));
            names.remove("path");
            modified = true;
        }

        // Update item values if supplied.
        for (String name : form.getNames()) {
            BaseItemValue itemValue = dataItemService.getItemValue(dataItem, name);
            if (itemValue != null) {
                itemValue.setValue(form.getFirstValue(name));
                modified = true;
            } else {
                log.warn("acceptDataItem() An ItemValue identifier was specified that does not exist: " + name);
            }
        }

        // Modified?
        if (modified) {
            dataItem.onModify();
        }
    }

    public void acceptFormPut(Form form) {

        log.debug("acceptFormPut()");

        // We use modDataCategory to see if the Data Category has been updated.
        modDataCategory = dataCategory;

        if (form.getNames().contains("name")) {
            modDataCategory.setName(form.getFirstValue("name"));
        }
        if (form.getNames().contains("path")) {
            modDataCategory.setPath(form.getFirstValue("path"));
        }
        if (form.getNames().contains("itemDefinitionUid")) {
            ItemDefinition itemDefinition =
                    definitionService.getItemDefinitionByUid(form.getFirstValue("itemDefinitionUid"));
            if (itemDefinition != null) {
                modDataCategory.setItemDefinition(itemDefinition);
            } else {
                modDataCategory.setItemDefinition(null);
            }
        }

        String deprecated = form.getFirstValue("deprecated");
        if (StringUtils.isNotBlank(deprecated)) {
            if (deprecated.equals("true")) {
                modDataCategory.setStatus(AMEEStatus.DEPRECATED);
            } else if (deprecated.equals("false")) {
                modDataCategory.setStatus(AMEEStatus.ACTIVE);
            }
        }

        // Parse any submitted locale names.
        for (String name : form.getNames()) {
            if (name.startsWith("name_")) {
                // Get locale and locale name to handle.
                String locale = name.substring(name.indexOf("_") + 1);
                // Validate - Must have an available locale.
                if (!LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                    badRequest(APIFault.INVALID_PARAMETERS);
                    return;
                }
                // Remove or Update/Create?
                if (form.getNames().contains("remove_name_" + locale)) {
                    // Remove.
                    localeService.clearLocaleName(modDataCategory, locale);
                    modDataCategory.onModify();
                } else {
                    // Update or create.
                    String localeNameStr = form.getFirstValue(name);
                    // Validate - Must have a locale name value.
                    if (StringUtils.isBlank(localeNameStr)) {
                        badRequest(APIFault.INVALID_PARAMETERS);
                        return;
                    }
                    // Do the update or create.
                    localeService.setLocaleName(modDataCategory, locale, localeNameStr);
                    modDataCategory.onModify();
                }
            }
        }

        invalidationService.invalidate(modDataCategory);
        successfulPut(getFullPath());
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        invalidationService.invalidate(dataCategory);
        dataService.remove(dataCategory);
        successfulDelete("/data/" + dataCategory.getDataCategory().getFullPath());
    }

    public String getFullPath() {
        return "/data" + dataCategory.getFullPath();
    }

    public DataBrowser getDataBrowser() {
        return dataBrowser;
    }

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    public DataCategory getModDataCategory() {
        return modDataCategory;
    }

    public List<DataCategory> getNewDataCategories() {
        return newDataCategories;
    }

    public DataItem getModDataItem() {
        return modDataItem;
    }

    public List<DataItem> getNewDataItems() {
        return newDataItems;
    }
}
