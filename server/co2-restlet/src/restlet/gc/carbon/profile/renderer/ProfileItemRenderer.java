package gc.carbon.profile.renderer;

import gc.carbon.profile.ProfileItemResource;
import gc.carbon.profile.ProfileItem;
import org.json.JSONObject;
import org.json.JSONException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import com.jellymold.utils.domain.APIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
public class ProfileItemRenderer extends Renderer {

    public ProfileItemRenderer(ProfileItemResource resource) {
        super(resource);
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        ProfileItem profileItem = resource.getProfileBrowser().getProfileItem();
        obj.put("profileItem", profileItem.getJSONObject());
        obj.put("path", resource.getPathItem().getFullPath());
        obj.put("profile", resource.getProfileBrowser().getProfile().getIdentityJSONObject());
        return obj;
    }


    public Element getElement(Document document) {
        ProfileItem profileItem = resource.getProfileBrowser().getProfileItem();
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(profileItem.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));
        element.appendChild(resource.getProfileBrowser().getProfile().getIdentityElement(document));
        return element;
    }
}
