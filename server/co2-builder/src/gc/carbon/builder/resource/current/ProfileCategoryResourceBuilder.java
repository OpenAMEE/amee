package gc.carbon.builder.resource.current;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.Pager;
import com.jellymold.sheet.Sheet;
import gc.carbon.builder.domain.current.ProfileItemBuilder;
import gc.carbon.builder.domain.BuildableProfileItem;
import gc.carbon.builder.resource.ResourceBuilder;
import gc.carbon.builder.resource.BuildableCategoryResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    BuildableCategoryResource resource;

    public ProfileCategoryResourceBuilder(BuildableCategoryResource resource) {
        this.resource = resource;
    }

    public JSONObject getJSONObject()  throws JSONException {

        JSONObject obj = new JSONObject();

        // TODO - Move up to sibling
        // Add APIVersion
        obj.put("apiVersion", resource.getVersion().getJSONObject());

        // add objects
        obj.put("path", resource.getFullPath());
        obj.put("startDate", resource.getStartDate());
        if (resource.getEndDate() != null) {
            obj.put("endDate", resource.getEndDate());
        } else {
            obj.put("endDate", "");
        }
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
            Sheet sheet = resource.getSheet();
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                children.put("profileItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
                obj.put("totalAmount", resource.getTotalAmount(sheet));
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

        // TODO - Move up to sibling
        // Add APIVersion
        element.appendChild(resource.getVersion().getElement(document));

        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));

        // add profile date
        //element.appendChild(resource.getDateTimeBrowser().getProfileDate().toXML(document));
                // add profile date
        element.appendChild(APIUtils.getElement(document, "StartDate",resource.getStartDate().toString()));
        if (resource.getEndDate() != null) {
            element.appendChild(APIUtils.getElement(document, "EndDate",resource.getEndDate().toString()));
        } else {
            element.appendChild(APIUtils.getElement(document, "EndDate",""));
        }

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
            for (APIObject pi : resource.getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(pi.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);

            // get Sheet containing Profile Items
            Sheet sheet = resource.getSheet();
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());

                // list child Profile Items via sheet
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));

                // add CO2 amount
                element.appendChild(APIUtils.getElement(document, "TotalAmount",
                        resource.getTotalAmount(sheet).toString()));
            }

        } else if (resource.isPost() || resource.isPut()) {

            if (!resource.getProfileItems().isEmpty()) {
                if (resource.getProfileItems().size() == 1) {
                    BuildableProfileItem pi = resource.getProfileItems().get(0);
                    pi.setBuilder(new ProfileItemBuilder(pi));
                    element.appendChild(pi.getElement(document, false));
                } else {
                    Element profileItemsElement = document.createElement("ProfileItems");
                    element.appendChild(profileItemsElement);
                    for (BuildableProfileItem pi : resource.getProfileItems()) {
                        pi.setBuilder(new ProfileItemBuilder(pi));
                        profileItemsElement.appendChild(pi.getElement(document, false));
                    }
                }
            }
        }

        return element;
    }

}