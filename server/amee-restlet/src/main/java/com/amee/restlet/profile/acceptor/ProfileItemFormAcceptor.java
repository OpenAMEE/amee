package com.amee.restlet.profile.acceptor;

import com.amee.calculation.service.CalculationService;
import com.amee.domain.AMEEStatistics;
import com.amee.domain.StartEndDate;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueMap;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.profile.ValidFromDate;
import com.amee.restlet.profile.ProfileItemResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.profile.ProfileService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
@Service
public class ProfileItemFormAcceptor implements IProfileItemFormAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private AMEEStatistics ameeStatistics;

    public List<ProfileItem> accept(ProfileItemResource resource, Form form) {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        ProfileItem profileItem;

        // Obtain the ProfileItem we'll attempt to update.
        profileItem = resource.getProfileItem();

        // Work on a temporary copy of the ProfileItem.
        ProfileItem profileItemCopy = profileItem.getCopy();

        // Ensure updated ProfileItem does not break rules for ProfileItems.
        updateProfileItem(resource, profileItemCopy, form);

        // Ensure endDate is not before startDate
        if (profileItemCopy.getEndDate() != null && profileItemCopy.getEndDate().before(profileItemCopy.getStartDate())) {
            resource.badRequest(APIFault.INVALID_DATE_RANGE);
            return profileItems;
        }

        // ProfileItem must be unique with the Profile.
        if (profileService.isUnique(profileItemCopy)) {

            // Update persistent ProfileItem
            updateProfileItem(resource, profileItem, form);

            // Update ItemValues if supplied
            ItemValueMap itemValues = profileItem.getItemValuesMap();
            for (String name : form.getNames()) {
                ItemValue itemValue = itemValues.get(name);
                if (itemValue != null) {
                    itemValue.setValue(form.getFirstValue(name));
                    if (resource.getAPIVersion().isNotVersionOne()) {
                        if (itemValue.hasUnit() && form.getNames().contains(name + "Unit")) {
                            itemValue.setUnit(form.getFirstValue(name + "Unit"));
                        }
                        if (itemValue.hasPerUnit() && form.getNames().contains(name + "PerUnit")) {
                            itemValue.setPerUnit(form.getFirstValue(name + "PerUnit"));
                        }
                    }
                }
            }
            log.debug("storeRepresentation() - ProfileItem updated");

            // All done. Recalculate, store, update statistics count and clear caches.
            calculationService.calculate(profileItem);
            profileItems.add(profileItem);
            ameeStatistics.updateProfileItem();
            profileService.clearCaches(resource.getProfile());

        } else {
            log.warn("storeRepresentation() - ProfileItem NOT updated");
            resource.badRequest(APIFault.DUPLICATE_ITEM);
        }

        return profileItems;
    }

    //TODO - parsing v1 and v2 params - see Acceptors which at least conditionally parse based on APIVersion. Ideal solution should be transparent.
    protected void updateProfileItem(ProfileItemResource resource, ProfileItem profileItem, Form form) {

        Set<String> names = form.getNames();

        if (!resource.validateParameters()) {
            return;
        }

        // Update 'name' value
        if (names.contains("name")) {
            profileItem.setName(form.getFirstValue("name"));
        }

        // Update 'startDate' value
        if (names.contains("startDate")) {
            profileItem.setStartDate(new StartEndDate(form.getFirstValue("startDate")));
        }

        // Update 'startDate' value
        if (names.contains("validFrom")) {
            profileItem.setStartDate(new ValidFromDate(form.getFirstValue("validFrom")));
        }

        // Update 'end' value
        if (names.contains("end")) {
            boolean end = Boolean.valueOf(form.getFirstValue("end"));
            if (end) {
                profileItem.setEndDate(profileItem.getStartDate());
            } else {
                profileItem.setEndDate(null);
            }
        }

        // Update 'endDate' value
        if (names.contains("endDate")) {
            if (StringUtils.isNotBlank(form.getFirstValue("endDate"))) {
                profileItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
            } else {
                profileItem.setEndDate(null);
            }
        } else {
            // Update 'duration' value
            if (form.getNames().contains("duration")) {
                StartEndDate endDate = profileItem.getStartDate().plus(form.getFirstValue("duration"));
                profileItem.setEndDate(endDate);
            }
        }
    }
}
