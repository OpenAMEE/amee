package gc.carbon.profile;

import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIObject;
import gc.carbon.AMEEResource;
import gc.carbon.APIVersion;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.profile.Profile;
import gc.carbon.data.builder.BuildableResource;
import gc.carbon.data.DataService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
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
@Component("baseProfileResource")
public abstract class BaseProfileResource extends AMEEResource implements BuildableResource {

    @Autowired
    protected ProfileService profileService;

    @Autowired
    protected DataService dataService;

    protected ProfileForm form;
    protected ProfileBrowser profileBrowser;

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        this.profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");

    }

    public Pager getPager() {
        return getPager(getItemsPerPage());
    }

    public void setForm(ProfileForm form) {
        this.form = form;
    }

    public ProfileForm getForm() throws IllegalArgumentException {
        if (form == null) {
            form = new ProfileForm(super.getForm(), getVersion());
        }
        return form;
    }

    public APIVersion getVersion() throws IllegalArgumentException {
        return (APIVersion) getRequest().getAttributes().get("apiVersion");
    }

    public String getFullPath() {
        return pathItem.getFullPath();
    }

    public boolean hasParent() {
        return pathItem.getParent() != null;
    }

    public Set<? extends APIObject> getChildrenByType(String type) {
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

    public Date getStartDate() {
        return profileBrowser.getStartDate();
    }

    public Date getEndDate() {
        return profileBrowser.getEndDate();
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

    //TODO - Move, validation is not general to all Profile Resources
    public boolean validateParameters() {
        if (getVersion().isVersionOne()) {
            if (containsCalendarParams() || containsReturnUnitParams() ) {
                return false;
            }
        } else {
            if (isGET()) {
                if (containsProfileDate()) {
                    return false;
                }
                if (proRateModeHasNoEndDate()) {
                    return false;
                }
            } else {
                if (containsValidFromOrEnd()) {
                    return false;
                }
            }
            return isValidBoundedCalendarRequest();
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

    private boolean containsReturnUnitParams() {
        return getForm().getNames().contains("returnUnit") ||
                getForm().getNames().contains("returnPerUnit");
    }

    private boolean containsProfileDate() {
        return getForm().getNames().contains("profileDate");
    }

    private boolean containsValidFromOrEnd() {
        return getForm().getNames().contains("validFrom") ||
                getForm().getNames().contains("end");
    }

    private boolean isValidBoundedCalendarRequest() {
        int count = CollectionUtils.countMatches(getForm().getNames(), new Predicate() {
            public boolean evaluate(Object o) {
                String p = (String) o;
                return (p.equals("endDate") || p.equals("duration"));
            }
        });
        return (count <= 1);
    }
}