package com.amee.restlet.profile.acceptor;

import com.amee.domain.profile.ProfileItem;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.utils.APIFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class ProfileCategoryJSONAcceptor implements IProfileCategoryRepresentationAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    // The default maximum size for batch ProfileItem POSTs.
    private static int MAX_PROFILE_BATCH_SIZE = 50;

    @Autowired
    private ProfileCategoryFormAcceptor formAcceptor;

    public ProfileCategoryJSONAcceptor() {
        String maxProfileBatchSize = System.getProperty("amee.MAX_PROFILE_BATCH_SIZE");
        if (StringUtils.isNumeric(maxProfileBatchSize)) {
            MAX_PROFILE_BATCH_SIZE = Integer.parseInt(maxProfileBatchSize);
        }
    }

    public List<ProfileItem> accept(ProfileCategoryResource resource, Representation entity) {
        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        if (entity.isAvailable()) {
            try {
                JSONObject rootJSON = new JSONObject(entity.getText());
                if (rootJSON.has("profileItems")) {
                    JSONArray profileItemsJSON = rootJSON.getJSONArray("profileItems");

                    // AMEE 2.0 has a maximum allowed size for batch POSTs and POSTs. If this request execeeds that limit
                    // do not process the request and return a 400 status
                    if ((profileItemsJSON.length() > MAX_PROFILE_BATCH_SIZE) && (resource.getAPIVersion().isNotVersionOne())) {
                        resource.badRequest();
                        return profileItems;
                    }

                    // If the POST inputstream contains more than one entity it is considered a batch request.
                    if (profileItemsJSON.length() > 1 && resource.isPost()) {
                        resource.setIsBatchPost(true);
                    }

                    for (int i = 0; i < profileItemsJSON.length(); i++) {
                        JSONObject profileItemJSON = profileItemsJSON.getJSONObject(i);
                        Form form = new Form();

                        for (Iterator iterator = profileItemJSON.keys(); iterator.hasNext();) {
                            String key = (String) iterator.next();
                            form.add(key, profileItemJSON.getString(key));
                        }

                        // Representations to be returned for batch requests can be specified as a query parameter.
                        form.add("representation", resource.getForm().getFirstValue("representation"));

                        List<ProfileItem> items = formAcceptor.accept(resource, form);
                        if (!items.isEmpty()) {
                            profileItems.addAll(items);
                        } else {
                            log.warn("Profile Item not added/modified");
                            break;
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
        if (profileItems.isEmpty()) {
            resource.badRequest(APIFault.EMPTY_LIST);
        }
        return profileItems;
    }
}