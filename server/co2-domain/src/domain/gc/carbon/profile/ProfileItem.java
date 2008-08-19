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
package gc.carbon.profile;

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.EngineUtils;
import gc.carbon.ObjectType;
import gc.carbon.data.DataCategory;
import gc.carbon.data.DataItem;
import gc.carbon.data.Item;
import org.hibernate.annotations.Index;
import org.jboss.seam.annotations.Name;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

@Entity
@Name("profileItem")
@DiscriminatorValue("PI")
public class ProfileItem extends Item {

    // 999,999,999,999,999,999.999
    public final static int PRECISION = 18;
    public final static int SCALE = 3;
    public final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public final static BigDecimal ZERO = BigDecimal.valueOf(0, SCALE);

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "PROFILE_ID")
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_ITEM_ID")
    private DataItem dataItem;

    @Column(name = "VALID_FROM")
    @Index(name = "VALID_FROM_IND")
    private Date validFrom = Calendar.getInstance().getTime();

    @Column(name = "END")
    private Boolean end = false;

    @Column(name = "AMOUNT_PER_MONTH", precision = PRECISION, scale = SCALE)
    private BigDecimal amountPerMonth = ZERO;

    public ProfileItem() {
        super();
    }

    public ProfileItem(Profile profile, DataItem dataItem) {
        super(dataItem.getDataCategory(), dataItem.getItemDefinition());
        setProfile(profile);
        setDataItem(dataItem);
    }

    public ProfileItem(Profile profile, DataCategory dataCategory, DataItem dataItem) {
        super(dataCategory, dataItem.getItemDefinition());
        setProfile(profile);
        setDataItem(dataItem);
    }

    public String toString() {
        return "ProfileItem_" + getUid();
    }

    @Transient
    public ProfileItem getCopy() {
        ProfileItem profileItem = new ProfileItem(getProfile(), getDataCategory(), getDataItem());
        profileItem.setValidFrom(getValidFrom());
        profileItem.setEnd(isEnd());
        profileItem.setAmountPerMonth(getAmountPerMonth());
        profileItem.setName(getName());
        profileItem.setCreated(getCreated());
        profileItem.setModified(getModified());
        profileItem.setUid(getUid());
        profileItem.setId(getId());
        return profileItem;
    }

    @Transient
    public void updateAmountPerMonth(BigDecimal newAmountPerMonth) {
        setAmountPerMonth(newAmountPerMonth);
    }

    @Transient
    public void addToAmountPerMonth(BigDecimal difference) {
        updateAmountPerMonth(getAmountPerMonth().add(difference));
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        buildElement(obj, detailed);
        obj.put("amountPerMonth", getAmountPerMonth());
        obj.put("validFrom", getValidFromFormatted());
        obj.put("end", Boolean.toString(isEnd()));
        obj.put("dataItem", getDataItem().getIdentityJSONObject());
        if (detailed) {
            obj.put("profile", getProfile().getIdentityJSONObject());
        }
        return obj;
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ProfileItem");
        buildElement(document, element, detailed);
        element.appendChild(APIUtils.getElement(document, "AmountPerMonth", getAmountPerMonth().toString()));
        element.appendChild(APIUtils.getElement(document, "ValidFrom", getValidFromFormatted()));
        element.appendChild(APIUtils.getElement(document, "End", Boolean.toString(isEnd())));
        element.appendChild(getDataItem().getIdentityElement(document));
        if (detailed) {
            element.appendChild(getProfile().getIdentityElement(document));
        }
        return element;
    }

    @Transient
    public void setValidFrom(String validFromStr) {
        // TODO: logic to use profileDatePrecision (MONTH, DATE, etc)
        setValidFrom(EngineUtils.getFullDate(validFromStr));
    }

    /**
     * Set validFrom to *now* but with MONTH precision (1st of the month)
     * <p/>
     * TODO: logic to use profileDatePrecision (MONTH, DATE, etc)
     * TODO: hard coded to MONTH for now
     */
    @Transient
    public void setValidFrom() {
        Calendar validFromCal = Calendar.getInstance();
        int year = validFromCal.get(Calendar.YEAR);
        int month = validFromCal.get(Calendar.MONTH);
        validFromCal.clear();
        validFromCal.set(year, month, 1); // first of the month
        setValidFrom(validFromCal.getTime());
    }

    @Transient
    public String getValidFromFormatted() {
        return EngineUtils.getFullDate(getValidFrom());
    }

    @Transient
    public void setEnd(String endStr) {
        setEnd(Boolean.valueOf(endStr));
    }

    @Transient
    public String getPath() {
        return getUid();
    }

    @Transient
    public String getDisplayPath() {
        return EngineUtils.getDisplayPath(this);
    }

    @Transient
    public String getDisplayName() {
        return EngineUtils.getDisplayName(this);
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        if (dataItem != null) {
            this.dataItem = dataItem;
        }
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        if (validFrom != null) {
            this.validFrom = validFrom;
        } else {
            setValidFrom();
        }
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public BigDecimal getAmountPerMonth() {
        return amountPerMonth;
    }

    public void setAmountPerMonth(BigDecimal amountPerMonth) {
        if (amountPerMonth == null) {
            amountPerMonth = ZERO;
        }
        this.amountPerMonth = amountPerMonth;
    }

    @Transient
    public String getType() {
        return ObjectType.PI.toString();
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.PI;
    }
}