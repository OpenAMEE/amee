package gc.carbon.profile;

import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.profile.ProfileBrowser;
import gc.carbon.profile.ProfileService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.List;

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
public class SelectByProfileService extends ProfileService {

    ProfileService delegatee;

    private String selectBy;

    public SelectByProfileService(ProfileService delegatee, String selectBy) {
        this.delegatee = delegatee;
        this.selectBy = selectBy;
    }

    public List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate) {

        List<ProfileItem> profileItems = delegatee.getProfileItems(profile, dataCategory, startDate, endDate);

        if ("start".equals(selectBy)) {
            profileItems = selectByStart(startDate, profileItems);

        } else if ("end".equals(selectBy)) {
            profileItems = selectByEnd(endDate, profileItems);
        }

        return profileItems;

    }

    private List<ProfileItem> selectByEnd(final StartEndDate endDate, List<ProfileItem> profileItems) {
        profileItems = (List) CollectionUtils.select(profileItems, new Predicate() {
            public boolean evaluate(Object o) {
                ProfileItem pi = (ProfileItem) o;
                return pi.getEndDate() != null && endDate != null &&
                        pi.getEndDate().getTime() < endDate.getTime();
            }
        });
        return profileItems;
    }

    private List<ProfileItem> selectByStart(final StartEndDate startDate, List<ProfileItem> profileItems) {
        profileItems = (List) CollectionUtils.select(profileItems, new Predicate() {
            public boolean evaluate(Object o) {
                return ((ProfileItem) o).getStartDate().getTime() >= startDate.getTime();
            }
        });
        return profileItems;
    }

}