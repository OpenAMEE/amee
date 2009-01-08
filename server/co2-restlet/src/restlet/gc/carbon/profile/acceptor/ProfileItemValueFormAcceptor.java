package gc.carbon.profile.acceptor;

import gc.carbon.profile.ProfileService;
import gc.carbon.profile.ProfileItemValueResource;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.data.ItemValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.resource.Representation;
import org.restlet.data.Form;

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
public class ProfileItemValueFormAcceptor implements ItemValueAcceptor {

    private final Log log = LogFactory.getLog(getClass());
    private ProfileItemValueResource resource;
    private ProfileService profileService;

    public ProfileItemValueFormAcceptor(ProfileItemValueResource resource) {
        this.resource = resource;
        this.profileService = resource.getProfileService();
    }

    public ItemValue accept(Representation entity) {
        return accept(resource.getForm());
    }

    public ItemValue accept(Form form) {

        ItemValue profileItemValue = resource.getProfileBrowser().getProfileItemValue();
        ProfileItem profileItem = resource.getProfileBrowser().getProfileItem();
        // are we updating this ProfileItemValue?
        if (form.getFirstValue("value") != null) {
            // update ProfileItemValue
            profileItemValue.setValue(form.getFirstValue("value"));
        }
        if (form.getFirstValue("unit") != null) {
            // update ProfileItemValue
            profileItemValue.setUnit(form.getFirstValue("unit"));
        }
        if (form.getFirstValue("perUnit") != null) {
            // update ProfileItemValue
            profileItemValue.setPerUnit(form.getFirstValue("perUnit"));
        }
        // should recalculate now (regardless)
        profileService.calculate(profileItem);
        // path may have changed
        profileService.clearCaches(resource.getProfileBrowser());

        return profileItemValue;
    }

}