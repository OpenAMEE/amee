package gc.carbon.profile;

import com.jellymold.utils.APIFault;
import com.jellymold.utils.Pager;
import gc.carbon.AMEEResource;
import gc.carbon.data.DataService;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.profile.ProfileItem;
import org.joda.time.format.ISOPeriodFormat;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.stereotype.Component;

import java.util.Date;
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
@Component("baseProfileResource")
public abstract class BaseProfileResource extends AMEEResource {

    protected ProfileBrowser profileBrowser;
    protected ProfileItem profileItem;

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        this.profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");
    }

    public Pager getPager() {
        return getPager(getItemsPerPage());
    }

    public String getFullPath() {
        return pathItem.getFullPath();
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
    
    public Date getProfileDate() {
        return profileBrowser.getProfileDate();
    }

    public Profile getProfile() {
        return (Profile) getRequest().getAttributes().get("profile");
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public DataService getDataService() {
        return dataService;
    }

    public String getBrowserFullPath() {
        if ((getProfile() != null) && (pathItem != null)) {
            return "/profiles/" + getProfile().getDisplayPath() + pathItem.getFullPath();
        } else {
            return "/profiles";
        }
    }

    //TODO - Move to filter - validation is not general to all Profile Resources
    public boolean validateParameters() {
        if (getAPIVersion().isVersionOne()) {
            if (containsCalendarParams()) {
                badRequest(APIFault.INVALID_API_PARAMETERS);
                return false;
            }
        } else {
            if (!validISODateTimeFormats()) {
                badRequest(APIFault.INVALID_DATE_FORMAT);
                return false;
            }
            if (isGET()) {
                if (containsProfileDate()) {
                    badRequest(APIFault.INVALID_API_PARAMETERS);
                    return false;
                }
                if (proRateModeHasNoEndDate()) {
                    badRequest(APIFault.INVALID_PRORATA_REQUEST);
                    return false;
                }
            } else {
                if (containsValidFromOrEnd()) {
                    badRequest(APIFault.INVALID_API_PARAMETERS);
                    return false;
                }
                if (containsPerUnitNoneAndNoDuraton()) {
                    badRequest(APIFault.INVALID_API_PARAMETERS);
                    return false;
                }
            }
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
