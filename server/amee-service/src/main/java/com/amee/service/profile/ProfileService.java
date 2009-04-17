package com.amee.service.profile;

import com.amee.domain.APIVersion;
import com.amee.domain.Pager;
import com.amee.domain.auth.User;
import com.amee.domain.cache.CacheableFactory;
import com.amee.domain.data.DataCategory;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.profile.StartEndDate;
import com.amee.domain.sheet.Sheet;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Primary service interface for Profile Resources.
 * <p/>
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
public class ProfileService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileSheetService profileSheetService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProfileServiceDAO dao;

    @Autowired
    PathItemService pathItemService;

    public void clearCaches(Profile profile) {
        log.debug("clearCaches()");
        profileSheetService.removeSheets(profile);
    }

    public void persist(ProfileItem pi, APIVersion apiVersion) {
        em.persist(pi);
        dao.checkProfileItem(pi, apiVersion);
    }

    public void persist(Profile p) {
        em.persist(p);
    }

    public void remove(ProfileItem pi) {
        em.remove(pi);
    }

    public void remove(Profile p) {
        dao.remove(p);
    }

    public List<Profile> getProfiles(Pager pager) {
        return dao.getProfiles(pager);
    }

    public ProfileItem getProfileItem(String profileItemUid) {
        //TODO - Need to remove ad-hoc usage of ThreadBeanHolder
        User user = (User) ThreadBeanHolder.get("user");
        return dao.getProfileItem(profileItemUid, user.getAPIVersion());
    }

    public List<ProfileItem> getProfileItems(Profile p) {
        return dao.getProfileItems(p);
    }

    public Profile getProfile(String path) {
        return dao.getProfile(path);
    }

    public List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate) {
        return dao.getProfileItems(profile, dataCategory, startDate, endDate);
    }

    public List<ProfileItem> getProfileItems(Profile p, DataCategory dc, Date date) {
        return dao.getProfileItems(p, dc, date);
    }

    public Collection<Long> getProfileDataCategoryIds(Profile profile) {
        return dao.getProfileDataCategoryIds(profile);
    }

    public Sheet getSheet(CacheableFactory sheetFactory) {
        return profileSheetService.getSheet(sheetFactory);
    }

    public Sheet getSheet(DataCategory dataCategory, CacheableFactory sheetFactory) {
        return profileSheetService.getSheet(dataCategory, sheetFactory);
    }

    public boolean isUnique(ProfileItem pi) {
        return !dao.equivilentProfileItemExists(pi);
    }
}


