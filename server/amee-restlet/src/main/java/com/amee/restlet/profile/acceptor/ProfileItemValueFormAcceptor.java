package com.amee.restlet.profile.acceptor;

import com.amee.calculation.service.CalculationService;
import com.amee.domain.AMEEStatistics;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.item.NumberValue;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.restlet.profile.ProfileItemValueResource;
import com.amee.service.item.ProfileItemServiceImpl;
import com.amee.service.profile.ProfileService;
import org.restlet.data.Form;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileItemValueFormAcceptor implements IItemValueFormAcceptor {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileItemServiceImpl profileItemService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private AMEEStatistics ameeStatistics;

    public BaseItemValue accept(ProfileItemValueResource resource, Form form) {

        // Clients can explicitly specify the return representation in API > 1.0. The default behaviour
        // for POSTS and PUTS is not to return a representation.
        resource.setRepresentationRequested(form.getFirstValue("representation", "none"));

        BaseItemValue profileItemValue = resource.getProfileItemValue();
        ProfileItem profileItem = resource.getProfileItem();

        if (form.getFirstValue("value") != null) {
            profileItemValue.setValue(form.getFirstValue("value"));
        }
        if (form.getFirstValue("unit") != null && NumberValue.class.isAssignableFrom(profileItemValue.getClass())) {
            ((NumberValue)profileItemValue).setUnit(form.getFirstValue("unit"));
        }
        if (form.getFirstValue("perUnit") != null && NumberValue.class.isAssignableFrom(profileItemValue.getClass())) {
            ((NumberValue)profileItemValue).setPerUnit(form.getFirstValue("perUnit"));
        }

        // calculate, update statistics and clear caches
        profileItemService.clearItemValues();
        calculationService.calculate(profileItem);
        ameeStatistics.updateProfileItemValue();
        profileService.clearCaches(resource.getProfile());

        return profileItemValue;
    }
}