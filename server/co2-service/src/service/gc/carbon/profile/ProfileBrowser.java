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
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.utils.ThreadBeanHolder;
import gc.carbon.BaseBrowser;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.ProfileDate;
import gc.carbon.domain.profile.ProfileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
@Scope("prototype")
public class ProfileBrowser extends BaseBrowser {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileService profileService;

    // Profiles
    private Profile profile;
    private ResourceActions profileActions = new ResourceActions("profile");

    // ProfileCategories
    private DataCategory dataCategory = null;
    private String dataCategoryUid = null;
    private ResourceActions dataCategoryActions = new ResourceActions("profileCategory");

    // ProfileItems
    private ProfileItem profileItem = null;
    private String profileItemUid = null;
    private ResourceActions profileItemActions = new ResourceActions("profileItem");

    // ProfileItemValues
    private ItemValue profileItemValue = null;
    private String profileItemValueUid = null;
    private ResourceActions profileItemValueActions = new ResourceActions("profileItemValue");

    // profile date
    private Date profileDate = Calendar.getInstance().getTime();

    private String selectBy;
    private String mode;

    public ProfileBrowser() {
        super();
        profile = (Profile) ThreadBeanHolder.get("profile");
        dataCategory = (DataCategory) ThreadBeanHolder.get("dataCategory");
        profileItem = (ProfileItem) ThreadBeanHolder.get("profileItem");
        profileItemValue = (ItemValue) ThreadBeanHolder.get("profileItemValue");
    }

    // General
    public String getFullPath() {
        if ((getProfile() != null) && (pathItem != null)) {
            return "/profiles/" + getProfile().getDisplayPath() + pathItem.getFullPath();
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
                dataCategory = dataService.getDataCategory(EnvironmentService.getEnvironment(), getDataCategoryUid());
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

    public void setProfileDate(String profileDate) {
        this.profileDate = new ProfileDate(profileDate);
    }


    public Date getProfileDate() {
        return profileDate;
    }

    public void setSelectBy(String selectBy) {
        this.selectBy = selectBy;
    }

    public String getSelectBy() {
        return selectBy;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setDuration(String duration) {
        if (duration != null) {
            endDate = startDate.plus(duration);
        }
    }

    public boolean isProRataRequest() {
        return getMode() != null && getMode().equals("prorata");
    }

    public boolean isSelectByRequest() {
        return getSelectBy() != null;
    }
}
