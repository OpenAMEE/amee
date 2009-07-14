package com.amee.restlet.profile;

import com.amee.domain.Pager;
import com.amee.domain.StartEndDate;
import com.amee.domain.path.PathItem;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileDate;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.profile.ValidFromDate;
import com.amee.restlet.AMEEResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataService;
import com.amee.service.profile.ProfileBrowser;
import com.amee.service.profile.ProfileService;
import org.joda.time.format.ISOPeriodFormat;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.util.Collection;
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
public abstract class BaseProfileResource extends AMEEResource {

    protected ProfileBrowser profileBrowser;
    protected ProfileItem profileItem;
    protected Collection<Long> profileDataCategoryIds;

    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        this.profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (getProfile() != null) &&
                getProfile().getEnvironment().equals(environment);
    }


    @Override
    public Pager getPager() {
        return getPager(getItemsPerPage());
    }

    public boolean hasParent() {
        return pathItem.getParent() != null;
    }

    public Set<PathItem> getChildrenByType(String type) {
        return pathItem.getChildrenByType(type);
    }

    protected boolean isGET() {
        return getRequest().getMethod().equals(Method.GET);
    }

    public ProfileBrowser getProfileBrowser() {
        return profileBrowser;
    }

    protected void setProfileItem(String profileItemUid) {
        if (profileItemUid.isEmpty()) return;
        this.profileItem = profileService.getProfileItem(profileItemUid);
    }

    public ProfileItem getProfileItem() {
        return profileItem;
    }

    public Profile getProfile() {
        return (Profile) getRequest().getAttributes().get("profile");
    }

    public Collection<Long> getProfileDataCategoryIds() {
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

    public String getFullPath() {
        if ((getProfile() != null) && (pathItem != null)) {
            return "/profiles/" + getProfile().getDisplayPath() + pathItem.getFullPath();
        } else {
            return "/profiles";
        }
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
            if (isGET()) {
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
        if (profileDate != null && !ProfileDate.validate(profileDate)) {
            return false;
        }

        String validFromDate = getForm().getFirstValue("validFrom");
        if (validFromDate != null && !ValidFromDate.validate(validFromDate)) {
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
