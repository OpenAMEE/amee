package gc.carbon.domain.data.builder.v2;

import com.jellymold.utils.domain.APIUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import gc.carbon.domain.Builder;
import gc.carbon.domain.data.builder.BuildableItemValueDefinition;

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
public class ItemValueDefinitionBuilder implements Builder {

    private BuildableItemValueDefinition itemValueDefinition;

    public ItemValueDefinitionBuilder(BuildableItemValueDefinition itemValueDefinition) {
        this.itemValueDefinition = itemValueDefinition;
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", itemValueDefinition.getUid());
        obj.put("path", itemValueDefinition.getPath());
        obj.put("name", itemValueDefinition.getName());

        if (itemValueDefinition.hasUnits()) {
            JSONObject unit = new JSONObject();
            unit.put("internalUnit", itemValueDefinition.getInternalUnit());
            unit.put("choices", itemValueDefinition.getUnit().getChoices());
            obj.append("unit", unit);
        }

        if (itemValueDefinition.hasPerUnits()) {
            JSONObject perUnit = new JSONObject();
            perUnit.put("internalUnit", itemValueDefinition.getInternalPerUnit());
            perUnit.put("choices", itemValueDefinition.getPerUnit().getChoices());
            obj.append("perUnit", perUnit);
        }

        obj.put("valueDefinition", itemValueDefinition.getValueDefinition().getJSONObject(false));
        if (detailed) {
            obj.put("created", itemValueDefinition.getCreated());
            obj.put("modified", itemValueDefinition.getModified());
            obj.put("value", itemValueDefinition.getValue());
            obj.put("choices", itemValueDefinition.getChoices());
            obj.put("fromProfile", itemValueDefinition.isFromProfile());
            obj.put("fromData", itemValueDefinition.isFromData());
            obj.put("allowedRoles", itemValueDefinition.getAllowedRoles());
            obj.put("environment", itemValueDefinition.getEnvironment().getIdentityJSONObject());
            obj.put("itemDefinition", itemValueDefinition.getItemDefinition().getIdentityJSONObject());
        }
        return obj;
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ItemValueDefinition");
        element.setAttribute("uid", itemValueDefinition.getUid());
        element.appendChild(APIUtils.getElement(document, "Path", itemValueDefinition.getPath()));
        element.appendChild(APIUtils.getElement(document, "Name", itemValueDefinition.getName()));

        Element unit = document.createElement("Unit");
        if (itemValueDefinition.hasUnits()) {
            unit.appendChild(APIUtils.getElement(document, "InternalUnit", itemValueDefinition.getInternalUnit().toString()));
            unit.appendChild(APIUtils.getElement(document, "Choices", itemValueDefinition.getUnit().getChoices()));
        }
        element.appendChild(unit);

        Element perUnit = document.createElement("PerUnit");
        if (itemValueDefinition.hasPerUnits()) {
            perUnit.appendChild(APIUtils.getElement(document, "InternalUnit", itemValueDefinition.getInternalPerUnit().toString()));
            perUnit.appendChild(APIUtils.getElement(document, "Choices", itemValueDefinition.getPerUnit().getChoices()));
        }
        element.appendChild(perUnit);

        element.appendChild(APIUtils.getElement(document, "FromProfile", Boolean.toString(itemValueDefinition.isFromProfile())));
        element.appendChild(APIUtils.getElement(document, "FromData", Boolean.toString(itemValueDefinition.isFromData())));
        element.appendChild(itemValueDefinition.getValueDefinition().getElement(document, false));
        if (detailed) {
            element.setAttribute("created", itemValueDefinition.getCreated().toString());
            element.setAttribute("modified", itemValueDefinition.getModified().toString());
            element.appendChild(APIUtils.getElement(document, "Value", itemValueDefinition.getValue()));
            element.appendChild(APIUtils.getElement(document, "Choices", itemValueDefinition.getChoices()));
            element.appendChild(APIUtils.getElement(document, "AllowedRoles", itemValueDefinition.getAllowedRoles()));
            element.appendChild(itemValueDefinition.getEnvironment().getIdentityElement(document));
            element.appendChild(itemValueDefinition.getItemDefinition().getIdentityElement(document));
        }
        return element;
    }

}