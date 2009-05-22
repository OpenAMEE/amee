package com.amee.service.profile;

import com.amee.domain.StartEndDate;
import com.amee.domain.data.DataCategory;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
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
public class OnlyActiveProfileService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ProfileService profileService;

    /**
     * Retreive only the active {@link com.amee.domain.profile.ProfileItem} instances.
     * <p/>
     * Active in this scenario means the latest in any historical sequence within the given datetime range.
     *
     * @param profile      - the owning {@link com.amee.domain.profile.Profile}
     * @param dataCategory - the containing {@link com.amee.domain.data.DataCategory}
     * @param startDate    - the opening {@link com.amee.domain.StartEndDate} of the datatime range.
     * @param endDate      - the closing {@link com.amee.domain.StartEndDate} of the datatime range.
     * @return the List of active {@link com.amee.domain.profile.ProfileItem}
     */
    public List<ProfileItem> getProfileItems(
            final Profile profile,
            final DataCategory dataCategory,
            final StartEndDate startDate,
            final StartEndDate endDate) {

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() start");
        }

        // get all profile items in range
        final List<ProfileItem> profileItems = profileService.getProfileItems(
                profile, dataCategory, startDate, endDate);

        // filter
        List<ProfileItem> requestedItems = getProfileItems(profileItems, startDate);

        if (log.isDebugEnabled()) {
            log.debug("getProfileItems() done (" + profileItems.size() + ")");
        }

        return requestedItems;
    }

    /**
     * Filter the supplied ProfileItem list to only include the latest items in the historical
     * sequence. If a start date is supplied items must start after that date.
     *
     * @param profileItems - list to filter
     * @param startDate    - the opening {@link com.amee.domain.StartEndDate} of the datatime range. Optional.
     * @return the List of active {@link com.amee.domain.profile.ProfileItem}
     */
    @SuppressWarnings("unchecked")
    public List<ProfileItem> getProfileItems(final List<ProfileItem> profileItems, final StartEndDate startDate) {
        if (profileItems == null) {
            return null;
        }
        return (List) CollectionUtils.select(profileItems, new Predicate() {
            public boolean evaluate(Object o) {
                ProfileItem pi = (ProfileItem) o;
                for (ProfileItem profileItem : profileItems) {
                    if (pi.getDataItem().equals(profileItem.getDataItem()) &&
                            pi.getName().equalsIgnoreCase(profileItem.getName()) &&
                            pi.getStartDate().before(profileItem.getStartDate()) &&
                            ((startDate == null) || !profileItem.getStartDate().after(startDate.toDate()))) {
                        return false;
                    }
                }
                return true;
            }
        });
    }
}
