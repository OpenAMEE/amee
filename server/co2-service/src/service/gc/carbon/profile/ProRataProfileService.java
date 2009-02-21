package gc.carbon.profile;

import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.Decimal;
import gc.carbon.domain.data.CO2Amount;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.profile.Profile;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.measure.DecimalMeasure;
import javax.measure.unit.SI;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A ProfileService which prorates amounts belonging to the {@link gc.carbon.domain.profile.ProfileItem ProfileItem} instances
 * that are returned by the delegated ProfileService.
 * <p/>
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

    private Log log = LogFactory.getLog(getClass());

    private ProfileService delegatee;

    public ProRataProfileService(ProfileService delegatee) {
        this.delegatee = delegatee;
    }

    @SuppressWarnings(value="unchecked")
    public List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory,
                                             StartEndDate startDate, StartEndDate endDate) {

        List<ProfileItem> requestedItems = new ArrayList<ProfileItem>();

        Interval requestInterval = getInterval(startDate, endDate);

        for (ProfileItem pi : delegatee.getProfileItems(profile, dataCategory, startDate, endDate)) {

            if(log.isDebugEnabled())
                log.debug("getProfileItems() - ProfileItem: " + pi.getName() + " has un-prorated CO2 Amount: " + pi.getAmount());

            Interval intersect = requestInterval;

            // Find the intersection of the event with the requested window.
            if (intersect.getStart().toDate().before(pi.getStartDate())) {
                intersect = intersect.withStartMillis(pi.getStartDate().getTime());
            }

            if (pi.getEndDate() != null && pi.getEndDate().before(intersect.getEnd().toDate())) {
                    intersect = intersect.withEndMillis(pi.getEndDate().getTime());
            }

            if (log.isDebugEnabled())
                log.debug("getProfileItems() - request interval: " + requestInterval + ", intersect:" + intersect);

            if (pi.hasNonZeroPerTimeValues()) {
                // The ProfileItem has perTime ItemValues. In this case, the ItemValues are multiplied by
                // the (intersect/PerTime) ratio and the CO2 value recalculated.

                log.debug("getProfileItems() - ProfileItem: " + pi.getName() + " has PerTime ItemValues.");

                ProfileItem pic = pi.getCopy();
                for (ItemValue iv : pi.getItemValues()) {
                    ItemValue ivc = iv.getCopy();
                    if(ivc.hasPerTimeUnit() && ivc.getItemValueDefinition().isFromProfile() && ivc.getValue().length() > 0) {
                        pic.add(getProRatedItemValue(intersect, ivc));

                        if(log.isDebugEnabled())
                            log.debug("getProfileItems() - ProfileItem: " + pi.getName() +
                                    ". ItemValue: " + ivc.getName() + " = " + iv.getValue() + " has PerUnit: " + ivc.getPerUnit() +
                                    ". Pro-rated ItemValue = " + ivc.getValue());

                    } else {
                        log.debug("getProfileItems() - ProfileItem: " + pi.getName() + ". Unchanged ItemValue: " + ivc.getName());
                        pic.add(ivc);
                    }
                }

                delegatee.calculate(pic);

                if(log.isDebugEnabled())
                    log.debug("getProfileItems() - ProfileItem: " + pi.getName() + ". Adding prorated CO2 Amount: " + pic.getAmount());

                requestedItems.add(pic);
                
            } else if (pi.getEndDate() != null) {
                // The ProfileItem has no perTime ItemValues and is bounded. In this case, the CO2 value is multiplied by
                // the (intersection/event) ratio.

                ProfileItem pic = pi.getCopy();
                BigDecimal event = new BigDecimal(getIntervalInMillis(pic.getStartDate(), pic.getEndDate()));
                BigDecimal eventIntersectRatio = new BigDecimal(intersect.toDurationMillis()).divide(event, Decimal.CONTEXT);
                BigDecimal proratedAmount = pic.getAmount().getValue().multiply(eventIntersectRatio, Decimal.CONTEXT);
                pic.setAmount(new CO2Amount(proratedAmount));

                if(log.isDebugEnabled()) {
                    log.debug("getProfileItems() - ProfileItem: " + pi.getName() +
                            " is bounded (" + getInterval(pic.getStartDate(),  pic.getEndDate()) +
                            ") and has no PerTime ItemValues.");
                    log.debug("getProfileItems() - Adding pro-rated CO2 Amount: " + pic.getAmount());
                }
                requestedItems.add(pic);

            } else {
                // The ProfileItem has no perTime ItemValues and is unbounded. In this case, the CO2 is not prorated.
                if(log.isDebugEnabled())
                    log.debug("getProfileItems() - ProfileItem: " + pi.getName() +
                            " is unbounded and has no PerTime ItemValues. Adding un-prorated CO2 Amount: " + pi.getAmount());
                requestedItems.add(pi);
            }

        }

        return requestedItems;
    }

    private ItemValue getProRatedItemValue(Interval interval, ItemValue ivc) {
        BigDecimal perTime = DecimalMeasure.valueOf(new BigDecimal(1), ivc.getPerUnit().toUnit()).to(SI.MILLI(SI.SECOND)).getValue();
        BigDecimal intersectPerTimeRatio = new BigDecimal(interval.toDurationMillis()).divide(perTime, Decimal.CONTEXT);
        BigDecimal value = new BigDecimal(ivc.getValue());
        value = value.multiply(intersectPerTimeRatio, Decimal.CONTEXT);
        ivc.setValue(value.setScale(Decimal.SCALE, Decimal.ROUNDING_MODE).toString());
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