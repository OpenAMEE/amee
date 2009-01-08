package gc.carbon.profile.acceptor;

import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileCategoryResource;
import gc.carbon.profile.ProfileForm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class ProfileCategoryJSONAcceptor implements Acceptor {

    private final Log log = LogFactory.getLog(getClass());
    private ProfileCategoryResource resource;

    public ProfileCategoryJSONAcceptor(ProfileCategoryResource resource) {
        this.resource = resource;
    }

    public List<ProfileItem> accept(Form form) {
        throw new UnsupportedOperationException();
    }

    public List<ProfileItem> accept(Representation entity) {
        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        String key;
        JSONObject rootJSON;
        JSONArray profileItemsJSON;
        JSONObject profileItemJSON;
        Form form;
        if (entity.isAvailable()) {
            try {
                rootJSON = new JSONObject(entity.getText());
                if (rootJSON.has("profileItems")) {
                    profileItemsJSON = rootJSON.getJSONArray("profileItems");
                    for (int i = 0; i < profileItemsJSON.length(); i++) {
                        profileItemJSON = profileItemsJSON.getJSONObject(i);
                        form = new ProfileForm(resource.getVersion());
                        for (Iterator iterator = profileItemJSON.keys(); iterator.hasNext();) {
                            key = (String) iterator.next();
                            form.add(key, profileItemJSON.getString(key));
                        }
                        List<ProfileItem> items = resource.getAcceptor(MediaType.TEXT_PLAIN).accept(form);
                        if (!items.isEmpty()) {
                            profileItems.addAll(items);
                        } else {
                            log.warn("Profile Item not added/modified");
                            return profileItems;
                        }
                    }
                }
            } catch (JSONException e) {
                log.warn("Caught JSONException: " + e.getMessage(), e);
            } catch (IOException e) {
                log.warn("Caught JSONException: " + e.getMessage(), e);
            }
        } else {
            log.warn("JSON not available");
        }
        return profileItems;
    }
}