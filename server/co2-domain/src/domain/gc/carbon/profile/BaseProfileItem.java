package gc.carbon.profile;

import gc.carbon.data.Item;
import gc.carbon.data.DataCategory;
import gc.carbon.data.ItemDefinition;
import gc.carbon.data.DataItem;
import gc.carbon.EngineUtils;
import gc.carbon.ObjectType;

import javax.persistence.*;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Calendar;

import org.hibernate.annotations.Index;
import org.jboss.seam.annotations.Name;

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

@MappedSuperclass
public abstract class BaseProfileItem extends Item {

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

    @Column(name = "START_DATE")
    @Index(name = "START_DATE_IND")
    private Date startDate = Calendar.getInstance().getTime();

    @Column(name = "END_DATE")
    @Index(name = "END_DATE_IND")
    private Date endDate;

    @Column(name = "END")
    private Boolean end = false;

    @Column(name = "AMOUNT", precision = PRECISION, scale = SCALE)
    private BigDecimal amountPerMonth = ZERO;

    public BaseProfileItem() {
      super();
    }
    
    public BaseProfileItem(Profile profile, DataItem dataItem) {
        super(dataItem.getDataCategory(), dataItem.getItemDefinition());
        setProfile(profile);
        setDataItem(dataItem);
    }

    public BaseProfileItem(Profile profile, DataCategory dataCategory, DataItem dataItem) {
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
        profileItem.setStartDate(getStartDate());
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

}
