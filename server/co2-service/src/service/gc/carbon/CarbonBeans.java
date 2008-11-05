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
package gc.carbon;

import gc.carbon.data.Calculator;
import gc.carbon.data.DataService;
import gc.carbon.definition.DefinitionService;
import gc.carbon.path.PathItemService;
import gc.carbon.profile.ProfileService;
import org.jboss.seam.Component;

public class CarbonBeans {

    public static ProfileService getProfileService() {
        return (ProfileService) Component.getInstance("profileService", true);
    }

    public static DataService getDataService() {
        return (DataService) Component.getInstance("dataService", true);
    }

    public static DefinitionService getEnvironmentService() {
        return (DefinitionService) Component.getInstance("environmentService", true);
    }

    public static PathItemService getPathItemService() {
        return (PathItemService) Component.getInstance("pathItemService", true);
    }

    public static Calculator getCalculator() {
        return (Calculator) Component.getInstance("calculator", true);
    }
}
