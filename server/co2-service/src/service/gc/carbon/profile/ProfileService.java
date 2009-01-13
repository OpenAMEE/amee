package gc.carbon.profile;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import gc.carbon.path.PathItemService;
import gc.carbon.profile.ProfileServiceDAO;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.data.Calculator;
import gc.carbon.APIVersion;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.cache.CacheableFactory;
import com.jellymold.utils.Pager;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Date;

/**
 * Primary service interface for Profile Resources.
 *
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

    @Autowired
    private ProfileSheetService profileSheetService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProfileServiceDAO dao;

    @Autowired
    PathItemService pathItemService;

    @Autowired
    private Calculator calculator;

    public void clearCaches(ProfileBrowser profileBrowser) {
        pathItemService.removePathItemGroup(profileBrowser.getProfile());
        profileSheetService.removeSheets(profileBrowser);
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

    public ItemValue getProfileItemValue(String profileItemValueUid) {
        return dao.getProfileItemValue(profileItemValueUid);
    }

    public ProfileItem getProfileItem(String profileItemUid) {
        return dao.getProfileItem(profileItemUid);
    }

    public List<ProfileItem> getProfileItems(Profile p) {
        return dao.getProfileItems(p);
    }

    public List<ProfileItem> getProfileItems(ProfileBrowser browser) {
        return dao.getProfileItems(browser);
    }

    public List<ProfileItem> getProfileItems(Profile p, DataCategory dc, Date date) {
        return dao.getProfileItems(p, dc, date);
    }

    public Sheet getSheet(ProfileBrowser browser, CacheableFactory sheetFactory) {
        return profileSheetService.getSheet(browser, sheetFactory);
    }

    public boolean isUnique(ProfileItem pi) {
        return !dao.equivilentProfileItemExists(pi);
    }

    public void calculate(ProfileItem pi) {
        calculator.calculate(pi);
    }
}


