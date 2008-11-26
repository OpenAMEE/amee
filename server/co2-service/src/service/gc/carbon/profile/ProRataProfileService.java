package gc.carbon.profile;

import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.profile.ProfileItem;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.measure.Measure;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
public class ProRataProfileService extends ProfileService {

    private ProfileService delegatee;

    public ProRataProfileService(ProfileService delegatee) {
        this.delegatee = delegatee;
    }

    public List<ProfileItem> getProfileItems(ProfileBrowser profileBrowser) {

        List<ProfileItem> requestedItems = new ArrayList<ProfileItem>();

        for (ProfileItem pi : delegatee.getProfileItems(profileBrowser)) {

            String perUnit = getPerUnit(pi);
            if (perUnit != null) {

                ProfileItem pic = pi.getCopy();

                Measure<Long, javax.measure.quantity.Duration> requestedDuration = getDuration(profileBrowser.getStartDate(), profileBrowser.getEndDate());
                Unit internalPerUnit = Unit.valueOf(perUnit);

                pic.setAmount(pi.getAmount().multiply(new BigDecimal(requestedDuration.doubleValue(internalPerUnit))).setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE));
                requestedItems.add(pic);

            } else if (pi.getEndDate() != null) {

                ProfileItem pic = pi.getCopy();

                Measure<Long, javax.measure.quantity.Duration> requestedDuration = getDuration(profileBrowser.getStartDate(), profileBrowser.getEndDate());
                Measure<Long, javax.measure.quantity.Duration> itemDuration = getDuration(pi.getStartDate(), pi.getEndDate());

                BigDecimal proRata = new BigDecimal(requestedDuration.getValue()).divide(new BigDecimal(itemDuration.getValue()));
                pic.setAmount(pi.getAmount().multiply(proRata).setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE));
                requestedItems.add(pic);

            } else {

                requestedItems.add(pi);

            }
        }

        return requestedItems;
    }

    private Measure<Long, javax.measure.quantity.Duration> getDuration(Date startDate, Date endDate) {
        DateTime start = new DateTime(startDate.getTime());
        DateTime end = new DateTime(endDate.getTime());
        long duration = new Duration(start, end).getMillis();
        return Measure.valueOf(duration, SI.MILLI(SI.SECOND));
    }

    private String getPerUnit(ProfileItem pi) {
        List<ItemValue> itemValues = pi.getItemValues();
        for (ItemValue iv : itemValues) {
            if (iv.hasPerUnits()) {
                return iv.getPerUnit();
            }
        }
        return null;
    }
}