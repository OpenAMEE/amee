package com.amee.restlet.profile.acceptor;

import com.amee.calculation.service.CalculationService;
import com.amee.domain.AMEEStatistics;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.item.NumberValue;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.domain.profile.MonthDate;
import com.amee.platform.science.StartEndDate;
import com.amee.restlet.profile.ProfileItemResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.item.ProfileItemServiceImpl;
import com.amee.service.profile.ProfileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProfileItemFormAcceptor implements IProfileItemFormAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileItemServiceImpl profileItemService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private AMEEStatistics ameeStatistics;

    public List<ProfileItem> accept(ProfileItemResource resource, Form form) {

        // Clients can explicitly specify the return representation in API > 1.0. The default behaviour
        // for POSTS and PUTS is not to return a representation.
        resource.setRepresentationRequested(form.getFirstValue("representation", "none"));

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        ProfileItem profileItem;

        // Obtain the ProfileItem we'll attempt to update.
        profileItem = resource.getProfileItem();

        // Ensure updated ProfileItem does not break rules for ProfileItems.
        updateProfileItem(resource, profileItem, form);

        // Ensure endDate is not before startDate
        if ((profileItem.getEndDate() != null) && profileItem.getEndDate().before(profileItem.getStartDate())) {
            resource.badRequest(APIFault.INVALID_DATE_RANGE);
            return profileItems;
        }

        // ProfileItem must be unique with the Profile.
        if (profileItemService.isUnique(profileItem)) {

            // Update ItemValues if supplied
            for (String name : form.getNames()) {
                BaseItemValue itemValue = profileItemService.getItemValue(profileItem, name);
                if (itemValue != null) {
                    itemValue.setValue(form.getFirstValue(name));
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
            log.debug("storeRepresentation() - ProfileItem updated");

            // All done. Recalculate, store, update statistics count and clear caches.
            profileItemService.clearItemValues();
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
        if (!StringUtils.isBlank(form.getFirstValue("startDate"))) {
            profileItem.setStartDate(new StartEndDate(form.getFirstValue("startDate")));
        }

        // Update 'validFrom' value
        if (!StringUtils.isBlank(form.getFirstValue("validFrom"))) {
            profileItem.setStartDate(new MonthDate(form.getFirstValue("validFrom")));
        }

        // Update 'end' value
        if (!StringUtils.isBlank(form.getFirstValue("end"))) {
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
            if (StringUtils.isNotBlank(form.getFirstValue("duration"))) {
                StartEndDate endDate = profileItem.getStartDate().plus(form.getFirstValue("duration"));
                profileItem.setEndDate(endDate);
            }
        }
    }
}
