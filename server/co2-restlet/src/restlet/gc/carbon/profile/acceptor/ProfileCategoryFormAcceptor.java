package gc.carbon.profile.acceptor;

import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.profile.ValidFromDate;
import gc.carbon.profile.ProfileCategoryResource;
import gc.carbon.profile.ProfileForm;
import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.resource.Representation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class ProfileCategoryFormAcceptor extends Acceptor {

    private final static Logger log = Logger.getLogger(ProfileCategoryFormAcceptor.class);

    public ProfileCategoryFormAcceptor(ProfileCategoryResource resource) {
        super(resource);
    }

    public List<ProfileItem> accept(Representation entity, ProfileForm form) {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        ProfileItem profileItem = null;

        if (resource.getRequest().getMethod().equals(Method.POST)) {
            profileItem = doPost(form);

        } else if (resource.getRequest().getMethod().equals(Method.PUT)) {
            profileItem = doPut(form);

        }

        if (profileItem != null)
            profileItems.add(profileItem);

        return profileItems;
    }

    private ProfileItem doPut(ProfileForm form) {
        ProfileItem profileItem;
        String uid = form.getFirstValue("profileItemUid");
        if (uid != null) {
            // find existing Profile Item
            // the root DataCategory has an empty path
            if (resource.getDataCategory().getPath().length() == 0) {
                // allow any ProfileItem for any DataCategory
                profileItem = resource.getProfileService().getProfileItem(resource.getProfileBrowser().getProfile().getUid(), uid);
            } else {
                // only allow ProfileItems for specific DataCategory (not root)
                profileItem = resource.getProfileService().getProfileItem(resource.getProfileBrowser().getProfile().getUid(), resource.getDataCategory().getUid(), uid);
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
        return profileItem;
    }

    private ProfileItem doPost(ProfileForm form) {
        ProfileItem profileItem;
        DataItem dataItem = getDataItem(form);
        if (dataItem != null) {
            // create new ProfileItem
            profileItem = new ProfileItem(resource.getProfile(), dataItem);
            profileItem = acceptProfileItem(form, profileItem);
        } else {
            log.warn("Data Item not found");
            profileItem = null;
        }
        return profileItem;
    }


    private ProfileItem acceptProfileItem(ProfileForm form, ProfileItem profileItem) {

        // validate passed parameters
        if (!resource.isValidRequest()) {
            resource.badRequest();
            return null;
        }

        // determine name for new ProfileItem
        profileItem.setName(form.getFirstValue("name"));

        if (form.isVersionOne()) {

            // determine startdate for new ProfileItem
            ValidFromDate startDate = new ValidFromDate(form.getFirstValue("validFrom"));
            profileItem.setStartDate(startDate);

            // determine if the new ProfileItem is an end marker
            profileItem.setEnd(Boolean.valueOf(form.getFirstValue("end")));

        } else {

            // determine startdate for new ProfileItem
            StartEndDate startDate = new StartEndDate(form.getFirstValue("startDate"));
            profileItem.setStartDate(startDate);


            if (form.getNames().contains("endDate"))
                profileItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));

            if (form.getNames().contains("duration")) {
                StartEndDate endDate = startDate.plus(form.getFirstValue("duration"));
                profileItem.setEndDate(endDate);
            }

            if (profileItem.getEndDate() != null && profileItem.getEndDate().before(profileItem.getStartDate())) {
                resource.badRequest();
                return null;
            }

        }

        // see if ProfileItem already exists
        if (resource.getProfileService().isEquivilentProfileItemExists(profileItem)) {
            log.warn("Profile Item already exists");
            return null;
        }

        try {
            reify(form, profileItem);
        } catch (IllegalArgumentException ex) {
            log.warn("Bad parameter received");
            resource.getEntityManager().remove(profileItem);
            return null;
        }

        return profileItem;
    }

    private void reify(ProfileForm form, ProfileItem profileItem) throws IllegalArgumentException {

        // save newProfileItem and do calculations
        resource.getEntityManager().persist(profileItem);
        resource.getProfileService().checkProfileItem(profileItem);

        // update item values if supplied
        Map<String, ItemValue> itemValues = profileItem.getItemValuesMap();
        for (String name : form.getNames()) {
            ItemValue itemValue = itemValues.get(name);
            if (itemValue != null) {
                itemValue.setValue(form.getFirstValue(name));
                if (itemValue.hasUnits())
                    itemValue.setUnit(form.getFirstValue(name + "Unit"));
                if (itemValue.hasPerUnits())
                itemValue.setPerUnit(form.getFirstValue(name + "PerUnit"));
            }
        }

        resource.getCalculator().calculate(profileItem);
    }


    private DataItem getDataItem(ProfileForm form) {
        DataItem dataItem = null;
        String uid = form.getFirstValue("dataItemUid");
        if (uid != null) {

            // the root DataCategory has an empty path
            if (resource.getDataCategory().getPath().length() == 0) {
                // allow any DataItem for any DataCategory
                dataItem = resource.getDataService().getDataItem(resource.getEnvironment(), uid);
            } else {
                // only allow DataItems for specific DataCategory (not root)
                dataItem = resource.getDataService().getDataItem(resource.getDataCategory(), uid);
            }
        } else {
            log.warn("dataItemUid not supplied");
        }
        return dataItem;
    }
}