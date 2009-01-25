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

import com.jellymold.sheet.Sheet;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.cache.CacheHelper;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.profile.ProfileBrowser;
import gc.carbon.domain.data.DataCategory;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class ProfileSheetService implements Serializable {

    private CacheHelper cacheHelper = CacheHelper.getInstance();

    ProfileSheetService() {
        super();
    }

    public Sheet getSheet(ProfileBrowser browser, CacheableFactory builder) {
        ThreadBeanHolder.set("profileBrowserForFactory", browser);
        ThreadBeanHolder.set("dataCategoryForFactory", browser.getDataCategory());
        return (Sheet) cacheHelper.getCacheable(builder);
    }

    public Sheet getSheet(ProfileBrowser browser, DataCategory dataCategory, CacheableFactory builder) {
        ThreadBeanHolder.set("profileBrowserForFactory", browser);
        ThreadBeanHolder.set("dataCategoryForFactory", dataCategory);
        return (Sheet) cacheHelper.getCacheable(builder);
    }

    public void removeSheets(ProfileBrowser browser) {
        cacheHelper.clearCache("ProfileSheets", "ProfileSheet_" + browser.getProfile().getUid());
    }
}