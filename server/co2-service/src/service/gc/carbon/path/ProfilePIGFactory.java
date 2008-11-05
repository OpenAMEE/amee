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
package gc.carbon.path;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.cache.Cacheable;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.data.DataService;
import gc.carbon.definition.DefinitionService;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.path.PathItem;
import gc.carbon.domain.path.PathItemGroup;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Name("profilePIGFactory")
@Scope(ScopeType.EVENT)
public class ProfilePIGFactory extends BasePIGFactory implements CacheableFactory {

    @In(create = true)
    private DefinitionService definitionService;

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private ProfileService profileService;

    private Environment environment;

    private Profile profile;

    public ProfilePIGFactory() {
        super();
    }

    public Cacheable createCacheable() {
        PathItemGroup pathItemGroup = null;
        List<DataCategory> dataCategories = dataService.getDataCategories(environment);
        DataCategory rootDataCategory = findRootDataCategory(dataCategories);
        if (rootDataCategory != null) {
            pathItemGroup = new PathItemGroup(new PathItem(rootDataCategory));
            while (!dataCategories.isEmpty()) {
                addDataCategories(pathItemGroup, dataCategories);
            }
            definitionService.getItemDefinitions(environment); // preload so we can iterate over ItemValues later
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
        return profile.getUid();
    }

    public String getCacheName() {
        return "ProfilePIGs";
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}