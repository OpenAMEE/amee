package com.amee.restlet.profile.acceptor;

import com.amee.domain.item.BaseItemValue;
import com.amee.restlet.profile.ProfileItemValueResource;
import com.amee.restlet.profile.builder.v2.AtomFeed;
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

import java.io.IOException;

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
public class ProfileItemValueAtomAcceptor implements IItemValueRepresentationAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ProfileItemValueFormAcceptor formAcceptor;

    public BaseItemValue accept(ProfileItemValueResource resource, Representation representation) {

        BaseItemValue itemValue = null;

        if (representation.isAvailable()) {
            try {
                Document<Entry> doc = AtomFeed.getInstance().parse(representation.getStream());
                Entry entry = doc.getRoot();

                Form form = new Form();

                Element value = entry.getFirstChild(AtomFeed.Q_NAME_VALUE);

                if (value != null) {
                    form.add(new Parameter("value",value.getText()));
                    Element unit = entry.getFirstChild(AtomFeed.Q_NAME_UNIT);
                    if (unit != null) {
                        form.add(new Parameter("unit",unit.getText()));
                    }
                    Element perUnit = entry.getFirstChild(AtomFeed.Q_NAME_PER_UNIT);
                    if (perUnit != null) {
                        form.add(new Parameter("perUnit",perUnit.getText()));
                    }
                }

                itemValue = formAcceptor.accept(resource, form);

            } catch (IOException e) {
                log.warn("accept() - Caught IOException: " + e.getMessage(), e);
            }
        } else {
            log.warn("accept() - representation not available");
        }

        return itemValue;
    }

}