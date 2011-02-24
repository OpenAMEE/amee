package com.amee.service.profile;

import com.amee.domain.data.DataCategory;
import com.amee.domain.profile.Profile;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.platform.science.StartEndDate;
import com.amee.service.item.ProfileItemService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class SelectByProfileService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ProfileService profileService;

    @Autowired
    ProfileItemService profileItemService;

    public List<ProfileItem> getProfileItems(
            Profile profile,
            DataCategory dataCategory,
            StartEndDate startDate,
            StartEndDate endDate,
            String selectBy) {

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() start");
        }

        List<ProfileItem> profileItems = profileItemService.getProfileItems(
                profile, dataCategory, startDate, endDate);

        if ("start".equals(selectBy)) {
            profileItems = selectByStart(startDate, profileItems);

        } else if ("end".equals(selectBy)) {
            profileItems = selectByEnd(endDate, profileItems);
        }

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() done (" + profileItems.size() + ")");
        }

        return profileItems;
    }

    private List<ProfileItem> selectByEnd(final StartEndDate endDate, List<ProfileItem> profileItems) {
        profileItems = (List) CollectionUtils.select(profileItems, new Predicate() {
            public boolean evaluate(Object o) {
                ProfileItem pi = (ProfileItem) o;
                return (pi.getEndDate() != null) &&
                        (endDate != null) &&
                        (pi.getEndDate().getTime() < endDate.getTime());
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