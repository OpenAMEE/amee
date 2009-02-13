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
import com.jellymold.utils.ThreadBeanHolder;
import gc.carbon.BaseBrowser;
import gc.carbon.data.DataService;
import gc.carbon.domain.AMEECompoundUnit;
import gc.carbon.domain.AMEEPerUnit;
import gc.carbon.domain.AMEEUnit;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.ProfileDate;
import gc.carbon.domain.profile.ProfileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component("profileBrowser")
@Scope("prototype")
public class ProfileBrowser extends BaseBrowser {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    protected DataService dataService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    @Qualifier("profileActions")
    private ResourceActions profileActions;

    @Autowired
    @Qualifier("profileCategoryActions")
    private ResourceActions profileCategoryActions;

    @Autowired
    @Qualifier("profileItemActions")
    private ResourceActions profileItemActions;

    @Autowired
    @Qualifier("profileItemValueActions")
    private ResourceActions profileItemValueActions;

    // Profiles
    private Profile profile;

    // ProfileCategories
    private DataCategory dataCategory = null;
    private String dataCategoryUid = null;

    // ProfileItems
    private ProfileItem profileItem = null;
    private String profileItemUid = null;

    // ProfileItemValues
    private ItemValue profileItemValue = null;
    private String profileItemValueUid = null;

    // ProfileDate (API v1)
    private Date profileDate = new ProfileDate();

    // Return Unit
    private AMEEUnit returnUnit = ProfileItem.INTERNAL_RETURN_UNIT;

    // Filters
    private String selectBy;
    private String mode;

    public ProfileBrowser() {
        super();
        profile = (Profile) ThreadBeanHolder.get("profile");
    }

    // General
    public String getFullPath() {
        if ((getProfile() != null) && (pathItem != null)) {
            return "/profiles/" + getProfile().getDisplayPath() + pathItem.getFullPath();
        } else {
            return "/profiles";
        }
    }

    // Actions

    public ResourceActions getProfileActions() {
        return profileActions;
    }

    public ResourceActions getProfileCategoryActions() {
        return profileCategoryActions;
    }

    public ResourceActions getProfileItemActions() {
        return profileItemActions;
    }

    public ResourceActions getProfileItemValueActions() {
        return profileItemValueActions;
    }

    // Profiles

    public Profile getProfile() {
        return profile;
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
                dataCategory = dataService.getDataCategory(getDataCategoryUid());
            }
        }
        return dataCategory;
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
                profileItem = profileService.getProfileItem(profileItemUid);
            }
        }
        return profileItem;
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
            if (profileItemValueUid != null) {
                profileItemValue = profileService.getProfileItemValue(profileItemValueUid);
            }
        }
        return profileItemValue;
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
        if (duration != null && endDate == null) {
            endDate = startDate.plus(duration);
        }
    }

    public boolean isProRataRequest() {
        return getMode() != null && getMode().equals("prorata");
    }

    public boolean isSelectByRequest() {
        return getSelectBy() != null;
    }

    public AMEEUnit getReturnUnit() {
        return returnUnit;
    }

    public boolean returnInExternalUnit() {
        return !returnUnit.equals(ProfileItem.INTERNAL_RETURN_UNIT);
    }

    //TODO - There is a documented work item to model return amount as an ItemValue. In the short-term, move to constants.
    public void setAmountReturnUnit(String returnUnit, String returnPerUnit) {

        if (returnUnit == null) {
            returnUnit = ProfileItem.INTERNAL_AMOUNT_UNIT.toString();
        }
        AMEEUnit unit = AMEEUnit.valueOf(returnUnit);

        if (returnPerUnit == null) {
            returnPerUnit = ProfileItem.INTERNAL_AMOUNT_PERUNIT.toString();
        }
        AMEEPerUnit perUnit = AMEEPerUnit.valueOf(returnPerUnit);

        this.returnUnit = AMEECompoundUnit.valueOf(unit, perUnit);
    }
}
