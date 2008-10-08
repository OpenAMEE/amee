package gc.carbon.profile.command;

import gc.carbon.profile.ProfileCategoryResource;
import gc.carbon.profile.ProfileItem;
import gc.carbon.data.DataCategory;
import gc.carbon.data.DataItem;
import gc.carbon.data.ItemValue;
import org.restlet.resource.Representation;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.jboss.seam.util.XML;
import org.dom4j.DocumentException;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
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
public class ProfileCategoryFormCommand extends ProfileCommand {

    private final static Logger log = Logger.getLogger(ProfileCategoryFormCommand.class);

    public ProfileCategoryFormCommand(ProfileCategoryResource resource) {
        super(resource);
    }

    public List<ProfileItem> accept(Representation entity, Form form) {
        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        DataCategory dataCategory;
        DataItem dataItem;
        ProfileItem profileItem = null;
        String uid;
        dataCategory = resource.getProfileBrowser().getDataCategory();
        if (resource.getRequest().getMethod().equals(Method.POST)) {
            // new ProfileItem
            uid = form.getFirstValue("dataItemUid");
            if (uid != null) {
                // the root DataCategory has an empty path
                if (dataCategory.getPath().length() == 0) {
                    // allow any DataItem for any DataCategory
                    dataItem = resource.getDataService().getDataItem(resource.getEnvironment(), uid);
                } else {
                    // only allow DataItems for specific DataCategory (not root)
                    dataItem = resource.getDataService().getDataItem(dataCategory, uid);
                }
                if (dataItem != null) {
                    // create new ProfileItem
                    profileItem = new ProfileItem(resource.getProfileBrowser().getProfile(), dataItem);
                    profileItem = acceptProfileItem(form, profileItem);
                } else {
                    log.warn("Data Item not found");
                    profileItem = null;
                }
            } else {
                log.warn("dataItemUid not supplied");
                profileItem = null;
            }
        } else if (resource.getRequest().getMethod().equals(Method.PUT)) {
            // update ProfileItem
            uid = form.getFirstValue("profileItemUid");
            if (uid != null) {
                // find existing Profile Item
                // the root DataCategory has an empty path
                if (dataCategory.getPath().length() == 0) {
                    // allow any ProfileItem for any DataCategory
                    profileItem = resource.getProfileService().getProfileItem(resource.getProfileBrowser().getProfile().getUid(), uid);
                } else {
                    // only allow ProfileItems for specific DataCategory (not root)
                    profileItem = resource.getProfileService().getProfileItem(resource.getProfileBrowser().getProfile().getUid(), dataCategory.getUid(), uid);
                }
                if (profileItem != null) {
                    // update existing Profile Item
                    profileItem = acceptProfileItem(form, profileItem);
                } else {
                    log.warn("Profile Item not found");
                    profileItem = null;
                }
            } else {
                log.warn("profileItemUid not supplied");
                profileItem = null;
            }
        }

        if (profileItem != null)
            profileItems.add(profileItem);

        return profileItems;
    }


    private ProfileItem acceptProfileItem(Form form, ProfileItem profileItem) {


        // alias deprecated params
        String startDate = form.getFirstValue("validFrom",form.getFirstValue("startDate"));

        // determine date for new ProfileItem
        profileItem.setStartDate(startDate);

        // determine name for new ProfileItem
        profileItem.setName(form.getFirstValue("name"));

        // determine if new ProfileItem is an end marker
        profileItem.setEnd(form.getFirstValue("end"));

        // see if ProfileItem already exists
        if (!resource.getProfileService().isEquivilentProfileItemExists(profileItem)) {
            // save newProfileItem and do calculations
            resource.getEntityManager().persist(profileItem);
            resource.getProfileService().checkProfileItem(profileItem);
            // update item values if supplied
            Map<String, ItemValue> itemValues = profileItem.getItemValuesMap();
            for (String name : form.getNames()) {
                ItemValue itemValue = itemValues.get(name);
                if (itemValue != null) {
                    itemValue.setValue(form.getFirstValue(name));
                }
            }
            resource.getCalculator().calculate(profileItem);
        } else {
            log.warn("Profile Item already exists");
            profileItem = null;
        }
        return profileItem;
    }
}