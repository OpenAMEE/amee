package gc.carbon.profile.renderer;

import gc.carbon.profile.ProfileCategoryResource;
import gc.carbon.profile.Profile;
import gc.carbon.profile.ProfileItem;
import gc.carbon.data.DataCategory;
import gc.carbon.path.PathItem;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import org.restlet.data.Method;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import com.jellymold.utils.domain.APIUtils;

import java.util.List;
import java.util.Date;

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
public class ProfileCategoryCalendarRenderer extends Renderer {

    public ProfileCategoryCalendarRenderer(ProfileCategoryResource resource) {
        super(resource);
    }

    public JSONObject getJSONObject()  throws JSONException {

        JSONObject obj = new JSONObject();

        Profile profile = resource.getProfileBrowser().getProfile();
        DataCategory dataCategory = resource.getProfileBrowser().getDataCategory();

        // add objects
        obj.put("path", resource.getPathItem().getFullPath());
        obj.put(resource.getDateTimeBrowser().getStartDate().getName(), resource.getDateTimeBrowser().getStartDate().toString());

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

            List<ProfileItem> items = getProfileItems(profile, dataCategory);
            // TODO - Add Paging
            if (!items.isEmpty()) {
                for (ProfileItem pi : items) {
                    children.put("profileItems", pi.getJSONObject());
                }
                obj.put("totalAmount", "XXXXXXXXXXXXXXXXXXXXX");
            } else {
                children.put("profileItems", new JSONObject());
                children.put("pager", new JSONObject());
                obj.put("totalAmount", "0");
            }

            // add chilren
            obj.put("children", children);

        } else if (resource.getRequest().getMethod().equals(Method.POST) || resource.getRequest().getMethod().equals(Method.PUT)) {

            if (!resource.getProfileItems().isEmpty()) {
                if (resource.getProfileItems().size() == 1) {
                    obj.put("profileItem", resource.getProfileItems().get(0).getJSONObject());
                } else {
                    JSONArray profileItems = new JSONArray();
                    obj.put("profileItems", profileItems);
                    for (ProfileItem pi : resource.getProfileItems()) {
                        profileItems.put(pi.getJSONObject(false));
                    }
                }
            }
        }

        return obj;
    }

    public Element getElement(Document document) {


        Profile profile = resource.getProfileBrowser().getProfile();
        DataCategory dataCategory = resource.getProfileBrowser().getDataCategory();

        // create element
        org.w3c.dom.Element element = document.createElement("ProfileCategoryResource");

        // add objects
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));

        // add profile date
        element.appendChild(resource.getDateTimeBrowser().getStartDate().toXML(document));

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

            List<ProfileItem> items = getProfileItems(profile, dataCategory);
            // TODO - Add Paging
            if (!items.isEmpty()) {
                for (ProfileItem pi : items) {
                    childrenElement.appendChild(pi.getElement(document));
                }
            }

            // add CO2 amount
            element.appendChild(APIUtils.getElement(document, "AmountPerMonth","XXXXXXXXXXXXXXXX"));

        } else if (resource.getRequest().getMethod().equals(Method.POST) || resource.getRequest().getMethod().equals(Method.PUT)) {

            if (!resource.getProfileItems().isEmpty()) {
                if (resource.getProfileItems().size() == 1) {
                    element.appendChild(resource.getProfileItems().get(0).getElement(document, false));
                } else {
                    org.w3c.dom.Element profileItemsElement = document.createElement("ProfileItems");
                    element.appendChild(profileItemsElement);
                    for (ProfileItem pi : resource.getProfileItems()) {
                        profileItemsElement.appendChild(pi.getElement(document, false));
                    }
                }
            }
        }

        return element;
    }

    //TODO - Refactor optional date params to Null Object pattern.
    //TODO - To implement
    private List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory) {
        Date startDate = resource.getDateTimeBrowser().getStartDate().toDate();
        Date endDate = (resource.getDateTimeBrowser().getEndDate() != null) ? resource.getDateTimeBrowser().getEndDate().toDate() : null;
        return null;
    }
}

