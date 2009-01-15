package gc.carbon.profile;

import com.jellymold.kiwi.User;
import com.jellymold.utils.APIFault;
import com.jellymold.utils.Pager;
import com.jellymold.utils.ThreadBeanHolder;
import gc.carbon.AMEEResource;
import gc.carbon.APIVersion;
import gc.carbon.data.DataService;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.StartEndDate;
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

    public DataCategory getDataCategory() {
        return profileBrowser.getDataCategory();
    }

    public Date getProfileDate() {
        return profileBrowser.getProfileDate();
    }

    public Profile getProfile() {
        return profileBrowser.getProfile();
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public DataService getDataService() {
        return dataService;
    }

    //TODO - Move to filter - validation is not general to all Profile Resources
    public boolean validateParameters() {
        if (getVersion().isVersionOne()) {
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
}
