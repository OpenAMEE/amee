package com.amee.restlet.profile.builder.v1;

import com.amee.core.ObjectType;
import com.amee.domain.APIUtils;
import com.amee.domain.Pager;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.Decimal;
import com.amee.domain.path.PathItem;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.profile.builder.v1.ProfileItemBuilder;
import com.amee.domain.sheet.Cell;
import com.amee.domain.sheet.Row;
import com.amee.domain.sheet.Sheet;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.profile.builder.IProfileCategoryResourceBuilder;
import com.amee.service.data.DataService;
import com.amee.service.profile.ProfileService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
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
@Service("v1ProfileCategoryResourceBuilder")
public class ProfileCategoryResourceBuilder implements IProfileCategoryResourceBuilder {

    @Autowired
    private DataService dataService;

    @Autowired
    private ProfileService profileService;

    public JSONObject getJSONObject(ProfileCategoryResource resource) throws JSONException {
        JSONObject obj = new JSONObject();
        addProfileCategoryInfo(resource, obj);
        if (resource.isGet()) {
            addProfileCategoryChildren(resource, obj, resource.getPathItem(), resource.getDataCategory());
        } else {
            addNewProfileItems(resource, obj);
        }
        return obj;
    }

    public void addProfileCategoryInfo(ProfileCategoryResource resource, JSONObject obj) throws JSONException {

        // add objects
        obj.put("path", resource.getPathItem().getFullPath());
        obj.put("profileDate", resource.getProfileBrowser().getProfileDate());

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            obj.put("profile", resource.getProfile().getIdentityJSONObject());
        } else {
            obj.put("profile", resource.getProfile().getJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", resource.getDataCategory().getIdentityJSONObject());
    }

    protected JSONObject getProfileCategoryJSONObject(ProfileCategoryResource resource, PathItem pathItem) throws JSONException {

        DataCategory dataCategory = dataService.getDataCategory(pathItem.getUid());

        JSONObject obj = new JSONObject();

        // add path and DataCategory
        obj.put("path", pathItem.getFullPath());
        obj.put("dataCategory", dataCategory.getJSONObject());

        // only add children if ProfileItems are available
        if (pathItem.hasChildrenByType(ObjectType.PI, true)) {
            addProfileCategoryChildren(resource, obj, pathItem, dataCategory);
        }

        return obj;
    }

    protected void addProfileCategoryChildren(ProfileCategoryResource resource, JSONObject obj, PathItem pathItem, DataCategory dataCategory) throws JSONException {

        Pager pager = null;

        // create children JSON
        JSONObject children = new JSONObject();

        // add Data Categories via pathItem to children
        JSONArray dataCategories = new JSONArray();
        for (PathItem pi : pathItem.getChildrenByType("DC")) {
            if (resource.isRecurse()) {
                dataCategories.put(getProfileCategoryJSONObject(resource, pi));
            } else {
                dataCategories.put(pi.getJSONObject());
            }
        }
        children.put("dataCategories", dataCategories);

        // add Sheet containing Profile Items & totalAmountPerMonth
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

        // add chilren
        obj.put("children", children);
    }

    protected void addNewProfileItems(ProfileCategoryResource resource, JSONObject obj) throws JSONException {
        if (!resource.getProfileItems().isEmpty()) {
            if (resource.getProfileItems().size() == 1) {
                ProfileItem pi = resource.getProfileItems().get(0);
                setBuilder(pi);
                obj.put("profileItem", pi.getJSONObject(true));
            } else {
                JSONArray profileItems = new JSONArray();
                obj.put("profileItems", profileItems);
                for (ProfileItem pi : resource.getProfileItems()) {
                    setBuilder(pi);
                    profileItems.put(pi.getJSONObject(false));
                }
            }
        }
    }

    public Element getElement(ProfileCategoryResource resource, Document document) {
        Element element = document.createElement("ProfileCategoryResource");
        addProfileCategoryInfo(resource, document, element);
        if (resource.isGet()) {
            addProfileCategoryChildren(resource, document, element, resource.getPathItem(), resource.getDataCategory());
        } else {
            addNewProfileItems(resource, document, element);
        }
        return element;
    }

    protected void addProfileCategoryInfo(ProfileCategoryResource resource, Document document, Element element) {

        // add objects
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));

