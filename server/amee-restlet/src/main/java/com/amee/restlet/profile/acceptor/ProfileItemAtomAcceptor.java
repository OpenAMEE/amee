package com.amee.restlet.profile.acceptor;

import com.amee.domain.item.profile.ProfileItem;
import com.amee.restlet.profile.ProfileItemResource;
import com.amee.restlet.profile.builder.v2.AtomFeed;
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
public class ProfileItemAtomAcceptor implements IProfileItemRepresentationAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private IProfileItemFormAcceptor formAcceptor;

    public List<ProfileItem> accept(ProfileItemResource resource, Representation entity) {

        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        if (entity.isAvailable()) {
            try {

                Document<Entry> doc = AtomFeed.getInstance().parse(entity.getStream());
                Entry entry = doc.getRoot();

                List<Category> categories = entry.getCategories();
                if (!categories.isEmpty()) {

                    Form form = new Form();

                    addDataItem(form, categories);
                    addParameter(form, entry, AtomFeed.Q_NAME_START_DATE);
                    addParameter(form, entry, AtomFeed.Q_NAME_END_DATE);
                    addParameter(form, entry, AtomFeed.Q_NAME_DURATION);
                    addParameter(form, entry, AtomFeed.Q_NAME_NAME);
                    addItemValues(form, entry);

                    List<ProfileItem> items = formAcceptor.accept(resource, form);
                    if (!items.isEmpty()) {
                        profileItems.addAll(items);
                    } else {
                        log.warn("accept() - Profile Item not added");
                        return profileItems;
                    }

                }

            } catch (IOException e) {
                log.warn("accept() - Caught IOException: " + e.getMessage(), e);
            }
        } else {
            log.warn("accept() - entity not available");
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
                form.add(new Parameter(name.getText(),value.getText()));
                Element unit = element.getFirstChild(AtomFeed.Q_NAME_UNIT);
                if (unit != null) {
                    form.add(new Parameter(name + "Unit",unit.getText()));
                }
                Element perUnit = element.getFirstChild(AtomFeed.Q_NAME_PER_UNIT);
                if (perUnit != null) {
                    form.add(new Parameter(name + "PerUnit",perUnit.getText()));
                }
            }
        }
    }
}