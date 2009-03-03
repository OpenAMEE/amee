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
import com.amee.domain.data.ItemValue;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.data.DataService;
import com.amee.service.definition.DefinitionServiceDAO;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.profile.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ProfilePIGFactory extends BasePIGFactory implements CacheableFactory {

    @Autowired
    private DefinitionServiceDAO definitionServiceDAO;

    @Autowired
    private DataService dataService;

    @Autowired
    private ProfileService profileService;

    public ProfilePIGFactory() {
        super();
    }

    public Object create() {
        PathItemGroup pathItemGroup = null;
        Environment environment = EnvironmentService.getEnvironment();
        Profile profile = (Profile) ThreadBeanHolder.get("profile");
        List<DataCategory> dataCategories = dataService.getDataCategories(environment);
        DataCategory rootDataCategory = findRootDataCategory(dataCategories);
        if (rootDataCategory != null) {
            pathItemGroup = new PathItemGroup(new PathItem(rootDataCategory));
            while (!dataCategories.isEmpty()) {
                addDataCategories(pathItemGroup, dataCategories);
            }
            definitionServiceDAO.getItemDefinitions(environment); // preload so we can iterate over ItemValues later
            List<ProfileItem> profileItems = profileService.getProfileItems(profile);
            while (!profileItems.isEmpty()) {
                addProfileItems(pathItemGroup, profileItems);
            }
        }
        return pathItemGroup;
    }

    protected void addProfileItems(PathItemGroup pathItemGroup, List<ProfileItem> profileItems) {
        PathItem parent;
        PathItem child;
        Map<String, PathItem> pathItems = pathItemGroup.getPathItems();
        Iterator<ProfileItem> iterator = profileItems.iterator();
        while (iterator.hasNext()) {
            ProfileItem profileItem = iterator.next();
            parent = pathItems.get(profileItem.getDataCategory().getUid());
            if (parent != null) {
                iterator.remove();
                child = new PathItem(profileItem);
                parent.add(child);
                for (ItemValue itemValue : profileItem.getItemValues()) {
                    child.add(new PathItem(itemValue));
                }
            }
        }
    }

    public String getKey() {
        Profile profile = (Profile) ThreadBeanHolder.get("profile");
        return profile.getUid();
    }

    public String getCacheName() {
        return "ProfilePIGs";
    }
}