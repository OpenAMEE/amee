package com.amee.domain.profile;

import com.amee.core.ObjectType;
import com.amee.domain.Builder;
import com.amee.domain.data.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.math.BigDecimal;

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

@Entity
@DiscriminatorValue("PI")
public class ProfileItem extends Item {

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "PROFILE_ID")
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_ITEM_ID")
    private DataItem dataItem;

    @Column(name = "AMOUNT", precision = Decimal.PRECISION, scale = Decimal.SCALE)
    private BigDecimal amount = Decimal.ZERO;

    @Transient
    private Builder builder;

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

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public String toString() {
        return "ProfileItem_" + getUid();
    }

    @Transient
    public ProfileItem getCopy() {
        ProfileItem profileItem = new ProfileItem(getProfile(), getDataCategory(), getDataItem());
        profileItem.setStartDate(getStartDate());
        profileItem.setEndDate(getEndDate());
        profileItem.setAmount(getAmount());
        profileItem.setName(getName());
        profileItem.setCreated(getCreated());
        profileItem.setModified(getModified());
        profileItem.setUid(getUid());
        profileItem.setId(getId());
        return profileItem;
    }

    @Transient
    public String getPath() {
        return getUid();
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

    public boolean isEnd() {
        return (endDate != null) && (startDate.compareTo(endDate) == 0);
    }

    public CO2Amount getAmount() {
        return new CO2Amount(amount);
    }

    public void setAmount(CO2Amount amount) {
        this.amount = amount.getValue();
    }

    @Transient
    public void updateAmount(CO2Amount newAmount) {
        setAmount(newAmount);
    }

    @Override
    @Transient
    public JSONObject getJSONObject(boolean b) throws JSONException {
        return builder.getJSONObject(b);
    }

    public Element getElement(Document document, boolean b) {
        return builder.getElement(document, b);
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.PI;
    }

    public boolean hasNonZeroPerTimeValues() {
        for (ItemValue iv : getItemValues()) {
            if (iv.hasPerTimeUnit() && iv.isNonZero()) {
                return true;
            }
        }
        return false;
    }

    //TEMP HACK - will remove as soon we decide how to handle return units in V1 correctly.
    public boolean isSingleFlight() {
        for (ItemValue iv : getItemValues()) {
            if ((iv.getName().startsWith("IATA") && iv.getValue().length() > 0) ||
                (iv.getName().startsWith("Lat") && !iv.getValue().equals("-999")) ||
                (iv.getName().startsWith("Lon") && !iv.getValue().equals("-999"))) {
                return true;
            }

        }
        return false;
    }

    public boolean supportsCalculation() {
        return !getItemDefinition().getAlgorithms().isEmpty();
    }
}
