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
package com.amee.service.profile;

import com.amee.platform.science.CO2AmountUnit;
import com.amee.domain.profile.MonthDate;
import com.amee.service.BaseBrowser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component("profileBrowser")
@Scope("prototype")
public class ProfileBrowser extends BaseBrowser {

    // ProfileDate (API v1)
    private Date profileDate = new MonthDate();

    // Return Unit
    private CO2AmountUnit co2AmountUnit = CO2AmountUnit.DEFAULT;

    // Filters
    private String selectBy;
    private String mode;

    public ProfileBrowser() {
        super();
    }

    public void setProfileDate(String profileDate) {
        this.profileDate = new MonthDate(profileDate);
    }

    public Date getProfileDate() {
        return profileDate;
    }

    public void setSelectBy(String selectBy) {
        this.selectBy = selectBy;
    }

    public String getSelectBy() {
        return selectBy;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setDuration(String duration) {
        if ((duration != null) && (endDate == null)) {
            endDate = getQueryStartDate().plus(duration);
        }
    }

    public boolean isProRataRequest() {
        return (getMode() != null) && getMode().equals("prorata");
    }

    public boolean isSelectByRequest() {
        return getSelectBy() != null;
    }

    public CO2AmountUnit getCo2AmountUnit() {
        return co2AmountUnit;
    }

    public boolean requestedCO2InExternalUnit() {
        return co2AmountUnit.isExternal();
    }

    public void setCO2AmountUnit(CO2AmountUnit co2AmountUnit) {
        this.co2AmountUnit = co2AmountUnit;
    }

}
