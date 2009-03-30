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

import com.amee.domain.core.CO2AmountUnit;
import com.amee.domain.profile.ProfileDate;
import com.amee.service.BaseBrowser;
import com.amee.service.auth.ResourceActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component("profileBrowser")
@Scope("prototype")
public class ProfileBrowser extends BaseBrowser {

    @Autowired
    @Qualifier("profileActions")
    private ResourceActions profileActions;

    @Autowired
    @Qualifier("profileCategoryActions")
    private ResourceActions profileCategoryActions;

    @Autowired
    @Qualifier("profileItemActions")
    private ResourceActions profileItemActions;

    @Autowired
    @Qualifier("profileItemValueActions")
    private ResourceActions profileItemValueActions;

    // ProfileDate (API v1)
    private Date profileDate = new ProfileDate();

    // Return Unit
    private CO2AmountUnit co2AmountUnit = CO2AmountUnit.DEFAULT;

    // Filters
    private String selectBy;
    private String mode;

    public ProfileBrowser() {
        super();
    }

    // Actions
    public ResourceActions getProfileActions() {
        return profileActions;
    }

    public ResourceActions getProfileCategoryActions() {
        return profileCategoryActions;
    }

    public ResourceActions getProfileItemActions() {
        return profileItemActions;
    }

    public ResourceActions getProfileItemValueActions() {
        return profileItemValueActions;
    }

    public void setProfileDate(String profileDate) {
        this.profileDate = new ProfileDate(profileDate);
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
            endDate = getStartDate().plus(duration);
        }
    }

    public boolean isProRataRequest() {
        return getMode() != null && getMode().equals("prorata");
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
