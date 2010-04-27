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
package com.amee.domain.data.builder.v2;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.Builder;
import com.amee.domain.TimeZoneHolder;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.platform.science.StartEndDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ItemValueInListBuilder implements Builder {

    private ItemValue itemValue;

    public ItemValueInListBuilder(ItemValue itemValue) {
        this.itemValue = itemValue;
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        // Meta.
        obj.put("uid", itemValue.getUid());
        obj.put("created", itemValue.getCreated());
        obj.put("modified", itemValue.getModified());
        // Data.
        obj.put("path", itemValue.getPath());
        obj.put("name", itemValue.getName());
        obj.put("value", itemValue.getValue());
        obj.put("unit", itemValue.getUnit());
        obj.put("perUnit", itemValue.getPerUnit());
        obj.put("startDate", StartEndDate.getLocalStartEndDate(itemValue.getStartDate(), TimeZoneHolder.getTimeZone()).toString());
        // Related entities.
        obj.put("itemValueDefinition", getItemValueDefinitionJSONObject(itemValue.getItemValueDefinition()));
        return obj;
    }

    protected JSONObject getItemValueDefinitionJSONObject(ItemValueDefinition itemValueDefinition) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("valueDefinition", itemValueDefinition.getValueDefinition().getJSONObject());
        return obj;
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ItemValue");
        // Meta.
        element.setAttribute("uid", itemValue.getUid());
        element.setAttribute("created", itemValue.getCreated().toString());
        element.setAttribute("modified", itemValue.getModified().toString());
        // Data.
        element.appendChild(XMLUtils.getElement(document, "Path", itemValue.getPath()));
        element.appendChild(XMLUtils.getElement(document, "Name", itemValue.getName()));
        element.appendChild(XMLUtils.getElement(document, "Value", itemValue.getValue()));
        element.appendChild(XMLUtils.getElement(document, "Unit", itemValue.getUnit().toString()));
        element.appendChild(XMLUtils.getElement(document, "PerUnit", itemValue.getPerUnit().toString()));
        element.appendChild(XMLUtils.getElement(document, "StartDate",
                StartEndDate.getLocalStartEndDate(itemValue.getStartDate(), TimeZoneHolder.getTimeZone()).toString()));
        // Related entities.
        element.appendChild(getItemValueDefinitionElement(document, itemValue.getItemValueDefinition()));
        return element;
    }

    protected Element getItemValueDefinitionElement(Document document, ItemValueDefinition itemValueDefinition) {
        Element element = document.createElement("ItemValueDefinition");
        element.appendChild(itemValueDefinition.getValueDefinition().getElement(document));
        return element;
    }
}