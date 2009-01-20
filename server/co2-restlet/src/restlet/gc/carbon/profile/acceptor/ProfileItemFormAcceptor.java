package gc.carbon.profile.acceptor;

import gc.carbon.profile.ProfileItemResource;
import gc.carbon.profile.ProfileService;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.profile.ValidFromDate;
import gc.carbon.domain.data.ItemValue;
import com.jellymold.utils.APIFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.restlet.resource.Representation;
import org.restlet.data.Form;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

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
public class ProfileItemFormAcceptor implements Acceptor {

    private final Log log = LogFactory.getLog(getClass());
    private ProfileItemResource resource;
    private ProfileService profileService;

    public ProfileItemFormAcceptor(ProfileItemResource resource) {
        this.resource = resource;
        this.profileService = resource.getProfileService();
    }

    public List<ProfileItem> accept(Representation entity) {
        return accept(resource.getForm());
    }

    //TODO - PI Acceptor is returning a collection of PIs - this reflect the Acceptor's original usage with PC. Move to seperate acceptor (done this for PIV)
    public List<ProfileItem> accept(Form form) {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        ProfileItem profileItem = null;

        profileItem = resource.getProfileBrowser().getProfileItem();

        // ensure updated ProfileItem does not break rules for ProfileItems
        ProfileItem profileItemCopy = profileItem.getCopy();
        updateProfileItem(profileItemCopy, form);

        // ensure endDate is not before startDate
        if (profileItemCopy.getEndDate() != null && profileItemCopy.getEndDate().before(profileItemCopy.getStartDate())) {
            resource.badRequest(APIFault.INVALID_DATE_RANGE);
            return profileItems;
        }

        if (profileService.isUnique(profileItemCopy)) {

            // update persistent ProfileItem
            updateProfileItem(profileItem, form);

            // update ItemValues if supplied
            Map<String, ItemValue> itemValues = profileItem.getItemValuesMap();
            for (String name : form.getNames()) {
                ItemValue itemValue = itemValues.get(name);
                if (itemValue != null) {
                    itemValue.setValue(form.getFirstValue(name));
                    if (itemValue.hasUnits() && form.getNames().contains(name+"Unit")) {
                        itemValue.setUnit(form.getFirstValue(name + "Unit"));
                    }
                    if (itemValue.hasPerUnits() && form.getNames().contains(name+"PerUnit")) {
                        itemValue.setPerUnit(form.getFirstValue(name + "PerUnit"));
                    }
                }
            }
            log.debug("storeRepresentation() - ProfileItem updated");

            // all done, need to recalculate and clear caches
            profileService.calculate(profileItem);
            profileService.clearCaches(resource.getProfileBrowser());

        } else {
            log.warn("storeRepresentation() - ProfileItem NOT updated");
            resource.badRequest(APIFault.DUPLICATE_ITEM);
            return profileItems;
        }

        if (profileItem != null) {
            profileItems.add(profileItem);
        }

        return profileItems;
    }

    //TODO - parsing v1 and v2 params - see Acceptors which at least conditionally parse based on APIVersion. Ideal solution should be transparent tho.
    protected void updateProfileItem(ProfileItem profileItem, Form form) {

        Set<String> names = form.getNames();

        if (!resource.validateParameters()) {
            return;
        }

        // update 'name' value
        if (names.contains("name")) {
            profileItem.setName(form.getFirstValue("name"));
        }

        // update 'startDate' value
        if (names.contains("startDate")) {
            profileItem.setStartDate(new StartEndDate(form.getFirstValue("startDate")));
        }

        // update 'startDate' value
        if (names.contains("validFrom")) {
            profileItem.setStartDate(new ValidFromDate(form.getFirstValue("validFrom")));
        }

        // update 'end' value
        if (names.contains("end")) {
            profileItem.setEnd(Boolean.valueOf(form.getFirstValue("end")));
        }

        // update 'endDate' value
        if (names.contains("endDate")) {
            if (StringUtils.isNotBlank(form.getFirstValue("endDate"))) {
                profileItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
            } else {
                profileItem.setEndDate(null);
            }
        } else {
            // update 'duration' value
            if (form.getNames().contains("duration")) {
                StartEndDate endDate = profileItem.getStartDate().plus(form.getFirstValue("duration"));
                profileItem.setEndDate(endDate);
            }
        }
    }

}
