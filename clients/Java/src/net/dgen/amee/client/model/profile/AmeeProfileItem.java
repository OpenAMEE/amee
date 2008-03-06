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
import net.dgen.amee.client.model.base.AmeeConstants;
import net.dgen.amee.client.model.base.AmeeItem;
import net.dgen.amee.client.model.base.AmeeObject;
import net.dgen.amee.client.model.base.AmeeObjectReference;
import net.dgen.amee.client.model.base.AmeeObjectType;
import net.dgen.amee.client.model.data.AmeeDataItem;
import net.dgen.amee.client.service.AmeeObjectFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// TODO: code here to do with dates should be moved elsewhere

public class AmeeProfileItem extends AmeeItem implements Serializable {

    private AmeeObjectReference profileRef;
    private AmeeObjectReference dataItemRef;
    private Date validFrom = Calendar.getInstance().getTime();
    private Boolean end = false;
    private BigDecimal amountPerMonth = AmeeConstants.ZERO;

    public AmeeProfileItem() {
        super();
    }

    public AmeeProfileItem(AmeeObjectReference ref) {
        super(ref);
    }

    public AmeeProfileItem(String path, AmeeObjectType objectType) {
        super(path, objectType);
    }

    public void populate(AmeeProfileItem copy) {
        super.populate(copy);
        copy.setProfileRef(profileRef);
        copy.setDataItemRef(dataItemRef);
        copy.setValidFrom(validFrom);
        copy.setEnd(end);
        copy.setAmountPerMonth(amountPerMonth);
    }

    public AmeeObject getCopy() {
        AmeeProfileItem copy = new AmeeProfileItem();
        populate(copy);
        return copy;
    }

    public AmeeDataItem getDataItem() throws AmeeException {
        AmeeDataItem dataItem = null;
        if (getDataItemRef() != null) {
            dataItem = (AmeeDataItem) AmeeObjectFactory.getInstance().getObject(getDataItemRef());
        }
        return dataItem;
    }

    public void setParentRef() {
        setParentRef(new AmeeObjectReference(getObjectReference().getParentUri(), AmeeObjectType.PROFILE_CATEGORY));
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

    public AmeeObjectReference getDataItemRef() {
        return dataItemRef;
    }

    public void setDataItemRef(AmeeObjectReference dataItemRef) {
        if (dataItemRef != null) {
            this.dataItemRef = dataItemRef;
        }
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public String getValidFromFormatted() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(getValidFrom());
    }

    public void setValidFrom(Date validFrom) {
        if (validFrom != null) {
            this.validFrom = validFrom;
        } else {
            setValidFrom();
        }
    }

    public void setValidFrom(String validFromStr) {
        setValidFrom(getFullDate(validFromStr));
    }

    public void setValidFrom() {
        Calendar validFromCal = Calendar.getInstance();
        int year = validFromCal.get(Calendar.YEAR);
        int month = validFromCal.get(Calendar.MONTH);
        validFromCal.clear();
        validFromCal.set(year, month, 1); // first of the month
        setValidFrom(validFromCal.getTime());
    }

    public static Date getFullDate(String date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            try {
                return dateFormat.parse(date);
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public Boolean getEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public void setEnd(String endStr) {
        setEnd(Boolean.valueOf(endStr));
    }

    public BigDecimal getAmountPerMonth() {
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