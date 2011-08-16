package com.amee.service.profile;

import com.amee.domain.data.DataCategory;
import com.amee.domain.item.profile.ProfileItem;
import com.amee.domain.profile.Profile;
import com.amee.platform.science.StartEndDate;
import com.amee.service.item.ProfileItemServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SelectByProfileService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ProfileService profileService;

    @Autowired
    ProfileItemServiceImpl profileItemService;

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