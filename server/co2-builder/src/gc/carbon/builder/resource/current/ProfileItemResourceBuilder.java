package gc.carbon.builder.resource.current;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.APIObject;
import gc.carbon.builder.resource.ResourceBuilder;
import gc.carbon.builder.resource.BuildableCategoryResource;
import gc.carbon.builder.resource.BuildableResource;
import gc.carbon.builder.domain.current.ProfileItemBuilder;
import gc.carbon.builder.domain.BuildableProfileItem;
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
public class ProfileItemResourceBuilder implements ResourceBuilder {

    BuildableResource resource;

    public ProfileItemResourceBuilder(BuildableResource resource) {
        this.resource = resource;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        BuildableProfileItem profileItem = resource.getProfileItem();
        profileItem.setBuilder(new ProfileItemBuilder(profileItem));
        obj.put("profileItem", profileItem.getJSONObject());
        obj.put("path", resource.getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }


    public Element getElement(Document document) {
        BuildableProfileItem profileItem = resource.getProfileItem();
        profileItem.setBuilder(new ProfileItemBuilder(profileItem));
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(profileItem.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }
}