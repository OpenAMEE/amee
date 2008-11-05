package gc.carbon.builder.resource.v1;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.APIObject;
import gc.carbon.builder.resource.ResourceBuilder;
import gc.carbon.builder.resource.BuildableResource;
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
public class ProfileItemResourceBuilder extends ResourceBuilder {

    public ProfileItemResourceBuilder(BuildableResource resource) {
        super(resource);
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        APIObject profileItem = resource.getProfileItem();
        obj.put("profileItem", profileItem.getJSONObject());
        obj.put("path", resource.getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }


    public Element getElement(Document document) {
        APIObject profileItem = resource.getProfileItem();
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(profileItem.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }
}
