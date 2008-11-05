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
import com.jellymold.utils.cache.CacheHelper;
import gc.carbon.domain.path.PathItemGroup;
import gc.carbon.domain.profile.Profile;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;

@Name("pathItemService")
@Scope(ScopeType.EVENT)
public class PathItemService implements Serializable {

    private final static Logger log = Logger.getLogger(PathItemService.class);

    @In(create = true)
    private EnvironmentPIGFactory environmentPIGFactory;

    @In(create = true)
    private ProfilePIGFactory profilePIGFactory;

    private CacheHelper cacheHelper = CacheHelper.getInstance();

    public PathItemService() {
        super();
    }

    public PathItemGroup getPathItemGroup(Environment environment) {
        environmentPIGFactory.setEnvironment(environment);
        return (PathItemGroup) cacheHelper.getCacheable(environmentPIGFactory);
    }

    public void removePathItemGroup(Environment environment) {
        cacheHelper.remove("EnvironmentPIGs", environment.getUid());
    }

    public PathItemGroup getPathItemGroup(Environment environment, Profile profile) {
        profilePIGFactory.setEnvironment(environment);
        profilePIGFactory.setProfile(profile);
        return (PathItemGroup) cacheHelper.getCacheable(profilePIGFactory);
    }

    // TODO:
    public void removePathItemGroup(Profile profile) {
        cacheHelper.remove("ProfilePIGs", profile.getUid());
    }
}
