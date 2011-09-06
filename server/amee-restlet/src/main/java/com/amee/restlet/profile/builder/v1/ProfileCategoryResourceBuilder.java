package com.amee.restlet.profile.builder.v1;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.IDataCategoryReference;
import com.amee.domain.Pager;
import com.amee.domain.data.DataCategory;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.builder.v1.ProfileItemBuilder;
import com.amee.domain.sheet.Cell;
import com.amee.domain.sheet.Row;
import com.amee.domain.sheet.Sheet;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.profile.builder.IProfileCategoryResourceBuilder;
import com.amee.service.data.DataService;
import com.amee.service.item.DataItemServiceImpl;
import com.amee.service.item.ProfileItemServiceImpl;
import com.amee.service.profile.ProfileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

@Service("v1ProfileCategoryResourceBuilder")
public class ProfileCategoryResourceBuilder implements IProfileCategoryResourceBuilder {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataService dataService;

    @Autowired
    private DataItemServiceImpl dataItemService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileItemServiceImpl profileItemService;

    public JSONObject getJSONObject(ProfileCategoryResource resource) throws JSONException {
        JSONObject obj = new JSONObject();
        addProfileCategoryInfo(resource, obj);
        if (resource.isGet()) {
            addProfileCategoryChildren(resource, obj, resource.getDataCategory());
        } else {
            addNewProfileItems(resource, obj);
        }
        return obj;
    }

