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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Entity
@Name("profileItem")
@DiscriminatorValue("PI")
public class ProfileItem extends BaseProfileItem {

    private static final String DAY_DATE = "yyyyMMdd";
    private static DateFormat DAY_DATE_FMT = new SimpleDateFormat(DAY_DATE);

    public ProfileItem() {
        super();
    }

    public ProfileItem(Profile profile, DataItem dataItem) {
        super(profile, dataItem);
    }

    public ProfileItem(Profile profile, DataCategory dataCategory, DataItem dataItem) {
        super(profile, dataCategory, dataItem);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        buildElement(obj, detailed);
        obj.put("amountPerMonth", getAmountPerMonth());
        obj.put("validFrom", DAY_DATE_FMT.format(getStartDate()));
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
        element.appendChild(APIUtils.getElement(document, "ValidFrom", DAY_DATE_FMT.format(getStartDate())));
        element.appendChild(APIUtils.getElement(document, "End", Boolean.toString(isEnd())));
        element.appendChild(getDataItem().getIdentityElement(document));
        if (detailed) {
            element.appendChild(getProfile().getIdentityElement(document));
        }
        return element;
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