/**
* This file is part of AMEE.
*
* AMEE is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
*
* AMEE is free software and is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* Created by http://www.dgen.net.
* Website http://www.amee.cc
*/
package gc.carbon.profile;

import com.jellymold.kiwi.ResourceActions;
import com.jellymold.kiwi.Environment;
import com.jellymold.utils.BaseBrowser;
import gc.carbon.data.DataCategory;
import gc.carbon.data.DataService;
import gc.carbon.data.ItemValue;
import gc.carbon.path.PathItem;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.restlet.data.Form;

import javax.persistence.EntityManager;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Name("profileBrowser")
@Scope(ScopeType.EVENT)
public class ProfileBrowser extends BaseBrowser {

    private final static Logger log = Logger.getLogger(ProfileBrowser.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private ProfileService profileService;

    @In(required = false)
    private Environment environment;

    @In(scope = ScopeType.EVENT, required = false)
    private Profile profile;

    private ResourceActions profileActions = new ResourceActions("profile");

    @In(scope = ScopeType.EVENT, required = false)
    private PathItem pathItem;

    // ProfileCategories

    @In(scope = ScopeType.EVENT, required = false)
    private DataCategory dataCategory = null;

    private String dataCategoryUid = null;

    private ResourceActions dataCategoryActions = new ResourceActions("profileCategory");

    // ProfileItems

    @In(scope = ScopeType.EVENT, required = false)
    private ProfileItem profileItem = null;

    private String profileItemUid = null;

    private ResourceActions profileItemActions = new ResourceActions("profileItem");

    // ProfileItemValues

    @In(scope = ScopeType.EVENT, required = false)
    private ItemValue profileItemValue = null;

    private String profileItemValueUid = null;

    private ResourceActions profileItemValueActions = new ResourceActions("profileItemValue");

    // profile date
    private Date profileDate = Calendar.getInstance().getTime();
    // TODO: private int profileDatePrecision = Calendar.MONTH;

    public PathItem getPathItem() {
        return pathItem;
    }

    public String getFullPath() {
        if ((getProfile() != null) && (getPathItem() != null)) {
            return "/profiles/" + getProfile().getDisplayPath() + getPathItem().getFullPath();
        } else {
            return "/profiles";
        }
    }

    // Profiles

    public Profile getProfile() {
        return profile;
    }

    public ResourceActions getProfileActions() {
        return profileActions;
    }

    // ProfileCategories

    public String getDataCategoryUid() {
        return dataCategoryUid;
    }

    public void setDataCategoryUid(String dataCategoryUid) {
        this.dataCategoryUid = dataCategoryUid;
    }

    public DataCategory getDataCategory() {
        if (dataCategory == null) {
            if (getDataCategoryUid() != null) {
                dataCategory = dataService.getDataCategory(environment, getDataCategoryUid());
            }
        }
        return dataCategory;
    }

    public ResourceActions getEnvironmentActions() {
        return dataCategoryActions;
    }

    // ProfileItems

    public String getProfileItemUid() {
        return profileItemUid;
    }

    public void setProfileItemUid(String profileItemUid) {
        this.profileItemUid = profileItemUid;
    }

    public ProfileItem getProfileItem() {
        if (profileItem == null) {
            if (profileItemUid != null) {
                profileItem = profileService.getProfileItem(profile.getUid(), dataCategoryUid, profileItemUid);
            }
        }
        return profileItem;
    }

    public ResourceActions getProfileItemActions() {
        return profileItemActions;
    }

    // ProfileItemValues

    public String getProfileItemValueUid() {
        return profileItemValueUid;
    }

    public void setProfileItemValueUid(String profileItemValueUid) {
        this.profileItemValueUid = profileItemValueUid;
    }

    public ItemValue getProfileItemValue() {
        if (profileItemValue == null) {
            if ((profileItemUid != null) && (profileItemValueUid != null)) {
                profileItemValue = profileService.getProfileItemValue(profileItemUid, profileItemValueUid);
            }
        }
        return profileItemValue;
    }

    public ResourceActions getProfileItemValueActions() {
        return profileItemValueActions;
    }

    // misc.

    /**
     * Set profileDate based on query string
     *
     * @param form
     */
    public void setProfileDate(Form form) {
        String profileDateStr = form.getFirstValue("profileDate");
        if (profileDateStr != null) {
            try {
                DateFormat profileDateFormat = new SimpleDateFormat("yyyyMM");
                setProfileDate(profileDateFormat.parse(profileDateStr));
            } catch (ParseException e) {
                // swallow
                log.warn("bad date");
                setProfileDate();
            }
        } else {
            setProfileDate();
        }
        log.debug("profileDate: " + getProfileDate());
    }

    /**
     * Set profileDate to *now* but with MONTH precision (1st of the month)
     * <p/>
     * TODO: logic to use profileDatePrecision (MONTH, DATE, etc)
     * TODO: hard coded to MONTH for now
     */
    public void setProfileDate() {
        Calendar profileDateCal = Calendar.getInstance();
        int year = profileDateCal.get(Calendar.YEAR);
        int month = profileDateCal.get(Calendar.MONTH);
        profileDateCal.clear();
        profileDateCal.set(year, month, 1); // first of the month
        setProfileDate(profileDateCal.getTime());
    }

    public Date getProfileDate() {
        return profileDate;
    }

    public void setProfileDate(Date profileDate) {
        this.profileDate = profileDate;
    }
}
