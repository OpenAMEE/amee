package com.amee.restlet.profile.acceptor;

import com.amee.core.APIUtils;
import com.amee.domain.profile.ProfileItem;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.utils.APIException;
import com.amee.restlet.utils.APIFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.restlet.data.Form;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
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
public class ProfileCategoryXMLAcceptor implements IProfileCategoryRepresentationAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    // The default maximum size for batch ProfileItem POSTs.
    private static int MAX_PROFILE_BATCH_SIZE = 50;

    @Autowired
    private ProfileCategoryFormAcceptor formAcceptor;

    public ProfileCategoryXMLAcceptor() {
        String maxProfileBatchSize = System.getProperty("amee.MAX_PROFILE_BATCH_SIZE");
        if (StringUtils.isNumeric(maxProfileBatchSize)) {
            MAX_PROFILE_BATCH_SIZE = Integer.parseInt(maxProfileBatchSize);
        }
    }

    public List<ProfileItem> accept(ProfileCategoryResource resource, Representation entity) throws APIException {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();

        if (entity.isAvailable()) {
            try {
                // Get root/ProfileCategory element.
                Element rootElem = APIUtils.getRootElement(entity.getStream());
                if (rootElem.getName().equalsIgnoreCase("ProfileCategory")) {

                    // Get ProfileItems element.
                    Element profileItemsElem = rootElem.element("ProfileItems");
                    if (profileItemsElem != null) {

                        // Get ProfileItem element.
                        List elements = profileItemsElem.elements("ProfileItem");

                        // AMEE 2.0 has a maximum allowed size for batch POSTs and PUTs. If this request exceeds that limit
                        // do not process the request and return a 400 status
                        if ((elements.size() > MAX_PROFILE_BATCH_SIZE) && (resource.getAPIVersion().isNotVersionOne())) {
                            throw new APIException(APIFault.MAX_BATCH_SIZE_EXCEEDED);
                        }

                        // If the POST InputStream contains more than one entity it is considered a batch request.
                        if (elements.size() > 1 && resource.isPost()) {
                            resource.setIsBatchPost(true);
                        }

                        // Iterate over XML ProfileItem submissions.
                        for (Object o1 : elements) {

                            // Convert XMl submission into a Restlet Form.
                            Element profileItemElem = (Element) o1;
                            Form form = new Form();
                            for (Object o2 : profileItemElem.elements()) {
                                Element profileItemValueElem = (Element) o2;
                                form.add(profileItemValueElem.getName(), profileItemValueElem.getText());
                            }

                            // Representations to be returned for batch requests can be specified as a query parameter.
                            form.add("representation", resource.getForm().getFirstValue("representation"));

                            // Use FormAcceptor to do the work.
                            profileItems.addAll(formAcceptor.accept(resource, form));
                        }

                    } else {
                        log.warn("ProfileItems element not found");
                        throw new APIException(APIFault.INVALID_CONTENT);
                    }

                } else {
                    log.warn("ProfileCategory element not found");
                    throw new APIException(APIFault.INVALID_CONTENT);
                }

            } catch (DocumentException e) {
                log.warn("Caught DocumentException: " + e.getMessage(), e);
                throw new APIException(APIFault.INVALID_CONTENT);
            } catch (IOException e) {
                log.warn("Caught IOException: " + e.getMessage(), e);
                throw new APIException(APIFault.INVALID_CONTENT);
            }

        } else {
            log.warn("XML not available");
            throw new APIException(APIFault.INVALID_CONTENT);
        }

        // Must have at least one ProfileItem.
        if (profileItems.isEmpty()) {
            throw new APIException(APIFault.EMPTY_LIST);
        }

        return profileItems;
    }
}
