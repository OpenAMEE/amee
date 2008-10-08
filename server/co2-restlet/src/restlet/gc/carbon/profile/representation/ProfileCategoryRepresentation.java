package gc.carbon.profile.representation;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Method;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import gc.carbon.profile.Profile;
import gc.carbon.profile.ProfileItem;
import gc.carbon.profile.ProfileCategoryResource;
import gc.carbon.profile.BaseProfileResource;
import gc.carbon.data.DataCategory;
import gc.carbon.path.PathItem;
import gc.carbon.IRepresentationStrategy;
import gc.carbon.EngineUtils;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.Pager;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.domain.APIUtils;

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
public class ProfileCategoryRepresentation implements IRepresentationStrategy {


    public JSONObject getJSONObject(BaseProfileResource resource)  throws JSONException {

        JSONObject obj = new JSONObject();

        Profile profile = resource.getProfileBrowser().getProfile();
        DataCategory dataCategory = resource.getProfileBrowser().getDataCategory();

        // add objects
        obj.put("path", resource.getPathItem().getFullPath());
        obj.put("profileDate", EngineUtils.getMonthlyDate(resource.getProfileBrowser().getProfileDate()));

        // add relevant Profile info depending on whether we are at root
        if (resource.getPathItem().getParent() == null) {
            obj.put("profile", profile.getJSONObject());
        } else {
            obj.put("profile", profile.getIdentityJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", dataCategory.getIdentityJSONObject());

        if (resource.isGet()) {

            // create children JSON
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (PathItem pi : resource.getPathItem().getChildrenByType("DC")) {
                dataCategories.put(pi.getJSONObject());
            }
            children.put("dataCategories", dataCategories);

            // add Sheet containing Profile Items & totalAmountPerMonth
            Sheet sheet = resource.getProfileSheetService().getSheet(profile, dataCategory, resource.getProfileBrowser().getProfileDate());
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                children.put("profileItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
                obj.put("totalAmountPerMonth", resource.getProfileSheetService().getTotalAmountPerMonth(sheet));
            } else {
                children.put("profileItems", new JSONObject());
                children.put("pager", new JSONObject());
                obj.put("totalAmountPerMonth", "0");
            }

            // add chilren
            obj.put("children", children);

        } else if (resource.getRequest().getMethod().equals(Method.POST) || resource.getRequest().getMethod().equals(Method.PUT)) {
            if (resource.getProfileItem() != null) {
                obj.put("profileItem", resource.getProfileItem().getJSONObject());
            } else if (resource.getProfileItems() != null) {
                JSONArray profileItems = new JSONArray();
                obj.put("profileItems", profileItems);
                for (ProfileItem pi : resource.getProfileItems()) {
                    profileItems.put(pi.getJSONObject(false));
                }
            }
        }
        return obj;

    }

    public Element getElement(BaseProfileResource resource, Document document) {
        Profile profile = resource.getProfileBrowser().getProfile();
        DataCategory dataCategory = resource.getProfileBrowser().getDataCategory();

        // create element
        org.w3c.dom.Element element = document.createElement("ProfileCategoryResource");

        // add objects
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));
        // add profile date
        element.appendChild(APIUtils.getElement(document, "ProfileDate",
                EngineUtils.getMonthlyDate(resource.getProfileBrowser().getProfileDate())));

        // add relevant Profile info depending on whether we are at root
        if (resource.getPathItem().getParent() == null) {
            element.appendChild(profile.getElement(document));
        } else {
            element.appendChild(profile.getIdentityElement(document));
        }

        // add DataCategory and Profile elements
        element.appendChild(dataCategory.getIdentityElement(document));

        if (resource.isGet()) {

            // list child Profile Categories and child Profile Items
            org.w3c.dom.Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Profile Categories via pathItem
            org.w3c.dom.Element dataCategoriesElement = document.createElement("ProfileCategories");
            for (PathItem pi : resource.getPathItem().getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(pi.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);

            // get Sheet containing Profile Items
            Sheet sheet = resource.getProfileSheetService().getSheet(profile, dataCategory, resource.getProfileBrowser().getProfileDate());
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                // list child Profile Items via sheet
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
                // add CO2 amount
                element.appendChild(APIUtils.getElement(document, "TotalAmountPerMonth",
                        resource.getProfileSheetService().getTotalAmountPerMonth(sheet).toString()));
            }

        } else if (resource.getRequest().getMethod().equals(Method.POST) || resource.getRequest().getMethod().equals(Method.PUT)) {
            if (resource.getProfileItem() != null) {
                element.appendChild(resource.getProfileItem().getElement(document, false));
            } else if (resource.getProfileItems() != null) {
                org.w3c.dom.Element profileItemsElement = document.createElement("ProfileItems");
                element.appendChild(profileItemsElement);
                for (ProfileItem pi : resource.getProfileItems()) {
                    profileItemsElement.appendChild(pi.getElement(document, false));
                }
            }
        }

        return element;
    }

}
