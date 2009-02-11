package gc.carbon.profile.builder.v1;

import com.jellymold.sheet.Cell;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.Pager;
import com.jellymold.utils.cache.CacheableFactory;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.ResourceBuilder;
import gc.carbon.data.DataService;
import gc.carbon.domain.ObjectType;
import gc.carbon.domain.Unit;
import gc.carbon.domain.PerUnit;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.builder.v1.ProfileItemBuilder;
import gc.carbon.profile.ProfileCategoryResource;
import gc.carbon.profile.ProfileService;
import gc.carbon.profile.builder.v2.AtomFeed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
public class ProfileCategoryResourceBuilder implements ResourceBuilder {

    private final Log log = LogFactory.getLog(getClass());

    private CacheableFactory sheetBuilder;
    private DataService dataService;
    private ProfileService profileService;
    private ProfileCategoryResource resource;

    public ProfileCategoryResourceBuilder(ProfileCategoryResource resource) {
        this.resource = resource;
        this.dataService = resource.getDataService();
        this.profileService = resource.getProfileService();
        this.sheetBuilder = new ProfileSheetBuilder(profileService);
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        addProfileCategoryInfo(obj);
        if (resource.isGet()) {
            addProfileCategoryChildren(obj, resource.getPathItem(), resource.getDataCategory());
        } else {
            addNewProfileItems(obj);
        }
        return obj;
    }

    public void addProfileCategoryInfo(JSONObject obj) throws JSONException {

        // add objects
        obj.put("path", resource.getFullPath());
        obj.put("profileDate", resource.getProfileDate());

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            obj.put("profile", resource.getProfile().getIdentityJSONObject());
        } else {
            obj.put("profile", resource.getProfile().getJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", resource.getDataCategory().getIdentityJSONObject());
    }

    protected JSONObject getProfileCategoryJSONObject(PathItem pathItem) throws JSONException {

        DataCategory dataCategory = dataService.getDataCategory(pathItem.getUid());

        JSONObject obj = new JSONObject();

        // add path and DataCategory
        obj.put("path", pathItem.getFullPath());
        obj.put("dataCategory", dataCategory.getJSONObject());

        // only add children if ProfileItems are available
        if (pathItem.hasChildrenByType(ObjectType.PI, true)) {
            addProfileCategoryChildren(obj, pathItem, dataCategory);
        }

        return obj;
    }

    protected void addProfileCategoryChildren(JSONObject obj, PathItem pathItem, DataCategory dataCategory) throws JSONException {

        Pager pager = null;

        // create children JSON
        JSONObject children = new JSONObject();

        // add Data Categories via pathItem to children
        JSONArray dataCategories = new JSONArray();
        for (PathItem pi : pathItem.getChildrenByType("DC")) {
            if (resource.isRecurse()) {
                dataCategories.put(getProfileCategoryJSONObject(pi));
            } else {
                dataCategories.put(pi.getJSONObject());
            }
        }
        children.put("dataCategories", dataCategories);

        // add Sheet containing Profile Items & totalAmountPerMonth
        Sheet sheet = getSheet(dataCategory);
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

    protected void addNewProfileItems(JSONObject obj) throws JSONException {
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

    public Element getElement(Document document) {
        Element element = document.createElement("ProfileCategoryResource");
        addProfileCategoryInfo(document, element);
        if (resource.isGet()) {
            addProfileCategoryChildren(document, element, resource.getPathItem(), resource.getDataCategory());
        } else {
            addNewProfileItems(document, element);
        }
        return element;
    }

    protected void addProfileCategoryInfo(Document document, Element element) {

        // add objects
        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));

        // add profile date
        element.appendChild(APIUtils.getElement(document, "ProfileDate", resource.getProfileDate().toString()));

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            element.appendChild(resource.getProfile().getIdentityElement(document));
        } else {
            element.appendChild(resource.getProfile().getElement(document));
        }

        // add DataCategory and Profile elements
        element.appendChild(resource.getDataCategory().getIdentityElement(document));
    }

    protected Element getProfileCategoryElement(Document document, PathItem pathItem) {

        DataCategory dataCategory = dataService.getDataCategory(pathItem.getUid());

        Element element = document.createElement("ProfileCategory");

        // add path and DataCategory
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        element.appendChild(dataCategory.getIdentityElement(document));

        // only add children if ProfileItems are available
        if (pathItem.hasChildrenByType(ObjectType.PI, true)) {
            addProfileCategoryChildren(document, element, pathItem, dataCategory);
        }

        return element;
    }

    protected void addProfileCategoryChildren(Document document, Element element, PathItem pathItem, DataCategory dataCategory) {

        Pager pager = null;

        // list child Profile Categories and child Profile Items
        org.w3c.dom.Element childrenElement = document.createElement("Children");
        element.appendChild(childrenElement);

        // add Profile Categories via pathItem
        org.w3c.dom.Element profileCategoriesElement = document.createElement("ProfileCategories");
        for (PathItem pi : pathItem.getChildrenByType("DC")) {
            if (resource.isRecurse()) {
                profileCategoriesElement.appendChild(getProfileCategoryElement(document, pi));
            } else {
                profileCategoriesElement.appendChild(pi.getElement(document));
            }
        }
        childrenElement.appendChild(profileCategoriesElement);

        // get Sheet containing Profile Items
        Sheet sheet = getSheet(dataCategory);
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

    protected void addNewProfileItems(Document document, Element element) {
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

    //TODO - v1 builders should not need to implement atom feeds
    public org.apache.abdera.model.Element getAtomElement() {
        return AtomFeed.getInstance().newFeed();
    }

    private void setBuilder(ProfileItem pi) {
        pi.setBuilder(new ProfileItemBuilder(pi));
    }

    private Sheet getSheet(DataCategory dataCategory) {
        return profileService.getSheet(resource.getProfileBrowser(), dataCategory, sheetBuilder);
    }

    private Sheet getSheet() {
        return profileService.getSheet(resource.getProfileBrowser(), sheetBuilder);
    }

    public Map<String, Object> getTemplateValues() {
        Profile profile = resource.getProfile();
        DataCategory dataCategory = resource.getDataCategory();
        Sheet sheet = getSheet();
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
        BigDecimal totalAmountPerMonth = ProfileItem.ZERO;
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
