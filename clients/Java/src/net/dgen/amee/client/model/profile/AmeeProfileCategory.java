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
package net.dgen.amee.client.model.profile;

import net.dgen.amee.client.AmeeException;
import net.dgen.amee.client.model.base.AmeeCategory;
import net.dgen.amee.client.model.base.AmeeConstants;
import net.dgen.amee.client.model.base.AmeeItem;
import net.dgen.amee.client.model.base.AmeeObject;
import net.dgen.amee.client.model.base.AmeeObjectReference;
import net.dgen.amee.client.model.base.AmeeObjectType;
import net.dgen.amee.client.model.data.AmeeDataItem;
import net.dgen.amee.client.service.AmeeObjectFactory;
import net.dgen.amee.client.util.Choice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AmeeProfileCategory extends AmeeCategory implements Serializable {

    private AmeeObjectReference profileRef;
    private AmeeObjectReference dataCategoryRef;
    private BigDecimal amountPerMonth = AmeeConstants.ZERO;

    public AmeeProfileCategory() {
        super();
    }

    public AmeeProfileCategory(AmeeObjectReference ref) {
        super(ref);
    }

    public AmeeProfileCategory(String path, AmeeObjectType objectType) {
        super(path, objectType);
    }

    public void populate(AmeeProfileCategory copy) {
        super.populate(copy);
        copy.setProfileRef(profileRef);
        copy.setDataCategoryRef(dataCategoryRef);
        copy.setAmountPerMonth(amountPerMonth);
    }

    public AmeeObject getCopy() {
        AmeeProfileCategory copy = new AmeeProfileCategory();
        populate(copy);
        return copy;
    }

    public void setParentRef() {
        String parentUri = getObjectReference().getParentUri();
        if (parentUri.equals(getProfileRef().getUri())) {
            setParentRef(getProfileRef());
        } else {
            setParentRef(new AmeeObjectReference(parentUri, AmeeObjectType.PROFILE_CATEGORY));
        }
    }

    public AmeeCategory getNewChildCategory(AmeeObjectReference ref) {
        return new AmeeProfileCategory(ref);
    }

    public AmeeObjectType getChildCategoryObjectType() {
        return AmeeObjectType.PROFILE_CATEGORY;
    }

    public AmeeItem getNewChildItem(AmeeObjectReference ref) {
        return new AmeeProfileItem(ref);
    }

    public AmeeObjectType getChildItemObjectType() {
        return AmeeObjectType.PROFILE_ITEM;
    }

    public List<AmeeProfileCategory> getProfileCategories() throws AmeeException {
        List<AmeeProfileCategory> profileCategories = new ArrayList<AmeeProfileCategory>();
        for (AmeeCategory category : super.getCategories()) {
            profileCategories.add((AmeeProfileCategory) category);
        }
        return profileCategories;
    }

    public List<AmeeProfileItem> getProfileItems() throws AmeeException {
        List<AmeeProfileItem> profileItems = new ArrayList<AmeeProfileItem>();
        for (AmeeItem item : super.getItems()) {
            profileItems.add((AmeeProfileItem) item);
        }
        return profileItems;
    }

    /**
     * Add a new AmeeProfileItem to this AmeeProfileCategory.
     *
     * @param dataItemUid the UID of the AmeeDataItem to base the new AmeeProfileItem on
     * @return the new AmeeProfileItem
     * @throws AmeeException
     */
    public AmeeProfileItem addProfileItem(String dataItemUid) throws AmeeException {
        return AmeeObjectFactory.getInstance().getProfileItem(this, dataItemUid, null);
    }

    /**
     * Add a new Profile Item to this Profile Category.
     *
     * @param dataItem the AmeeDataItem to base the new AmeeProfileItem on
     * @return the new AmeeProfileItem
     * @throws AmeeException
     */
    public AmeeProfileItem addProfileItem(AmeeDataItem dataItem) throws AmeeException {
        return addProfileItem(dataItem.getUid());
    }

    /**
     * Add a new Profile Item to this Profile Category.
     *
     * @param dataItemUid the UID of the AmeeDataItem to base the new AmeeProfileItem on
     * @param values
     * @return the new AmeeProfileItem
     * @throws AmeeException
     */
    public AmeeProfileItem addProfileItem(String dataItemUid, List<Choice> values) throws AmeeException {
        return AmeeObjectFactory.getInstance().getProfileItem(this, dataItemUid, values);
    }

    /**
     * Add a new Profile Item to this Profile Category.
     *
     * @param dataItem the AmeeDataItem to base the new AmeeProfileItem on
     * @param values
     * @return the new AmeeProfileItem
     * @throws AmeeException
     */
    public AmeeProfileItem addProfileItem(AmeeDataItem dataItem, List<Choice> values) throws AmeeException {
        return addProfileItem(dataItem.getUid(), values);
    }

    public AmeeObjectReference getProfileRef() {
        if (profileRef == null) {
            setProfileRef();
        }
        return profileRef;
    }

    public void setProfileRef(AmeeObjectReference profileRef) {
        if (profileRef != null) {
            this.profileRef = profileRef;
        }
    }

    public void setProfileRef() {
        String ref = getObjectReference().getUriFirstTwoParts();
        if (ref != null) {
            setProfileRef(new AmeeObjectReference(ref, AmeeObjectType.PROFILE));
        }
    }

    public AmeeObjectReference getDataCategoryRef() {
        if (dataCategoryRef == null) {
            setDataCategoryRef();
        }
        return dataCategoryRef;
    }

    public void setDataCategoryRef(AmeeObjectReference dataCategoryRef) {
        if (dataCategoryRef != null) {
            this.dataCategoryRef = dataCategoryRef;
        }
    }

    public void setDataCategoryRef() {
        setProfileRef(
                new AmeeObjectReference(
                        "/data/" + getObjectReference().getUriExceptFirstTwoParts(),
                        AmeeObjectType.DATA_CATEGORY));
    }

    public BigDecimal getAmountPerMonth() throws AmeeException {
        if (!isFetched()) {
            fetch();
        }
        return amountPerMonth;
    }

    public void setAmountPerMonth(BigDecimal amountPerMonth) {
        if (amountPerMonth != null) {
            this.amountPerMonth = amountPerMonth;
        }
    }

    public void setAmountPerMonth(String amountPerMonth) {
        if (amountPerMonth != null) {
            BigDecimal newAmountPerMonth = new BigDecimal(amountPerMonth);
            newAmountPerMonth = newAmountPerMonth.setScale(AmeeConstants.SCALE, AmeeConstants.ROUNDING_MODE);
            if (newAmountPerMonth.precision() > AmeeConstants.PRECISION) {
                // TODO: do something
            }
            setAmountPerMonth(newAmountPerMonth);
        }
    }
}