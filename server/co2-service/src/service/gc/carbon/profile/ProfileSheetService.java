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

import com.jellymold.sheet.Cell;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.cache.CacheHelper;
import gc.carbon.data.DataCategory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Service
@Scope("prototype")
public class ProfileSheetService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ProfileSheetFactory profileSheetFactory;

    private CacheHelper cacheHelper = CacheHelper.getInstance();

    public ProfileSheetService() {
        super();
    }

    public Sheet getSheet(Profile profile, DataCategory dataCategory, Date profileDate) {
        profileSheetFactory.setProfile(profile);
        profileSheetFactory.setDataCategory(dataCategory);
        profileSheetFactory.setProfileDate(profileDate);
        return (Sheet) cacheHelper.getCacheable(profileSheetFactory);
    }

    public void removeSheets(Profile profile) {
        cacheHelper.clearCache("ProfileSheets", "ProfileSheet_" + profile.getUid());
    }

    public BigDecimal getTotalAmountPerMonth(Sheet sheet) {
        Cell endCell;
        BigDecimal totalAmountPerMonth = ProfileItem.ZERO;
        BigDecimal amountPerMonth;
        for (Row row : sheet.getRows()) {
            endCell = row.findCell("end");
            if (!endCell.getValueAsBoolean()) {
                try {
                    amountPerMonth = row.findCell("amountPerMonth").getValueAsBigDecimal();
                    amountPerMonth = amountPerMonth.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                    if (amountPerMonth.precision() > ProfileItem.PRECISION) {
                        log.warn("precision is too big: " + amountPerMonth);
                        // TODO: do something?
                    }
                } catch (Exception e) {
                    // swallow
                    log.warn("caught Exception: " + e);
                    amountPerMonth = ProfileItem.ZERO;
                }
                totalAmountPerMonth = totalAmountPerMonth.add(amountPerMonth);
            }
        }
        return totalAmountPerMonth;
    }
}