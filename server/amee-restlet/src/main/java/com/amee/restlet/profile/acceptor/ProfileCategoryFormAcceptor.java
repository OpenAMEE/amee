package com.amee.restlet.profile.acceptor;

import com.amee.calculation.service.CalculationService;
import com.amee.domain.AMEEStatistics;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.auth.AccessSpecification;
import com.amee.domain.auth.AuthorizationContext;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.item.NumberValue;
import com.amee.domain.item.data.DataItem;
import com.amee.domain.profile.MonthDate;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.platform.science.CO2AmountUnit;
import com.amee.platform.science.StartEndDate;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.utils.APIException;
import com.amee.restlet.utils.APIFault;
import com.amee.service.auth.AuthorizationService;
import com.amee.service.data.DataService;
import com.amee.service.item.DataItemServiceImpl;
import com.amee.service.item.ProfileItemServiceImpl;
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
    private AuthorizationService authorizationService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileItemServiceImpl profileItemService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataItemServiceImpl dataItemService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private AMEEStatistics ameeStatistics;

    public List<ProfileItem> accept(ProfileCategoryResource resource, Form form) throws APIException {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        DataItem dataItem;
        ProfileItem profileItem = null;
        String uid;
        AuthorizationContext authorizationContext;

        if (resource.getRequest().getMethod().equals(Method.POST)) {
            // new ProfileItem
            uid = form.getFirstValue("dataItemUid");
            if (uid != null) {
                dataItem = dataItemService.getItemByUid(uid);
                if (dataItem != null) {
                    // Is authorized for the DataItem?
                    authorizationContext = getAuthorizationContext(resource, dataItem);
                    if (authorizationService.isAuthorized(authorizationContext)) {
                        // create new ProfileItem
                        profileItem = new ProfileItem(resource.getProfile(), dataItem);
                        profileItem = acceptProfileItem(resource, form, profileItem);
                    } else {
                        log.warn("accept() - Not authorized to access DataItem");
                        throw new APIException(APIFault.NOT_AUTHORIZED_FOR_INDIRECT_ACCESS);
                    }
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
                profileItem = profileItemService.getItemByUid(uid);
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

    private AuthorizationContext getAuthorizationContext(ProfileCategoryResource resource, DataItem dataItem) {
        AuthorizationContext authorizationContext = new AuthorizationContext();
        authorizationContext.addPrincipals(resource.getPrincipals());
        for (IAMEEEntityReference entity : dataItem.getHierarchy()) {
            authorizationContext.addAccessSpecification(new AccessSpecification(entity, PermissionEntry.VIEW));
        }
        return authorizationContext;
    }

    // Note, this can be called by both POSTs and PUTs

    private ProfileItem acceptProfileItem(ProfileCategoryResource resource, Form form, ProfileItem profileItem) throws APIException {

        // Validate request.
        APIFault apiFault = resource.getValidationAPIFault();
        if (!apiFault.equals(APIFault.NONE)) {
            throw new APIException(apiFault);
        }

        boolean isNewProfileItem = (profileItem.getId() == null);

        // TODO - Each APIVersion should have it's own Acceptor
        if (resource.getAPIVersion().isVersionOne()) {
            // Set the startDate and end marker.
            profileItem.setStartDate(new MonthDate(form.getFirstValue("validFrom")));
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

            // Set the startDate & endDate, or endDate via duration.
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
        if (profileItemService.isUnique(profileItem)) {
            try {
                // save ProfileItem
                profileItemService.persist(profileItem);
                profileItemService.clearItemValues();
                // update item values if supplied
                for (String name : form.getNames()) {
                    // Find the matching active ItemValue for the prevailing datetime context.
                    BaseItemValue itemValue = profileItemService.getItemValue(profileItem, name);
                    if (itemValue != null) {
                        itemValue.setValue(form.getFirstValue(name));
                        log.debug("acceptProfileItem() - set itemValue " + itemValue.getPath() +
                                " :  " + itemValue.getValueAsString());
                        if (resource.getAPIVersion().isNotVersionOne()) {
                            if ((NumberValue.class.isAssignableFrom(itemValue.getClass()) && ((NumberValue)itemValue).hasUnit() && form.getNames().contains(name + "Unit"))) {
                                ((NumberValue)itemValue).setUnit(form.getFirstValue(name + "Unit"));
                            }
                            if ((NumberValue.class.isAssignableFrom(itemValue.getClass()) && ((NumberValue)itemValue).hasPerUnit() && form.getNames().contains(name + "PerUnit"))) {
                                ((NumberValue)itemValue).setPerUnit(form.getFirstValue(name + "PerUnit"));
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
            } catch (IllegalArgumentException e) {
                log.warn("accept() - Bad parameter received - " + e.getMessage());
                throw new APIException(APIFault.INVALID_PARAMETERS);
            }
        } else {
            log.warn("accept() - Profile Item already exists");
            throw new APIException(APIFault.DUPLICATE_ITEM);
        }
        return profileItem;
    }
}
