package com.amee.restlet.profile.acceptor;

import com.amee.calculation.service.CalculationService;
import com.amee.core.CO2AmountUnit;
import com.amee.domain.AMEEStatistics;
import com.amee.domain.StartEndDate;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueMap;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.profile.ValidFromDate;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.utils.APIException;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataService;
import com.amee.service.profile.ProfileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
public class ProfileCategoryFormAcceptor implements IProfileCategoryFormAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DataService dataService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private AMEEStatistics ameeStatistics;

    public List<ProfileItem> accept(ProfileCategoryResource resource, Form form) throws APIException {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        DataItem dataItem;
        ProfileItem profileItem = null;
        String uid;

        if (resource.getRequest().getMethod().equals(Method.POST)) {
            // new ProfileItem
            uid = form.getFirstValue("dataItemUid");
            if (uid != null) {
                dataItem = dataService.getDataItemByUid(resource.getEnvironment(), uid);
                if (dataItem != null) {
                    // create new ProfileItem
                    profileItem = new ProfileItem(resource.getProfile(), dataItem);
                    profileItem = acceptProfileItem(resource, form, profileItem);
                } else {
                    log.warn("accept() - Data Item not found");
                    throw new APIException(APIFault.ENTITY_NOT_FOUND);
                }
            } else {
                log.warn("accept() - dataItemUid not supplied");
                throw new APIException(APIFault.MISSING_PARAMETERS);
            }
        } else if (resource.getRequest().getMethod().equals(Method.PUT)) {
            // update ProfileItem
            uid = form.getFirstValue("profileItemUid");
            if (uid != null) {
                profileItem = profileService.getProfileItem(uid);
                if (profileItem != null) {
                    // update existing Profile Item
                    profileItem = acceptProfileItem(resource, form, profileItem);
                } else {
                    log.warn("accept() - Profile Item not found");
                    throw new APIException(APIFault.ENTITY_NOT_FOUND);
                }
            } else {
                log.warn("accept() - profileItemUid not supplied");
                throw new APIException(APIFault.MISSING_PARAMETERS);
            }
        }

        profileItems.add(profileItem);
        return profileItems;
    }

    private ProfileItem acceptProfileItem(ProfileCategoryResource resource, Form form, ProfileItem profileItem) throws APIException {

        // Validate request.
        APIFault apiFault = resource.getValidationAPIFault();
        if (!apiFault.equals(APIFault.NONE)) {
            throw new APIException(apiFault);
        }

        // TODO - Each APIVersion should have it's own Acceptor
        if (resource.getAPIVersion().isVersionOne()) {
            // Set the startDate and end marker.
            profileItem.setStartDate(new ValidFromDate(form.getFirstValue("validFrom")));
            boolean end = Boolean.valueOf(form.getFirstValue("end"));
            if (end) {
                profileItem.setEndDate(profileItem.getStartDate());
            }
        } else {

            // Clients can set units for the calculated CO2Amount in API > 1.0
            String unit = form.getFirstValue("returnUnit");
            String perUnit = form.getFirstValue("returnPerUnit");
            resource.getProfileBrowser().setCO2AmountUnit(new CO2AmountUnit(unit, perUnit));

            // Clients can explicitly specify the return representation in API > 1.0. The default behaviour
            // for POSTS and PUTS is not to return a representation.
            resource.setRepresentationRequested(form.getFirstValue("representation", "none"));

            // Set the startDate, endDate and duration.
            profileItem.setStartDate(new StartEndDate(form.getFirstValue("startDate")));
            if (form.getNames().contains("endDate") && form.getFirstValue("endDate") != null) {
                profileItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
            } else {
                if (form.getNames().contains("duration") && form.getFirstValue("duration") != null) {
                    StartEndDate endDate = profileItem.getStartDate().plus(form.getFirstValue("duration"));
                    profileItem.setEndDate(endDate);
                }
            }

            // If there is an endDate it must not be before the startDate.
            if ((profileItem.getEndDate() != null) && profileItem.getEndDate().before(profileItem.getStartDate())) {
                throw new APIException(APIFault.INVALID_DATE_RANGE);
            }
        }

        // determine name for new ProfileItem
        profileItem.setName(form.getFirstValue("name"));

        // see if equivalent ProfileItem already exists
        if (profileService.isUnique(profileItem)) {
            try {
                // save ProfileItem
                boolean isNewProfileItem = (profileItem.getId() == null);
                profileService.persist(profileItem);
                // update item values if supplied
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
                        ameeStatistics.updateProfileItemValue();
                    }
                }
                // do calculations
                calculationService.calculate(profileItem);
                // update statistics
                if (isNewProfileItem) {
                    ameeStatistics.createProfileItem();
                } else {
                    ameeStatistics.updateProfileItem();
                }
                // clear caches
                profileService.clearCaches(resource.getProfile());
            } catch (IllegalArgumentException ex) {
                log.warn("accept() - Bad parameter received", ex);
                throw new APIException(APIFault.INVALID_PARAMETERS);
            }
        } else {
            log.warn("accept() - Profile Item already exists");
            throw new APIException(APIFault.DUPLICATE_ITEM);
        }
        return profileItem;
    }
}
