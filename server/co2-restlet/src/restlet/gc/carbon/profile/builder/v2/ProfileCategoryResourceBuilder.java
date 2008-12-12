package gc.carbon.profile.builder.v2;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.Pager;
import com.jellymold.utils.cache.CacheableFactory;
import com.jellymold.sheet.Sheet;
import com.jellymold.sheet.Row;
import gc.carbon.domain.profile.builder.BuildableProfileItem;
import gc.carbon.domain.profile.builder.v2.ProfileItemBuilder;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.data.builder.BuildableCategoryResource;
import gc.carbon.data.builder.ResourceBuilder;
import gc.carbon.profile.ProfileService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

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

public class ProfileCategoryResourceBuilder implements ResourceBuilder {

    private final Log log = LogFactory.getLog(getClass());

    private CacheableFactory sheetBuilder;

    private ProfileService profileService;

    BuildableCategoryResource resource;

    public ProfileCategoryResourceBuilder(BuildableCategoryResource resource) {
        this.resource = resource;
        this.profileService = resource.getProfileService();
        this.sheetBuilder = new ProfileSheetBuilder(profileService);
    }

    public JSONObject getJSONObject()  throws JSONException {

        JSONObject obj = new JSONObject();

        // TODO - Move up to sibling
        // Add APIVersion
        obj.put("apiVersion", resource.getVersion().getJSONObject());

        // add objects
        obj.put("path", resource.getFullPath());

        obj.put("startDate", resource.getStartDate());
        obj.put("endDate", (resource.getEndDate() != null) ? resource.getEndDate().toString() : "");

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            obj.put("profile", resource.getProfile().getIdentityJSONObject());
        } else {
            obj.put("profile", resource.getProfile().getJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", resource.getDataCategory().getIdentityJSONObject());

        if (resource.isGet()) {

            // create children JSON
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (APIObject pi : resource.getChildrenByType("DC")) {
                dataCategories.put(pi.getJSONObject());
            }
            children.put("dataCategories", dataCategories);

            // add Sheet containing Profile Items & totalAmountPerMonth
            Sheet sheet = getSheet();
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                children.put("profileItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
                obj.put("totalAmount", getTotalAmount(sheet));
            } else {
                children.put("profileItems", new JSONObject());
                children.put("pager", new JSONObject());
                obj.put("totalAmount", "0");
            }

            // add chilren
            obj.put("children", children);

        } else if (resource.isPost() || resource.isPut()) {

            if (!resource.getProfileItems().isEmpty()) {
                if (resource.getProfileItems().size() == 1) {
                    BuildableProfileItem pi = resource.getProfileItems().get(0);
                    pi.setBuilder(new ProfileItemBuilder(pi));
                    obj.put("profileItem", resource.getProfileItems().get(0).getJSONObject());
                } else {
                    JSONArray profileItems = new JSONArray();
                    obj.put("profileItems", profileItems);
                    for (BuildableProfileItem pi : resource.getProfileItems()) {
                        pi.setBuilder(new ProfileItemBuilder(pi));
                        profileItems.put(pi.getJSONObject(false));
                    }
                }
            }
        }

        return obj;
    }

    public Element getElement(Document document) {

        // create element
        Element element = document.createElement("ProfileCategoryResource");

        // Add APIVersion
        element.appendChild(resource.getVersion().getElement(document));

        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));

        element.appendChild(APIUtils.getElement(document, "StartDate",resource.getStartDate().toString()));
        element.appendChild(APIUtils.getElement(document, "EndDate", (resource.getEndDate() != null) ? resource.getEndDate().toString() : ""));

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            element.appendChild(resource.getProfile().getIdentityElement(document));
        } else {
            element.appendChild(resource.getProfile().getElement(document));
        }

        // add DataCategory and Profile elements
        element.appendChild(resource.getDataCategory().getIdentityElement(document));

        if (resource.isGet()) {

            // list child Profile Categories and child Profile Items
            Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Profile Categories via pathItem
            Element dataCategoriesElement = document.createElement("ProfileCategories");
            for (APIObject dc : resource.getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(dc.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);

            // get Sheet containing Profile Items
            Sheet sheet = getSheet();
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());

                // list child Profile Items via sheet
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));

                // add CO2 amount
                element.appendChild(APIUtils.getElement(document, "TotalAmount", getTotalAmount(sheet).toString()));
            }

        } else if (resource.isPost() || resource.isPut()) {

            if (!resource.getProfileItems().isEmpty()) {
                Element profileItemsElement = document.createElement("ProfileItems");
                element.appendChild(profileItemsElement);
                for (BuildableProfileItem pi : resource.getProfileItems()) {
                    pi.setBuilder(new ProfileItemBuilder(pi));
                    profileItemsElement.appendChild(pi.getElement(document, false));
                }
            }
        }

        return element;
    }

    private Sheet getSheet() {
        return profileService.getSheet(resource.getProfileBrowser(), sheetBuilder);
    }

    public Map<String, Object> getTemplateValues() {
        APIObject profile = resource.getProfile();
        APIObject dataCategory = resource.getDataCategory();
        Sheet sheet = getSheet();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profile);
        values.put("dataCategory", dataCategory);
        values.put("node", dataCategory);
        values.put("sheet", sheet);
        if (sheet != null) {
            values.put("totalAmount", getTotalAmount(sheet));
        }
        return values;
    }

    private BigDecimal getTotalAmount(Sheet sheet) {
        BigDecimal totalAmount = ProfileItem.ZERO;
        BigDecimal amount;
        for (Row row : sheet.getRows()) {
            try {
                amount = row.findCell("amount").getValueAsBigDecimal();
                amount = amount.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                if (amount.precision() > ProfileItem.PRECISION) {
                    log.warn("getTotalAmount() - precision is too big: " + amount);
                    // TODO: do something?
                }
            } catch (Exception e) {
                // swallow
                log.warn("getTotalAmount() - caught Exception: " + e);
                amount = ProfileItem.ZERO;
            }
            totalAmount = totalAmount.add(amount);
        }
        return totalAmount;
    }
}