package gc.carbon.profile.builder.v2;

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.profile.builder.BuildableProfileItem;
import gc.carbon.domain.profile.builder.v2.ProfileItemBuilder;
import gc.carbon.data.builder.ResourceBuilder;
import gc.carbon.data.builder.BuildableResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.HashMap;

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
        setBuilder(profileItem);
        obj.put("profileItem", profileItem.getJSONObject());
        obj.put("path", resource.getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }


    public Element getElement(Document document) {
        BuildableProfileItem profileItem = resource.getProfileItem();
        setBuilder(profileItem);
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(profileItem.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }

    public Map<String, Object> getTemplateValues() {
        BuildableProfileItem profileItem = resource.getProfileItem();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profileItem.getProfile());
        values.put("profileItem", profileItem);
        values.put("node", profileItem);
        return values;
    }

    private void setBuilder(BuildableProfileItem pi) {
        if (resource.getProfileBrowser().returnAmountInExternalUnit()) {
            pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getAmountUnit()));
        } else {
            pi.setBuilder(new ProfileItemBuilder(pi));
        }
    }
}