package com.amee.restlet.profile.acceptor;

import com.amee.domain.profile.ProfileItem;
import com.amee.restlet.profile.ProfileCategoryResource;
import com.amee.restlet.profile.builder.v2.AtomFeed;
import com.amee.restlet.utils.APIException;
import com.amee.restlet.utils.APIFault;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
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
public class ProfileCategoryAtomAcceptor implements IProfileCategoryRepresentationAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ProfileCategoryFormAcceptor formAcceptor;

    // TODO - Add Batch processing of entries
    public List<ProfileItem> accept(ProfileCategoryResource resource, Representation entity) throws APIException {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();

        if (entity.isAvailable()) {
            try {

                Document<Entry> doc = AtomFeed.getInstance().parse(entity.getStream());
                Entry entry = doc.getRoot();

                List<Category> categories = entry.getCategories();
                if (!categories.isEmpty()) {

                    // Convert Atom submission into a Restlet Form.
                    Form form = new Form();
                    addDataItem(form, categories);
                    addParameter(form, entry, AtomFeed.Q_NAME_START_DATE);
                    addParameter(form, entry, AtomFeed.Q_NAME_END_DATE);
                    addParameter(form, entry, AtomFeed.Q_NAME_DURATION);
                    addParameter(form, entry, AtomFeed.Q_NAME_NAME);
                    addItemValues(form, entry);

                    // Use FormAcceptor to do the work.
                    profileItems.addAll(formAcceptor.accept(resource, form));
                }

            } catch (IOException e) {
                log.warn("accept() - Caught IOException: " + e.getMessage(), e);
                throw new APIException(APIFault.INVALID_CONTENT);
            }

        } else {
            log.warn("accept() - entity not available");
            throw new APIException(APIFault.INVALID_CONTENT);
        }

        // Must have at least one ProfileItem.
        if (profileItems.isEmpty()) {
            throw new APIException(APIFault.EMPTY_LIST);
        }

        return profileItems;
    }

    private void addDataItem(Form form, List<Category> categories) {
        form.add("dataItemUid", categories.get(0).getTerm());
    }

    private void addParameter(Form form, Entry entry, QName qName) {
        Element element = entry.getExtension(qName);
        if (element != null) {
            form.add(new Parameter(qName.getLocalPart(), element.getText()));
        }
    }

    private void addItemValues(Form form, Entry entry) {
        for (Element element : entry.getExtensions(AtomFeed.Q_NAME_ITEM_VALUE)) {
            Element name = element.getFirstChild(AtomFeed.Q_NAME_NAME);
            Element value = element.getFirstChild(AtomFeed.Q_NAME_VALUE);
            if (name != null && value != null) {
                form.add(new Parameter(name.getText(), value.getText()));
                Element unit = element.getFirstChild(AtomFeed.Q_NAME_UNIT);
                if (unit != null) {
                    form.add(new Parameter(name + "Unit", unit.getText()));
                }
                Element perUnit = element.getFirstChild(AtomFeed.Q_NAME_PER_UNIT);
                if (perUnit != null) {
                    form.add(new Parameter(name + "PerUnit", perUnit.getText()));
                }
            }
        }
    }
}
