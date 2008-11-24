package gc.carbon.profile;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIObject;
import gc.carbon.BaseResource;
import gc.carbon.builder.APIVersion;
import gc.carbon.builder.resource.BuildableResource;
import gc.carbon.data.Calculator;
import gc.carbon.data.DataService;
import gc.carbon.domain.path.PathItem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import javax.persistence.EntityManager;
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
public abstract class BaseProfileResource extends BaseResource implements BuildableResource {

    ProfileForm form;

    public BaseProfileResource() {
        super();
    }

    public BaseProfileResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    public abstract ProfileSheetService getProfileSheetService();

    public abstract PathItem getPathItem();

    public abstract Pager getPager();

    public abstract ProfileService getProfileService();

    public abstract DataService getDataService();

    public abstract Environment getEnvironment();

    public abstract Calculator getCalculator();

    public abstract EntityManager getEntityManager();

    public void setForm(ProfileForm form) {
        this.form = form;
    }

    public ProfileForm getForm() throws IllegalArgumentException {
        if (form == null)
            form = new ProfileForm(super.getForm());
        return form;
    }

    public APIVersion getVersion() throws IllegalArgumentException {
        if (form == null)
            form = new ProfileForm(super.getForm());
        return form.getVersion();
    }

    public String getFullPath() {
        return getPathItem().getFullPath();
    }

    public boolean hasParent() {
        return getPathItem().getParent() != null;
    }

    public Set<? extends APIObject> getChildrenByType(String type) {
        return getPathItem().getChildrenByType(type);
    }

    private boolean isGET() {
        return getRequest().getMethod().equals(Method.GET);
    }

    public boolean isValidRequest() {

        if (getForm().isVersionOne()) {

            if (containsCalendarParams())
                return false;

        } else {

            if (isGET()) {

                if (containsProfileDate())
                    return false;

            } else {

                if (containsValidFromOrEnd())
                    return false;
            }

            return isValidBoundedCalendarRequest();

        }
        return true;
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

    //TODO - end is not allowed in 2.0 so can be removed
    private boolean isValidBoundedCalendarRequest() {
        int count = CollectionUtils.countMatches(getForm().getNames(), new Predicate() {
            public boolean evaluate(Object o) {
                String p = (String) o;
                return (p.equals("end") || (p.equals("endDate") || p.equals("duration")));
            }
        });
        return (count <= 1);
    }

}


