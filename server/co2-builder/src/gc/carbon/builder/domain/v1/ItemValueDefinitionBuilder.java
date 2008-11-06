package gc.carbon.builder.domain.v1;

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.builder.Builder;
import gc.carbon.builder.domain.BuildableItemValueDefinition;
import gc.carbon.builder.mapper.LegacyItemValueDefinitionMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    private LegacyItemValueDefinitionMapper itemValueDefinition;

    public ItemValueDefinitionBuilder(BuildableItemValueDefinition itemValueDefinition) {
        this.itemValueDefinition = new LegacyItemValueDefinitionMapper(itemValueDefinition);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", itemValueDefinition.getUid());
        obj.put("path", itemValueDefinition.getPath());
        obj.put("name", itemValueDefinition.getName());
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