    public void addProfileCategoryInfo(ProfileCategoryResource resource, JSONObject obj) throws JSONException {

        // add objects
        obj.put("path", resource.getDataCategory().getFullPath());
        obj.put("profileDate", resource.getProfileBrowser().getProfileDate());

        // add relevant Profile info depending on whether we are at root
        if (!resource.getDataCategory().getPath().isEmpty()) {
            obj.put("profile", resource.getProfile().getIdentityJSONObject());
        } else {
            obj.put("profile", resource.getProfile().getJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", resource.getDataCategory().getIdentityJSONObject());
    }

    protected JSONObject getProfileCategoryJSONObject(ProfileCategoryResource resource, IDataCategoryReference dc) throws JSONException {

        DataCategory dataCategory = dataService.getDataCategoryByUid(dc.getEntityUid());
        if (dataCategory == null) {
            throw new IllegalStateException("Data Category should not be null.");
        }

        JSONObject obj = new JSONObject();

        // Add path and DataCategory.
        obj.put("path", dc.getFullPath());
        obj.put("dataCategory", dataCategory.getJSONObject());

        // Only add child Data Categories if ProfileItems are available within.
        if (resource.getProfileDataCategoryIds().contains(dc.getEntityId())) {
            addProfileCategoryChildren(resource, obj, dc);
        }

        return obj;
    }

    protected void addProfileCategoryChildren(ProfileCategoryResource resource, JSONObject obj, IDataCategoryReference dataCategory) throws JSONException {

        Pager pager = null;

        // create children JSON
        JSONObject children = new JSONObject();

        // add Data Categories to children
        JSONArray dataCategories = new JSONArray();
        for (IDataCategoryReference dc : dataService.getDataCategories(dataCategory).values()) {
            if (resource.isRecurse()) {
                dataCategories.put(getProfileCategoryJSONObject(resource, dc));
            } else {
                JSONObject dcObj = new JSONObject();
                dcObj.put("uid", dc.getEntityUid());
                dcObj.put("name", dc.getName());
                dcObj.put("path", dc.getPath());
                dataCategories.put(dcObj);
            }
        }
        children.put("dataCategories", dataCategories);

        // add Sheet containing Profile Items & totalAmountPerMonth
        log.debug("addProfileCategoryChildren() " + dataCategory.getFullPath());
        Sheet sheet = getSheet(resource, dataCategory);
        if (sheet != null) {
            // don't use pagination in recursive mode
            if (resource.isRecurse()) {
                sheet = Sheet.getCopy(sheet, true);
            } else {
                pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
            }
            children.put("profileItems", sheet.getJSONObject());
            if (pager != null) {
                children.put("pager", pager.getJSONObject());
            }
            obj.put("totalAmountPerMonth", getTotalAmountPerMonth(sheet));
        } else {
            children.put("profileItems", new JSONObject());
            children.put("pager", new JSONObject());
            obj.put("totalAmountPerMonth", "0");
        }

        // add children
        obj.put("children", children);
    }

    protected void addNewProfileItems(ProfileCategoryResource resource, JSONObject obj) throws JSONException {
        if (!resource.getProfileItems().isEmpty()) {
            if (resource.getProfileItems().size() == 1) {
                ProfileItem pi = resource.getProfileItems().get(0);
                obj.put("profileItem", new ProfileItemBuilder(pi, dataItemService, profileItemService).getJSONObject(true));
            } else {
                JSONArray profileItems = new JSONArray();
                obj.put("profileItems", profileItems);
                for (ProfileItem pi : resource.getProfileItems()) {
                    profileItems.put(new ProfileItemBuilder(pi, dataItemService, profileItemService).getJSONObject(false));
                }
            }
        }
    }

    public Element getElement(ProfileCategoryResource resource, Document document) {
        Element element = document.createElement("ProfileCategoryResource");
        addProfileCategoryInfo(resource, document, element);
        if (resource.isGet()) {
            addProfileCategoryChildren(resource, document, element, resource.getDataCategory());
        } else {
            addNewProfileItems(resource, document, element);
        }
        return element;
    }

    protected void addProfileCategoryInfo(ProfileCategoryResource resource, Document document, Element element) {

        // add objects
        element.appendChild(XMLUtils.getElement(document, "Path", resource.getDataCategory().getFullPath()));

        // add profile date
        element.appendChild(XMLUtils.getElement(document, "ProfileDate", resource.getProfileBrowser().getProfileDate().toString()));

        // add relevant Profile info depending on whether we are at root
        if (!resource.getDataCategory().getPath().isEmpty()) {
            element.appendChild(resource.getProfile().getIdentityElement(document));
        } else {
            element.appendChild(resource.getProfile().getElement(document));
        }

        // add DataCategory and Profile elements
        element.appendChild(resource.getDataCategory().getIdentityElement(document));
    }

    protected Element getProfileCategoryElement(ProfileCategoryResource resource, Document document, IDataCategoryReference dc) {

        DataCategory dataCategory = dataService.getDataCategoryByUid(dc.getEntityUid());
        if (dataCategory == null) {
            throw new IllegalStateException("Data Category should not be null.");
        }

        Element element = document.createElement("ProfileCategory");

        // Add path and DataCategory.
        element.appendChild(XMLUtils.getElement(document, "Path", dc.getFullPath()));
        element.appendChild(dataCategory.getIdentityElement(document));

        // Only add child Data Categories if ProfileItems are available within.
        if (resource.getProfileDataCategoryIds().contains(dc.getEntityId())) {
            addProfileCategoryChildren(resource, document, element, dc);
        }

        return element;
    }

    protected void addProfileCategoryChildren(ProfileCategoryResource resource, Document document, Element element, IDataCategoryReference dataCategory) {

        Pager pager = null;

        // list child Profile Categories and child Profile Items
        org.w3c.dom.Element childrenElement = document.createElement("Children");
        element.appendChild(childrenElement);

        // add Data Categories
        Element profileCategoriesElement = document.createElement("ProfileCategories");
        for (IDataCategoryReference dc : dataService.getDataCategories(dataCategory).values()) {
            if (resource.isRecurse()) {
                profileCategoriesElement.appendChild(getProfileCategoryElement(resource, document, dc));
            } else {
                Element dcElement = document.createElement("DataCategory");
                dcElement.setAttribute("uid", dc.getEntityUid());
                dcElement.appendChild(XMLUtils.getElement(document, "Name", dc.getName()));
                dcElement.appendChild(XMLUtils.getElement(document, "Path", dc.getPath()));
                profileCategoriesElement.appendChild(dcElement);
            }
        }
        childrenElement.appendChild(profileCategoriesElement);

        // get Sheet containing Profile Items
        Sheet sheet = getSheet(resource, dataCategory);
        if (sheet != null) {
            // don't use pagination in recursive mode
            if (resource.isRecurse()) {
                sheet = Sheet.getCopy(sheet, true);
            } else {
                pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
            }
            // list child Profile Items via sheet
            childrenElement.appendChild(sheet.getElement(document, false));
            if (pager != null) {
                childrenElement.appendChild(pager.getElement(document));
            }
            // add CO2 amount
            element.appendChild(XMLUtils.getElement(document, "TotalAmountPerMonth",
                    Double.toString(getTotalAmountPerMonth(sheet))));
        }
    }

    protected void addNewProfileItems(ProfileCategoryResource resource, Document document, Element element) {
        if (!resource.getProfileItems().isEmpty()) {
            if (resource.getProfileItems().size() == 1) {
                ProfileItem pi = resource.getProfileItems().get(0);
                element.appendChild(new ProfileItemBuilder(pi, dataItemService, profileItemService).getElement(document, false));
            } else {
                org.w3c.dom.Element profileItemsElement = document.createElement("ProfileItems");
                element.appendChild(profileItemsElement);
                for (ProfileItem pi : resource.getProfileItems()) {
                    profileItemsElement.appendChild(new ProfileItemBuilder(pi, dataItemService, profileItemService).getElement(document, false));
                }
            }
        }
    }

    public org.apache.abdera.model.Element getAtomElement(ProfileCategoryResource resource) {
        throw new UnsupportedOperationException();
    }

    private Sheet getSheet(ProfileCategoryResource resource, IDataCategoryReference dataCategory) {
        return profileService.getSheet(new ProfileSheetBuilder(resource, dataService, profileItemService, dataItemService, dataCategory));
    }

    private Sheet getSheet(ProfileCategoryResource resource) {
        return profileService.getSheet(new ProfileSheetBuilder(resource, dataService, profileItemService, dataItemService));
    }

    public Map<String, Object> getTemplateValues(ProfileCategoryResource resource) {
        Profile profile = resource.getProfile();
        DataCategory dataCategory = resource.getDataCategory();
        Sheet sheet = getSheet(resource);
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profile);
        values.put("dataCategory", dataCategory);
        values.put("node", dataCategory);
        values.put("dataCategories", dataService.getDataCategories(dataCategory).values());
        values.put("sheet", sheet);
        if (sheet != null) {
            values.put("totalAmountPerMonth", getTotalAmountPerMonth(sheet));
        }
        return values;
    }

    public double getTotalAmountPerMonth(Sheet sheet) {
        Cell endCell;
        double totalAmountPerMonth = 0.0;
        double amountPerMonth;
        for (Row row : sheet.getRows()) {
            endCell = row.findCell("end");
            if (!endCell.getValueAsBoolean()) {
                amountPerMonth = row.findCell("amountPerMonth").getValueAsDouble();
                totalAmountPerMonth = totalAmountPerMonth + amountPerMonth;
            }
        }
        return totalAmountPerMonth;
    }
}