        // add profile date
        element.appendChild(APIUtils.getElement(document, "ProfileDate", resource.getProfileBrowser().getProfileDate().toString()));

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            element.appendChild(resource.getProfile().getIdentityElement(document));
        } else {
            element.appendChild(resource.getProfile().getElement(document));
        }

        // add DataCategory and Profile elements
        element.appendChild(resource.getDataCategory().getIdentityElement(document));
    }

    protected Element getProfileCategoryElement(ProfileCategoryResource resource, Document document, PathItem pathItem) {

        DataCategory dataCategory = dataService.getDataCategory(pathItem.getUid());

        Element element = document.createElement("ProfileCategory");

        // add path and DataCategory
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        element.appendChild(dataCategory.getIdentityElement(document));

        // only add children if ProfileItems are available
        if (pathItem.hasChildrenByType(ObjectType.PI, true)) {
            addProfileCategoryChildren(resource, document, element, pathItem, dataCategory);
        }

        return element;
    }

    protected void addProfileCategoryChildren(ProfileCategoryResource resource, Document document, Element element, PathItem pathItem, DataCategory dataCategory) {

        Pager pager = null;

        // list child Profile Categories and child Profile Items
        org.w3c.dom.Element childrenElement = document.createElement("Children");
        element.appendChild(childrenElement);

        // add Profile Categories via pathItem
        org.w3c.dom.Element profileCategoriesElement = document.createElement("ProfileCategories");
        for (PathItem pi : pathItem.getChildrenByType("DC")) {
            if (resource.isRecurse()) {
                profileCategoriesElement.appendChild(getProfileCategoryElement(resource, document, pi));
            } else {
                profileCategoriesElement.appendChild(pi.getElement(document));
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
            element.appendChild(APIUtils.getElement(document, "TotalAmountPerMonth",
                    getTotalAmountPerMonth(sheet).toString()));
        }
    }

    protected void addNewProfileItems(ProfileCategoryResource resource, Document document, Element element) {
        if (!resource.getProfileItems().isEmpty()) {
            if (resource.getProfileItems().size() == 1) {
                ProfileItem pi = resource.getProfileItems().get(0);
                setBuilder(pi);
                element.appendChild(pi.getElement(document, false));
            } else {
                org.w3c.dom.Element profileItemsElement = document.createElement("ProfileItems");
                element.appendChild(profileItemsElement);
                for (ProfileItem pi : resource.getProfileItems()) {
                    setBuilder(pi);
                    profileItemsElement.appendChild(pi.getElement(document, false));
                }
            }
        }
    }

    public org.apache.abdera.model.Element getAtomElement(ProfileCategoryResource resource) {
        throw new UnsupportedOperationException();
    }

    private void setBuilder(ProfileItem pi) {
        pi.setBuilder(new ProfileItemBuilder(pi));
    }

    private Sheet getSheet(ProfileCategoryResource resource, DataCategory dataCategory) {
        return profileService.getSheet(dataCategory, new ProfileSheetBuilder(resource, profileService));
    }

    private Sheet getSheet(ProfileCategoryResource resource) {
        return profileService.getSheet(new ProfileSheetBuilder(resource, profileService));
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
        values.put("sheet", sheet);
        if (sheet != null) {
            values.put("totalAmountPerMonth", getTotalAmountPerMonth(sheet));
        }
        return values;
    }

    public BigDecimal getTotalAmountPerMonth(Sheet sheet) {
        Cell endCell;
        BigDecimal totalAmountPerMonth = Decimal.ZERO;
        BigDecimal amountPerMonth;
        for (Row row : sheet.getRows()) {
            endCell = row.findCell("end");
            if (!endCell.getValueAsBoolean()) {
                amountPerMonth = row.findCell("amountPerMonth").getValueAsBigDecimal();
                totalAmountPerMonth = totalAmountPerMonth.add(amountPerMonth);
            }
        }
        return totalAmountPerMonth;
    }
}
