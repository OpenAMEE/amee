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
package net.dgen.amee.client.model.base;

import net.dgen.amee.client.AmeeException;
import net.dgen.amee.client.service.AmeeObjectFactory;
import net.dgen.amee.client.util.Pager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AmeeCategory extends AmeeObject implements Serializable {

    private AmeeObjectReference parentRef = null;
    private List<AmeeObjectReference> categoryRefs = new ArrayList<AmeeObjectReference>();
    private List<AmeeObjectReference> itemRefs = new ArrayList<AmeeObjectReference>();
    private int page = 1;
    private Pager itemsPager = new Pager(0, 10); // TODO: is this a reasonable default? this is also hard-coded elsewhere

    public AmeeCategory() {
        super();
    }

    public AmeeCategory(AmeeObjectReference ref) {
        super(ref);
        setParentRef();
    }

    public AmeeCategory(String path, AmeeObjectType objectType) {
        super(path, objectType);
        setParentRef();
    }

    public void populate(AmeeCategory copy) {
        super.populate(copy);
        copy.setParentRef(parentRef);
        copy.setCategoryRefs(new ArrayList<AmeeObjectReference>(categoryRefs));
        copy.setItemRefs(new ArrayList<AmeeObjectReference>(itemRefs));
        copy.setPage(page);
        copy.setItemsPager(new Pager(itemsPager.getItems(), itemsPager.getItemsPerPage(), itemsPager.getCurrentPage()));
    }

    public abstract void setParentRef();

    public abstract AmeeCategory getNewChildCategory(AmeeObjectReference ref);

    public abstract AmeeObjectType getChildCategoryObjectType();

    public abstract AmeeItem getNewChildItem(AmeeObjectReference ref);

    public abstract AmeeObjectType getChildItemObjectType();

    public String getNewUri() throws AmeeException {
        String uri = getUri();
        if ((uri == null) || (uri.length() == 0)) {
            throw new AmeeException("Could not create URI.");
        }
        // get rid of old params
        int pos = uri.indexOf("?");
        if (pos >= 0) {
            uri = uri.substring(0, pos);
        }
        // add new param
        uri = uri + "?page=" + page;
        // new uri is old path + new params
        return uri;
    }

    public void fetch() throws AmeeException {
        setObjectReference(new AmeeObjectReference(getNewUri(), this.getObjectType()));
        super.fetch();
    }

    public List<AmeeCategory> getCategories() throws AmeeException {
        AmeeObjectFactory ameeObjectFactory = AmeeObjectFactory.getInstance();
        List<AmeeCategory> categories = new ArrayList<AmeeCategory>();
        AmeeObject ameeObject;
        for (AmeeObjectReference ref : getCategoryRefs()) {
            ameeObject = ameeObjectFactory.getObject(ref);
            if (ameeObject != null) {
                categories.add((AmeeCategory) ameeObject);
            }
        }
        return categories;
    }

    public List<AmeeItem> getItems() throws AmeeException {
        AmeeObjectFactory ameeObjectFactory = AmeeObjectFactory.getInstance();
        List<AmeeItem> items = new ArrayList<AmeeItem>();
        AmeeObject ameeObject;
        for (AmeeObjectReference ref : getItemRefs()) {
            ameeObject = ameeObjectFactory.getObject(ref);
            if (ameeObject != null) {
                items.add((AmeeItem) ameeObject);
            }
        }
        return items;
    }

    public AmeeCategory getParent() throws AmeeException {
        AmeeCategory category = null;
        if (getParentRef() != null) {
            category = (AmeeCategory) AmeeObjectFactory.getInstance().getObject(getParentRef());
        }
        return category;
    }

    public AmeeObjectReference getParentRef() {
        return parentRef;
    }

    public void setParentRef(AmeeObjectReference parentRef) {
        this.parentRef = parentRef;
    }

    public List<AmeeObjectReference> getCategoryRefs() throws AmeeException {
        return getCategoryRefs(true);
    }

    public List<AmeeObjectReference> getCategoryRefs(boolean fetchIfNotFetched) throws AmeeException {
        if (fetchIfNotFetched && !isFetched()) {
            fetch();
        }
        return categoryRefs;
    }

    public void addCategoryRef(AmeeObjectReference ref) {
        categoryRefs.add(ref);
    }

    public void clearCategoryRefs() {
        categoryRefs.clear();
    }

    public void setCategoryRefs(List<AmeeObjectReference> categoryRefs) {
        if (categoryRefs != null) {
            this.categoryRefs = categoryRefs;
        }
    }

    public List<AmeeObjectReference> getItemRefs() throws AmeeException {
        return getItemRefs(true);
    }

    public List<AmeeObjectReference> getItemRefs(boolean fetchIfNotFetched) throws AmeeException {
        if (fetchIfNotFetched && !isFetched()) {
            fetch();
        }
        return itemRefs;
    }

    public void addItemRef(AmeeObjectReference ref) {
        itemRefs.add(ref);
    }

    public void clearItemRefs() {
        itemRefs.clear();
    }

    public void setItemRefs(List<AmeeObjectReference> itemRefs) {
        if (categoryRefs != null) {
            this.itemRefs = itemRefs;
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Pager getItemsPager() throws AmeeException {
        if (!isFetched()) {
            fetch();
        }
        return itemsPager;
    }

    public void setItemsPager(Pager itemsPager) {
        if (itemsPager != null) {
            this.itemsPager = itemsPager;
        }
    }
}