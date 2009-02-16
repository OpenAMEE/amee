package gc.carbon.data;

import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.profile.StartEndDate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

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
public class OnlyActiveDataService extends DataService {

    private final Log log = LogFactory.getLog(getClass());

    private DataService delegatee;

    public OnlyActiveDataService(DataService delegatee) {
        this.delegatee = delegatee;
    }

    public List<DataItem> getDataItems(final DataCategory dataCategory, final StartEndDate startDate, final StartEndDate endDate) {
        log.debug("getDataItems() start");
        final List<DataItem> dataItems = delegatee.getDataItems(dataCategory, startDate, endDate);
        log.debug("getDataItems() got DataItems");
        Map<String, Set<DataItem>> labelsToDataItems = getLabelsToDataItems(dataItems);
        log.debug("getDataItems() got DataItems labels");
        List<DataItem> requestedItems = new ArrayList<DataItem>();
        for (String label : labelsToDataItems.keySet()) {
            requestedItems.addAll(getActiveItems(labelsToDataItems.get(label), startDate));
        }
        log.debug("getDataItems() end");
        return requestedItems;
    }

    private Map<String, Set<DataItem>> getLabelsToDataItems(List<DataItem> dataItems) {
        String label;
        Set<DataItem> dataItemSet;
        Map<String, Set<DataItem>> labelsToDataItems = new HashMap<String, Set<DataItem>>();
        for (DataItem di : dataItems) {
            label = di.getLabel();
            dataItemSet = labelsToDataItems.get(label);
            if (dataItemSet == null) {
                dataItemSet = new HashSet<DataItem>();
                labelsToDataItems.put(label, dataItemSet);
            }
            dataItemSet.add(di);
        }
        return labelsToDataItems;
    }

    private List<DataItem> getActiveItems(final Set<DataItem> dataItems, final StartEndDate startDate) {
        return (List) CollectionUtils.select(dataItems, new Predicate() {
            public boolean evaluate(Object o) {
                DataItem di = (DataItem) o;
                for (DataItem dataItem : dataItems) {
                    if (di.getStartDate().before(dataItem.getStartDate()) &&
                            !dataItem.getStartDate().after(startDate.toDate())) {
                        return false;
                    }
                }
                return true;
            }
        });
    }
}