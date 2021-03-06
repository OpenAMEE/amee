package com.amee.restlet.profile;

import com.amee.domain.profile.MonthDate;
import com.amee.domain.profile.Profile;
import com.amee.platform.science.StartEndDate;
import com.amee.restlet.AMEEResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataService;
import com.amee.service.profile.ProfileBrowser;
import com.amee.service.profile.ProfileService;
import org.joda.time.format.ISOPeriodFormat;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public abstract class BaseProfileResource extends AMEEResource {

    @Autowired
    protected ProfileService profileService;

    protected ProfileBrowser profileBrowser;
    protected Set<Long> profileDataCategoryIds;

    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        this.profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (getProfile() != null);
    }

    public ProfileBrowser getProfileBrowser() {
        return profileBrowser;
    }

    public Profile getProfile() {
        return (Profile) getRequest().getAttributes().get("profile");
    }

    public Set<Long> getProfileDataCategoryIds() {
        if (profileDataCategoryIds == null) {
            profileDataCategoryIds = profileService.getProfileDataCategoryIds(getProfile());
        }
        return profileDataCategoryIds;
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public DataService getDataService() {
        return dataService;
    }

    public boolean validateParameters() {
        APIFault apiFault = getValidationAPIFault();
        if (apiFault.equals(APIFault.NONE)) {
            return true;
        } else {
            badRequest(apiFault);
            return false;
        }
    }

    //TODO - Move to filter - validation is not general to all Profile Resources

    public APIFault getValidationAPIFault() {
        if (getAPIVersion().isVersionOne()) {
            if (containsCalendarParams()) {
                return APIFault.INVALID_API_PARAMETERS;
            }
            if (!validMonthDateTimeFormat()) {
                return APIFault.INVALID_DATE_FORMAT;
            }
        } else {
            if (!validISODateTimeFormats()) {
                return APIFault.INVALID_DATE_FORMAT;
            }
            if (isGet()) {
                if (containsProfileDate()) {
                    return APIFault.INVALID_API_PARAMETERS;
                }
                if (proRateModeHasNoEndDate()) {
                    return APIFault.INVALID_PRORATA_REQUEST;
                }
            } else {
                if (containsValidFromOrEnd()) {
                    return APIFault.INVALID_API_PARAMETERS;
                }
                if (containsPerUnitNoneAndNoDuraton()) {
                    return APIFault.INVALID_API_PARAMETERS;
                }
            }
        }
        return APIFault.NONE;
    }

    private boolean validMonthDateTimeFormat() {
        String profileDate = getForm().getFirstValue("profileDate");
        if (profileDate != null && !MonthDate.validate(profileDate)) {
            return false;
        }

        String validFromDate = getForm().getFirstValue("validFrom");
        if (validFromDate != null && !MonthDate.validate(validFromDate)) {
            return false;
        }
        return true;
    }

    private boolean validISODateTimeFormats() {
        String startDate = getForm().getFirstValue("startDate");
        if (startDate != null && !StartEndDate.validate(startDate)) {
            return false;
        }

        String endDate = getForm().getFirstValue("endDate");
        if (endDate != null && !StartEndDate.validate(endDate)) {
            return false;
        }

        String duration = getForm().getFirstValue("duration");
        if (duration != null) {
            try {
                ISOPeriodFormat.standard().parsePeriod(duration);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        return true;
    }

    private boolean proRateModeHasNoEndDate() {
        return getForm().getFirstValue("mode", "null").equals("prorata")
                && !getForm().getNames().contains("endDate");
    }

    private boolean containsCalendarParams() {
        return getForm().getNames().contains("endDate") ||
                getForm().getNames().contains("startDate") ||
                getForm().getNames().contains("duration");
    }

    private boolean containsProfileDate() {
        return getForm().getNames().contains("profileDate");
    }

    private boolean containsValidFromOrEnd() {
        return getForm().getNames().contains("validFrom") ||
                getForm().getNames().contains("end");
    }

    private boolean containsPerUnitNoneAndNoDuraton() {
        //TODO
        return false;
    }
}
