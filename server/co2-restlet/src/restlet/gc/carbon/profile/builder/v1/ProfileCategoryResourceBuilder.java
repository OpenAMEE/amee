package gc.carbon.profile.builder.v1;

import com.jellymold.sheet.Cell;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.Pager;
import com.jellymold.utils.cache.CacheableFactory;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.data.builder.ResourceBuilder;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.builder.v1.ProfileItemBuilder;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.profile.ProfileService;
import gc.carbon.profile.ProfileCategoryResource;
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

    ProfileCategoryResource resource;

    public ProfileCategoryResourceBuilder(ProfileCategoryResource resource) {
        this.resource = resource;
        this.profileService = resource.getProfileService();
        this.sheetBuilder = new ProfileSheetBuilder(profileService);
    }

    public JSONObject getJSONObject()  throws JSONException {

        JSONObject obj = new JSONObject();

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

        if (resource.isGet()) {

            // create children JSON
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (PathItem pi : resource.getChildrenByType("DC")) {
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
                obj.put("totalAmountPerMonth", getTotalAmountPerMonth(sheet));
            } else {
                children.put("profileItems", new JSONObject());
                children.put("pager", new JSONObject());
                obj.put("totalAmountPerMonth", "0");
            }

            // add chilren
            obj.put("children", children);

        } else if (resource.isPost() || resource.isPut()) {

            if (!resource.getProfileItems().isEmpty()) {
                if (resource.getProfileItems().size() == 1) {
                    ProfileItem pi = resource.getProfileItems().get(0);
                    setBuilder(pi);
                    obj.put("profileItem", pi.getJSONObject());
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

        return obj;
    }

    public Element getElement(Document document) {

        // create element
        org.w3c.dom.Element element = document.createElement("ProfileCategoryResource");

        // add objects
        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));

        // add profile date
        element.appendChild(APIUtils.getElement(document, "ProfileDate",resource.getProfileDate().toString()));

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
            org.w3c.dom.Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Profile Categories via pathItem
            org.w3c.dom.Element dataCategoriesElement = document.createElement("ProfileCategories");
            for (PathItem pi : resource.getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(pi.getElement(document));
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
                element.appendChild(APIUtils.getElement(document, "TotalAmountPerMonth",
                        getTotalAmountPerMonth(sheet).toString()));
            }

        } else if (resource.isPost() || resource.isPut()) {

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

        return element;
    }

    //TODO - v1 builders should not need to implement atom feeds
    public org.apache.abdera.model.Element getAtomElement() {
        return AtomFeed.getInstance().newFeed();
    }

    private void setBuilder(ProfileItem pi) {
        if (resource.getProfileBrowser().returnAmountInExternalUnit()) {
            pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getAmountUnit()));
        } else {
            pi.setBuilder(new ProfileItemBuilder(pi));
        }
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
                try {
                    amountPerMonth = row.findCell("amountPerMonth").getValueAsBigDecimal();
                    amountPerMonth = amountPerMonth.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                    if (amountPerMonth.precision() > ProfileItem.PRECISION) {
                        log.warn("precision is too big: " + amountPerMonth);
                        // TODO: do something?
                    }
                } catch (Exception e) {
                    // swallow
                    log.warn("caught Exception: " + e);
                    amountPerMonth = ProfileItem.ZERO;
                }
                totalAmountPerMonth = totalAmountPerMonth.add(amountPerMonth);
            }
        }
        return totalAmountPerMonth;
    }
}
