package gc.carbon.profile;

import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.Profile;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemValue;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.measure.Measure;
import javax.measure.unit.SI;
import javax.measure.unit.NonSI;

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
public class ProRataProfileService extends ProfileService {

    ProfileService delegatee;

    public ProRataProfileService(ProfileService delegatee) {
        this.delegatee = delegatee;
    }

    public List<ProfileItem> getProfileItems(ProfileBrowser profileBrowser) {

        List<ProfileItem> requestedItems = new ArrayList<ProfileItem>();
        
        for (ProfileItem pi : delegatee.getProfileItems(profileBrowser)) {

            if (hasOnePerUnitItemValue(pi)) {

                ProfileItem pic = pi.getCopy();

                // Calculate the duration covered by the query.
                DateTime start = new DateTime(profileBrowser.getStartDate().getTime());
                DateTime end = new DateTime(profileBrowser.getEndDate().getTime());
                long duration = new Duration(start, end).getMillis();


                // Calculate the pro-rated amount
                // TODO - Assumes amount in a monthly value
                Measure<Long, javax.measure.quantity.Duration> durationRequested = Measure.valueOf(duration, SI.MILLI(SI.SECOND));
                pic.setAmount(pi.getAmount().multiply(new BigDecimal(durationRequested.doubleValue(NonSI.MONTH))).setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE));
                requestedItems.add(pic);

            } else {

                requestedItems.add(pi);

            }
        }

        return requestedItems;
    }

    private boolean hasOnePerUnitItemValue(ProfileItem pi) {
        List<ItemValue> itemValues = pi.getItemValues();
        int count = CollectionUtils.countMatches(itemValues, new Predicate() {
            public boolean evaluate(Object o) {
                return ((ItemValue) o).hasPerUnits();
            }
        });
        return (count == 1);
    }
}
