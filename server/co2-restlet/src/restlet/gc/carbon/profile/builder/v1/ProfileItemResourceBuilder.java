package gc.carbon.profile.builder.v1;

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.ResourceBuilder;
import gc.carbon.domain.profile.builder.v1.ProfileItemBuilder;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileItemResource;
import gc.carbon.profile.builder.v2.AtomFeed;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

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

    ProfileItemResource resource;

    public ProfileItemResourceBuilder(ProfileItemResource resource) {
        this.resource = resource;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        ProfileItem profileItem = resource.getProfileItem();
        setBuilder(profileItem);
        obj.put("profileItem", profileItem.getJSONObject(true));
        obj.put("path", resource.getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }


    public Element getElement(Document document) {
        ProfileItem profileItem = resource.getProfileItem();
        setBuilder(profileItem);
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(profileItem.getElement(document, true));
        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }

    public Map<String, Object> getTemplateValues() {
        ProfileItem profileItem = resource.getProfileItem();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profileItem.getProfile());
        values.put("profileItem", profileItem);
        values.put("node", profileItem);
        return values;
    }

    //TODO - v1 builders should not need to implement atom feeds
    public org.apache.abdera.model.Element getAtomElement() {
        return AtomFeed.getInstance().newFeed();
    }

    private void setBuilder(ProfileItem pi) {
        if (resource.getProfileBrowser().returnInExternalUnit()) {
            pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getReturnUnit()));
        } else {
            pi.setBuilder(new ProfileItemBuilder(pi));
        }
    }    
}
