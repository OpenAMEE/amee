package com.amee.domain;

import com.amee.base.utils.XMLUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Pager implements APIObject, Serializable {

    public static final int OUT_OF_RANGE = -1;

    // indexes are zero based
    private long start; // the index of the first item on the current page
    private long from; // the index + 1 of the first item on the current page - used in UI as the display start
    private long to; // the index + 1 of the last item on the current page
    private long items; // the number of items available over all pages
    private int currentPage; // the current page
    private int requestedPage; // the page requested by the view
    private int nextPage; // the next page in the sequence
    private int previousPage; // the previous page in the sequence
    private int lastPage; // the last page in the sequence
    private int itemsPerPage; // the amount of items per page
    private int itemsFound; // the number of items found for the current page (can be less than itemsPerPage)
    private PagerSetType pagerSetType;
    private Set<Object> pagerSet;

    private Pager() {
        super();
        reset();
    }

    public Pager(long items, int itemsPerPage, int currentPage) {
        this();
        reset(items, itemsPerPage, currentPage);
    }

    public void reset(long items, int itemsPerPage) {
        reset();
        setItemsPerPage(itemsPerPage); // must be set before anything else
        setItems(items);
    }

    public void reset(long items, int itemsPerPage, int currentPage) {
        reset(items, itemsPerPage);
        setCurrentPage(currentPage);
    }

    private void reset() {
        start = 0;
        from = 0;
        to = 0;
        items = 0;
        currentPage = 1;
        requestedPage = 1;
        nextPage = Pager.OUT_OF_RANGE;
        previousPage = Pager.OUT_OF_RANGE;
        lastPage = 1;
        itemsPerPage = 1;
        itemsFound = 0;
        pagerSetType = PagerSetType.ALL;
        pagerSet = new HashSet<Object>();
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

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("start", getStart());
        obj.put("from", getFrom());
        obj.put("to", getTo());
        obj.put("items", getItems());
        obj.put("currentPage", getCurrentPage());
        obj.put("requestedPage", getRequestedPage());
        obj.put("nextPage", getNextPage());
        obj.put("previousPage", getPreviousPage());
        obj.put("lastPage", getLastPage());
        obj.put("itemsPerPage", getItemsPerPage());
        obj.put("itemsFound", getItemsFound());
        return obj;
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return getJSONObject();
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return new JSONObject();
    }

    public Element getElement(Document document) {
        Element element = document.createElement("Pager");
        element.appendChild(XMLUtils.getElement(document, "Start", getStart() + ""));
        element.appendChild(XMLUtils.getElement(document, "From", getFrom() + ""));
        element.appendChild(XMLUtils.getElement(document, "To", getTo() + ""));
        element.appendChild(XMLUtils.getElement(document, "Items", getItems() + ""));
        element.appendChild(XMLUtils.getElement(document, "CurrentPage", getCurrentPage() + ""));
        element.appendChild(XMLUtils.getElement(document, "RequestedPage", getRequestedPage() + ""));
        element.appendChild(XMLUtils.getElement(document, "NextPage", getNextPage() + ""));
        element.appendChild(XMLUtils.getElement(document, "PreviousPage", getPreviousPage() + ""));
        element.appendChild(XMLUtils.getElement(document, "LastPage", getLastPage() + ""));
        element.appendChild(XMLUtils.getElement(document, "ItemsPerPage", getItemsPerPage() + ""));
        element.appendChild(XMLUtils.getElement(document, "ItemsFound", getItemsFound() + ""));
        return element;
    }

    public Element getElement(Document document, boolean detailed) {
        return getElement(document);
    }

    public Element getIdentityElement(Document document) {
        return document.createElement("Pager");
    }

    public PagerSetType getPagerSetType() {
        return pagerSetType;
    }

    public void setPagerSetType(PagerSetType pagerSetType) {
        this.pagerSetType = pagerSetType;
    }

    public Set<Object> getPagerSet() {
        return pagerSet;
    }

    public void setPagerSet(Set<Object> pagerSet) {
        if (pagerSet != null) {
            this.pagerSet = pagerSet;
        }
    }

    public boolean isPagerSetApplicable() {
        return !getPagerSetType().equals(PagerSetType.ALL) && (getPagerSet().size() > 0);
    }
}