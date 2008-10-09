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
import gc.carbon.BaseBrowser;
import gc.carbon.data.DataCategory;
import gc.carbon.data.ItemValue;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("profileBrowser")
@Scope(ScopeType.EVENT)
public class ProfileBrowser extends BaseBrowser {

    private final static Logger log = Logger.getLogger(ProfileBrowser.class);

    @In(create = true)
    private ProfileService profileService;

    @In(scope = ScopeType.EVENT, required = false)
    private Profile profile;

    private ResourceActions profileActions = new ResourceActions("profile");

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

    // General

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

}
