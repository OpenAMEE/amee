package gc.carbon.profile.acceptor;

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileCategoryResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;

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
public class ProfileCategoryXMLAcceptor implements Acceptor {

    private final Log log = LogFactory.getLog(getClass());
    private ProfileCategoryResource resource;

    public ProfileCategoryXMLAcceptor(ProfileCategoryResource resource) {
        this.resource = resource;
    }

    public List<ProfileItem> accept(Form form) {
        throw new UnsupportedOperationException();
    }

    public List<ProfileItem> accept(Representation entity) {
        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        Element rootElem;
        Element profileItemsElem;
        Element profileItemElem;
        Element profileItemValueElem;
        Form form;
        if (entity.isAvailable()) {
            try {
                rootElem = APIUtils.getRootElement(entity.getStream());
                if (rootElem.getName().equalsIgnoreCase("ProfileCategory")) {
                    profileItemsElem = rootElem.element("ProfileItems");
                    if (profileItemsElem != null) {
                        for (Object o1 : profileItemsElem.elements("ProfileItem")) {
                            profileItemElem = (Element) o1;
                            form = new Form();
                            for (Object o2 : profileItemElem.elements()) {
                                profileItemValueElem = (Element) o2;
                                form.add(profileItemValueElem.getName(), profileItemValueElem.getText());
                            }
                            List<ProfileItem> items = resource.getAcceptor(MediaType.TEXT_PLAIN).accept(form);
                            if (!items.isEmpty()) {
                                profileItems.addAll(items);
                            } else {
                                log.warn("Profile Item not added");
                                return profileItems;
                            }
                        }
                    }
                } else {
                    log.warn("Profile Category not found");
                }
            } catch (DocumentException e) {
                log.warn("Caught DocumentException: " + e.getMessage(), e);
            } catch (IOException e) {
                log.warn("Caught IOException: " + e.getMessage(), e);
            }
        } else {
            log.warn("XML not available");
        }
        return profileItems;
    }
}
