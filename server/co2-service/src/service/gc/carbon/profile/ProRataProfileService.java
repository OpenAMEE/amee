package gc.carbon.profile;

import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.profile.ProfileItem;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A ProfileService which prorates amounts belonging to the {@link gc.carbon.domain.profile.ProfileItem ProfileItem} instances
 * that are returned by the delegated ProfileService.
 * <p/>
 * The protated duration is specified by the ProfileBrowser passed to {@link #getProfileItems(gc.carbon.profile.ProfileBrowser profileBrowser)}
 * <p/>
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
 * <p/>
 * Website http://www.amee.cc
 */
public class ProRataProfileService extends ProfileService {

    private ProfileService delegatee;

    public ProRataProfileService(ProfileService delegatee) {
        this.delegatee = delegatee;
    }

    @SuppressWarnings(value="unchecked")
    public List<ProfileItem> getProfileItems(ProfileBrowser profileBrowser) {

        List<ProfileItem> requestedItems = new ArrayList<ProfileItem>();

        Interval requestInterval = getInterval(profileBrowser.getStartDate(), profileBrowser.getEndDate());

        for (ProfileItem pi : delegatee.getProfileItems(profileBrowser)) {

            Interval interval = requestInterval;

            if (pi.hasPerUnits()) {

                ProfileItem pic = pi.getCopy();

                if (interval.getStart().toDate().before(pi.getStartDate())) {
                    interval = interval.withStartMillis(pi.getStartDate().getTime());
                }

                if (pi.getEndDate() != null) {
                    if (pi.getEndDate().before(interval.getEnd().toDate())) {
                        interval = interval.withEndMillis(pi.getEndDate().getTime());
                    }
                }

                for (ItemValue iv : pi.getItemValues()) {
                    if(iv.hasPerUnits() && iv.getValue().length() > 0) {
                        pic.add(getProRatedItemValue(interval, iv));
                    } else {
                        pic.add(iv.getCopy());
                    }
                }

                delegatee.calculate(pic);

                requestedItems.add(pic);
                
            } else if (pi.getEndDate() != null) {

                ProfileItem pic = pi.getCopy();
                BigDecimal requestedInterval = new BigDecimal(interval.toDurationMillis());
                BigDecimal itemInterval = new BigDecimal(getIntervalInMillis(pic.getStartDate(), pic.getEndDate()));
                BigDecimal itemIntervalBasedToRequestInterval = itemInterval.divide(requestedInterval, ProfileItem.CONTEXT);
                pic.setAmount(pic.getAmount().multiply(itemIntervalBasedToRequestInterval, ProfileItem.CONTEXT));

                requestedItems.add(pic);

            } else {
                requestedItems.add(pi);
            }

        }

        return requestedItems;
    }

    private ItemValue getProRatedItemValue(Interval interval, ItemValue iv) {
        ItemValue ivc = iv.getCopy();
        Measure<Long, Duration> intercept = Measure.valueOf(interval.toDurationMillis(), SI.MILLI(SI.SECOND));
        BigDecimal interceptBasedToPerUnit = new BigDecimal(intercept.doubleValue(ivc.getPerUnit().toUnit()));
        BigDecimal value = new BigDecimal(ivc.getValue());
        value = value.multiply(interceptBasedToPerUnit, ProfileItem.CONTEXT);
        ivc.setValue(value.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE).toString());
        return ivc;
    }

    private Interval getInterval(Date startDate, Date endDate) {
        DateTime start = new DateTime(startDate.getTime());
        DateTime end = new DateTime(endDate.getTime());
        return new Interval(start, end);
    }

    private long getIntervalInMillis(Date startDate, Date endDate) {
        return getInterval(startDate, endDate).toDurationMillis();
    }

}