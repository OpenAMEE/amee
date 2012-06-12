package com.amee.restlet.profile.acceptor;

import com.amee.domain.item.profile.ProfileItem;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.utils.APIException;
import com.amee.restlet.utils.APIFault;
import org.apache.commons.lang3.StringUtils;
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

    public List<ProfileItem> accept(ProfileCategoryResource resource, Representation entity) throws APIException {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        
        if (entity.isAvailable()) {
            try {
                JSONObject rootJSON = new JSONObject(entity.getText());
                if (rootJSON.has("profileItems")) {
                    JSONArray profileItemsJSON = rootJSON.getJSONArray("profileItems");

                    // AMEE 2.0 has a maximum allowed size for batch POSTs and POSTs. If this request execeeds that limit
                    // do not process the request and return a 400 status
                    if ((profileItemsJSON.length() > MAX_PROFILE_BATCH_SIZE) && (resource.getAPIVersion().isNotVersionOne())) {
                        throw new APIException(APIFault.MAX_BATCH_SIZE_EXCEEDED);
                    }

                    // If the POST inputstream contains more than one entity it is considered a batch request.
                    if (profileItemsJSON.length() > 1 && resource.isPost()) {
                        resource.setIsBatchPost(true);
                    }

                    // Iterate over JSON ProfileItem submissions.
                    for (int i = 0; i < profileItemsJSON.length(); i++) {

                        // Convert JSON submission into a Restlet Form.
                        JSONObject profileItemJSON = profileItemsJSON.getJSONObject(i);
                        Form form = new Form();
                        for (Iterator iterator = profileItemJSON.keys(); iterator.hasNext();) {
                            String key = (String) iterator.next();
                            form.add(key, profileItemJSON.getString(key));
                        }

                        // Representations to be returned for batch requests can be specified as a query parameter.
                        form.add("representation", resource.getForm().getFirstValue("representation"));

                        // Use FormAcceptor to do the work.
                        profileItems.addAll(formAcceptor.accept(resource, form));
                    }

                } else {
                    log.warn("profileItems node not found");
                    throw new APIException(APIFault.INVALID_CONTENT);
                }

            } catch (JSONException e) {
                log.warn("Caught JSONException: " + e.getMessage(), e);
                throw new APIException(APIFault.INVALID_CONTENT);
            } catch (IOException e) {
                log.warn("Caught JSONException: " + e.getMessage(), e);
                throw new APIException(APIFault.INVALID_CONTENT);
            }

        } else {
            log.warn("JSON not available");
            throw new APIException(APIFault.INVALID_CONTENT);
        }

        // Must have at least one ProfileItem.
        if (profileItems.isEmpty()) {
            throw new APIException(APIFault.EMPTY_LIST);
        }

        return profileItems;
    }
}