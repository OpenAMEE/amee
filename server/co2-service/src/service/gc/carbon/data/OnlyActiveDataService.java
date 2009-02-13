package gc.carbon.data;

import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.profile.StartEndDate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        DataItem.resetCachedLabels();
        final List<DataItem> dataItems = delegatee.getDataItems(dataCategory, startDate, endDate);
        log.debug("getDataItems() got DataItems");
        List<DataItem> requestedItems = new ArrayList<DataItem>();
        for (String type : getDistinctLabels(dataItems)) {
            List<DataItem> itemsByLabel = getItemsByLabel(dataItems, type);
            requestedItems.addAll(getActiveItems(itemsByLabel, startDate));
        }
        log.debug("getDataItems() end");
        return requestedItems;
    }

    private Set<String> getDistinctLabels(List<DataItem> dataItems) {
        Set<String> labels = new HashSet<String>();
        for (DataItem di : dataItems) {
            labels.add(di.getLabel());
        }
        return labels;
    }

    private List<DataItem> getItemsByLabel(List<DataItem> dataItems, final String label) {
        List<DataItem> selectedDataItems = new ArrayList<DataItem>(dataItems.size());
        CollectionUtils.select(dataItems, new Predicate() {
            public boolean evaluate(Object o) {
                DataItem di = (DataItem) o;
                return label.equals(di.getLabel());
            }
        }, selectedDataItems);
        return selectedDataItems;
    }

    private List<DataItem> getActiveItems(final List<DataItem> dataItems, final StartEndDate startDate) {
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