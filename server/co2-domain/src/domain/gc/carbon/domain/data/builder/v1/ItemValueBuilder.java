package gc.carbon.domain.data.builder.v1;

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.Builder;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.mapper.LegacyItemValueMapper;
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
public class ItemValueBuilder implements Builder {

    private LegacyItemValueMapper itemValue;
    private Builder itemValueDefinitionRenderer;
    
    public ItemValueBuilder(ItemValue itemValue) {
        this.itemValue = new LegacyItemValueMapper(itemValue);
        this.itemValueDefinitionRenderer = new ItemValueDefinitionBuilder(itemValue.getItemValueDefinition());
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", itemValue.getUid());
        obj.put("path", itemValue.getPath());
        obj.put("name", itemValue.getName());
        obj.put("value", itemValue.getValue());
        ItemValueDefinition itemValueDefinition = itemValue.getItemValueDefinition();
        itemValueDefinition.setBuilder(itemValueDefinitionRenderer);
        obj.put("itemValueDefinition", itemValueDefinition.getJSONObject(false));
        if (detailed) {
            obj.put("created", itemValue.getCreated());
            obj.put("modified", itemValue.getModified());
            obj.put("item", itemValue.getItem().getIdentityJSONObject());
        }
        return obj;
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ItemValue");
        element.setAttribute("uid", itemValue.getUid());
        element.appendChild(APIUtils.getElement(document, "Path", itemValue.getPath()));
        element.appendChild(APIUtils.getElement(document, "Name", itemValue.getName()));
        element.appendChild(APIUtils.getElement(document, "Value", itemValue.getValue()));
        ItemValueDefinition itemValueDefinition = itemValue.getItemValueDefinition();
        itemValueDefinition.setBuilder(itemValueDefinitionRenderer);
        element.appendChild(itemValueDefinition.getElement(document, false));
        if (detailed) {
            element.setAttribute("Created", itemValue.getCreated().toString());
            element.setAttribute("Modified", itemValue.getModified().toString());
            element.appendChild(itemValue.getItem().getIdentityElement(document));
        }
        return element;
    }

}
