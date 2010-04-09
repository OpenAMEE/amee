/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
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
package com.amee.domain.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A Map of {@link ItemValue} instances.
 * <p/>
 * The keys will be the {@link ItemValue} paths. The entries will be a Set of {@link ItemValue} instances. The Set will
 * consist of a single entry for single-valued {@link ItemValue} histories.
 */
@SuppressWarnings("unchecked")
public class ItemValueMap extends HashMap {

    Log log = LogFactory.getLog(getClass());

    /**
     * Get the head {@link ItemValue} in the historical sequence.
     *
     * @param path - the {@link ItemValue} path.
     * @return the head {@link ItemValue} in the historical sequence.
     */
    public ItemValue get(String path) {
        ItemValue itemValue = null;
        TreeSet<ItemValue> series = (TreeSet<ItemValue>) super.get(path);
        if (series != null) {
            itemValue = series.first();
        }
        return itemValue;
    }

    /**
     * Get the list of active {@link ItemValue}s at the passed start Date.
     *
     * @param startDate - the active {@link ItemValue} will be that starting immediately prior-to or on this date.
     * @return the set of active {@link ItemValue}s at the passed start Date.
     */
    public List<ItemValue> getAll(Date startDate) {
        List<ItemValue> itemValues = new ArrayList();
        for (Object path : super.keySet()) {
            ItemValue itemValue = get((String) path, startDate);
            if (itemValue != null) {
                itemValues.add(itemValue);
            } else {
                log.warn("getAll() Got null ItemValue: path=" + path + ", startDate=" + startDate);
            }
        }
        return itemValues;
    }

    /**
     * Get all instances of {@link ItemValue} with the passed path.
     *
     * @param path - the {@link ItemValue} path.
     * @return the List of {@link ItemValue}. Will be empty is there exists no {@link ItemValue}s with this path.
     */
    public List<ItemValue> getAll(String path) {
/*
        if (log.isDebugEnabled()) {
            ArrayList<ItemValue> v = new ArrayList((TreeSet<ItemValue>) super.get(path));
            String[] a = new String[v.size()];
            String name = v.get(0).getItem().getDisplayName();
            for(int i = 0; i < a.length; i++) {
                a[i] = v.get(i).getStartDate() + " : " + v.get(i).getValue();
            }
            log.debug("getAll() - Item: " + name + " - all ItemValues for path: " + path + " => " + Arrays.toString(a));

        }
*/
        return new ArrayList((TreeSet<ItemValue>) super.get(path));
    }

    /**
     * Get the active {@link ItemValue} at the passed start Date.
     *
     * @param path      - the {@link ItemValue} path.
     * @param startDate - the active {@link ItemValue} will be that starting immediately prior-to or on this date.
     * @return the active {@link ItemValue} at the passed start Date.
     */
    public ItemValue get(String path, Date startDate) {
        ItemValue itemValue = null;
        Set<ItemValue> series = (TreeSet<ItemValue>) super.get(path);
        if (series != null) {
            itemValue = find(series, startDate);
            //if (log.isDebugEnabled())
            //    log.debug("get() - Item: " + itemValue.getItem().getDisplayName() + " - ItemValue for path: " + path + " => " +
            //            itemValue.getStartDate() + " : " + itemValue.getValue());
        }
        return itemValue;
    }

    public void put(String path, ItemValue itemValue) {
        if (!containsKey(path)) {
            super.put(path, new TreeSet<ItemValue>(new Comparator<ItemValue>() {
                public int compare(ItemValue iv1, ItemValue iv2) {
                    return iv2.getStartDate().compareTo(iv1.getStartDate());
                }
            }));
        }

        Set<ItemValue> itemValues = (Set<ItemValue>) super.get(path);
        itemValues.add(itemValue);
    }

    /**
     * Find the active ItemValue at startDate. The active ItemValue is the one occurring at or
     * immediately before startDate.
     *
     * @param itemValues
     * @param startDate
     * @return the discovered ItemValue, or null if not found
     */
    private ItemValue find(Set<ItemValue> itemValues, Date startDate) {
        ItemValue selected = null;
        for (ItemValue itemValue : itemValues) {
            if (!itemValue.getStartDate().after(startDate)) {
                selected = itemValue;
                break;
            }
        }
        return selected;
    }
}
