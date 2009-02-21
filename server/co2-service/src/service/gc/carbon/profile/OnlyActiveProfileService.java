package gc.carbon.profile;

import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.profile.ProfileBrowser;
import gc.carbon.profile.ProfileService;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

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
public class OnlyActiveProfileService extends ProfileService {

    ProfileService delegatee;

    public OnlyActiveProfileService(ProfileService delegatee) {
        this.delegatee = delegatee;
    }

    public List<ProfileItem> getProfileItems(final Profile profile, final DataCategory dataCategory,
                                             final StartEndDate startDate, final StartEndDate endDate) {

       List<ProfileItem> requestedItems;

        final List<ProfileItem> profileItems = delegatee.getProfileItems(profile, dataCategory, startDate, endDate);
        requestedItems = (List) CollectionUtils.select(profileItems, new Predicate() {
            public boolean evaluate(Object o) {
                ProfileItem pi = (ProfileItem) o;
                for (ProfileItem innerProfileItem : profileItems) {
                    if (pi.getDataItem().equals(innerProfileItem.getDataItem()) &&
                            pi.getName().equalsIgnoreCase(innerProfileItem.getName()) &&
                            pi.getStartDate().before(innerProfileItem.getStartDate()) &&
                            !innerProfileItem.getStartDate().after(startDate.toDate())) {
                        return false;
                    }
                }
                return true;
            }
        });
        return requestedItems;
    }
}
