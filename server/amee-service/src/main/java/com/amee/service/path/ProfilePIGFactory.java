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
package com.amee.service.path;

import com.amee.domain.cache.CacheableFactory;
import com.amee.domain.data.DataCategory;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.profile.Profile;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfilePIGFactory extends BasePIGFactory implements CacheableFactory {

    @Autowired
    private DataService dataService;

    public ProfilePIGFactory() {
        super();
    }

    public Object create() {
        PathItemGroup pathItemGroup = null;
        Environment environment = EnvironmentService.getEnvironment();
        List<DataCategory> dataCategories = dataService.getDataCategories(environment);
        DataCategory rootDataCategory = findRootDataCategory(dataCategories);
        if (rootDataCategory != null) {
            pathItemGroup = new PathItemGroup(new PathItem(rootDataCategory), true);
            while (!dataCategories.isEmpty()) {
                addDataCategories(pathItemGroup, dataCategories);
            }
        }
        return pathItemGroup;
    }

    public String getKey() {
        Profile profile = (Profile) ThreadBeanHolder.get("profile");
        return profile.getUid();
    }

    public String getCacheName() {
        return "ProfilePIGs";
    }
}