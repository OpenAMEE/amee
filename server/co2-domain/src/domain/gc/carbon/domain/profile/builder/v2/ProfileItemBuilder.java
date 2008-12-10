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
package gc.carbon.domain.profile.builder.v2;

import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.profile.builder.BuildableProfileItem;
import gc.carbon.domain.Builder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.measure.unit.Unit;
import javax.measure.unit.SI;
import javax.measure.unit.NonSI;
import javax.measure.quantity.Mass;
import java.math.BigDecimal;

public class ProfileItemBuilder implements Builder {

    private Unit<Mass> AMOUNT_RETURN_UNIT = SI.KILOGRAM;
    private Unit<javax.measure.quantity.Duration> AMOUNT_RETURN_PER_UNIT = NonSI.YEAR;

    private BuildableProfileItem item;
    private Unit returnUnit;

    public ProfileItemBuilder(BuildableProfileItem item, Unit returnUnit) {
        this.item = item;
        this.returnUnit = returnUnit;
    }

    public ProfileItemBuilder(BuildableProfileItem item) {
        this.item = item;
    }

    public void buildElement(JSONObject obj, boolean detailed) throws JSONException {
        obj.put("uid", item.getUid());
        obj.put("name", item.getDisplayName());
        JSONArray itemValues = new JSONArray();
        for (APIObject itemValue : item.getItemValues()) {
            itemValues.put(itemValue.getJSONObject(false));
        }
        obj.put("itemValues", itemValues);
        if (detailed) {
            obj.put("created", item.getCreated());
            obj.put("modified", item.getModified());
            obj.put("environment", item.getEnvironment().getIdentityJSONObject());
            obj.put("itemDefinition", item.getItemDefinition().getIdentityJSONObject());
            obj.put("dataCategory", item.getDataCategory().getIdentityJSONObject());
        }
    }

    public void buildElement(Document document, Element element, boolean detailed) {
        element.setAttribute("uid", item.getUid());
        element.appendChild(APIUtils.getElement(document, "Name", item.getDisplayName()));
        Element itemValuesElem = document.createElement("ItemValues");
        for (APIObject itemValue : item.getItemValues()) {
            itemValuesElem.appendChild(itemValue.getElement(document, false));
        }
        element.appendChild(itemValuesElem);
        if (detailed) {
            element.setAttribute("created", item.getCreated().toString());
            element.setAttribute("modified", item.getModified().toString());
            element.appendChild(item.getEnvironment().getIdentityElement(document));
            element.appendChild(item.getItemDefinition().getIdentityElement(document));
            element.appendChild(item.getDataCategory().getIdentityElement(document));
        }
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        buildElement(obj, detailed);

        JSONObject amount = new JSONObject();
        amount.put("value", getAmount(item));
        obj.put("amount",amount);

        obj.put("startDate", item.getStartDate().toString());
        obj.put("endDate", (item.getEndDate() != null) ? item.getEndDate().toString() : "");        
        obj.put("dataItem", item.getDataItem().getIdentityJSONObject());
        if (detailed) {
            obj.put("profile", item.getProfile().getIdentityJSONObject());
        }
        return obj;
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ProfileItem");
        buildElement(document, element, detailed);

        Element amount = document.createElement("Amount");
        amount.appendChild(APIUtils.getElement(document, "Value", getAmount(item)));
        element.appendChild(amount);

        element.appendChild(APIUtils.getElement(document, "StartDate", item.getStartDate().toString()));
            element.appendChild(APIUtils.getElement(document, "EndDate", (item.getEndDate() != null) ? item.getEndDate().toString() : ""));
        element.appendChild(item.getDataItem().getIdentityElement(document));
        if (detailed) {
            element.appendChild(item.getProfile().getIdentityElement(document));
        }
        return element;
    }

    private String getAmount(BuildableProfileItem item) {
        BigDecimal amount = item.getAmount();
        if (returnUnit != null) {

        }
        return amount.toString();
    }
    
}