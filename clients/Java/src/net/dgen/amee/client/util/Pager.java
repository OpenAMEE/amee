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
/**
 * This file is part of AMEE Java Client Library.
 *
 * AMEE Java Client Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE Java Client Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dgen.amee.client.util;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Forked from com.jellymold.utils.Pager. Should be merged back at some point.
 */
public class Pager implements Serializable {

    public static final int OUT_OF_RANGE = -1;

    // indexes are zero based
    private long start = 0; // the index of the first item on the current page
    private long from = 0; // the index + 1 of the first item on the current page - used in UI as the display start
    private long to = 0; // the index + 1 of the last item on the current page
    private long items = 0; // the number of items available over all pages
    private int currentPage = 1; // the current page
    private int requestedPage = 1; // the page requested by the view
    private int nextPage = Pager.OUT_OF_RANGE; // the next page in the sequence
    private int previousPage = Pager.OUT_OF_RANGE; // the previous page in the sequence
    private int lastPage = 1; // the last page in the sequence
    private int itemsPerPage = 1; // the amount of items per page
    private int itemsFound = 0; // the number of items found for the current page (can be less than itemsPerPage)

    public Pager() {
        super();
    }

    public Pager(long items, int itemsPerPage) {
        this();
        setItemsPerPage(itemsPerPage); // must be set before anything else
        setItems(items);
    }

    public Pager(long items, int itemsPerPage, int currentPage) {
        this(items, itemsPerPage);
        setCurrentPage(currentPage);
    }

    public String toString() {
        return "From " + getFrom() + " to " + getTo() + " currentPage " + getCurrentPage();
    }

    private void calculate() {
        setStart((getCurrentPage() - 1) * getItemsPerPage()); // zero based index
        setFrom(getStart() + 1);
        setTo(getStart() + getItemsPerPage());
        if (!isEmpty()) {
            setLastPage((int) (getItems() / getItemsPerPage()) + ((getItems() % getItemsPerPage()) == 0 ? 0 : 1));
        } else {
            setLastPage(1);
        }
        setNextPage(getCurrentPage() + 1);
        setPreviousPage(getCurrentPage() - 1);
    }

    public void goRequestedPage() {
        setCurrentPage(getRequestedPage());
    }

    public void goFirstPage() {
        setCurrentPage(1);
    }

    public void goLastPage() {
        setCurrentPage(getLastPage());
    }

    public void goNextPage() {
        setCurrentPage(getNextPage());
    }

    public void goPreviousPage() {
        setCurrentPage(getPreviousPage());
    }

    public boolean isAtFirstPage() {
        return getCurrentPage() == 1;
    }

    public boolean isAtLastPage() {
        return getCurrentPage() == getLastPage();
    }

    public boolean isEmpty() {
        return getItems() == 0;
    }

    public long getStart() {
        return start;
    }

    private void setStart(long start) {
        if ((start >= 0) && (start < getItems())) {
            this.start = start;
        } else {
            this.start = 0;
        }
    }

    public long getFrom() {
        return from;
    }

    private void setFrom(long from) {
        if (from > getItems()) {
            this.from = getItems();
        } else if (from < 0) {
            this.from = 0;
        } else {
            this.from = from;
        }
    }

    public long getTo() {
        return to;
    }

    private void setTo(long to) {
        if (to > getItems()) {
            this.to = getItems();
        } else if (to < 0) {
            this.to = 0;
        } else {
            this.to = to;
        }
    }

    public long getItems() {
        return items;
    }

    public void setItems(long items) {
        if (items >= 0) {
            this.items = items;
        } else {
            this.items = 0;
        }
        calculate();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        setRequestedPage(currentPage);
        if ((currentPage >= 1) && (currentPage <= getLastPage())) {
            this.currentPage = currentPage;
        } else {
            this.currentPage = 1;
        }
        calculate();
    }

    public int getRequestedPage() {
        return requestedPage;
    }

    private void setRequestedPage(int requestedPage) {
        this.requestedPage = requestedPage;
    }

    public int getNextPage() {
        return nextPage;
    }

    private void setNextPage(int nextPage) {
        if ((nextPage > 1) && (nextPage <= getLastPage())) {
            this.nextPage = nextPage;
        } else {
            this.nextPage = Pager.OUT_OF_RANGE;
        }
    }

    public int getPreviousPage() {
        return previousPage;
    }

    private void setPreviousPage(int previousPage) {
        if ((previousPage >= 1) && (previousPage < getLastPage())) {
            this.previousPage = previousPage;
        } else {
            this.previousPage = Pager.OUT_OF_RANGE;
        }
    }

    public int getLastPage() {
        return lastPage;
    }

    private void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    private void setItemsPerPage(int itemsPerPage) {
        if (itemsPerPage > 0) {
            this.itemsPerPage = itemsPerPage;
        } else {
            this.itemsPerPage = 1;
        }
    }

    public int getItemsFound() {
        return itemsFound;
    }

    public void setItemsFound(int itemsFound) {
        if (itemsFound > 0) {
            this.itemsFound = itemsFound;
        } else {
            this.itemsFound = 0;
        }
    }

    public Map getPageChoices() {
        Map<String, String> pageChoices = new LinkedHashMap<String, String>();
        for (int page = 1; page <= getLastPage(); page++) {
            pageChoices.put("" + page, "" + page);
        }
        return pageChoices;
    }
}