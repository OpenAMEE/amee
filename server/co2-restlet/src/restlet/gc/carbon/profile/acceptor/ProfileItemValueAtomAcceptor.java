package gc.carbon.profile.acceptor;

import gc.carbon.domain.data.ItemValue;
import gc.carbon.profile.ProfileItemValueResource;
import gc.carbon.profile.builder.v2.AtomFeed;
import gc.carbon.profile.acceptor.ItemValueAcceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.abdera.model.*;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.Representation;

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
public class ProfileItemValueAtomAcceptor implements ItemValueAcceptor {

    private final Log log = LogFactory.getLog(getClass());
    private ProfileItemValueResource resource;

    public ProfileItemValueAtomAcceptor(ProfileItemValueResource resource) {
        this.resource = resource;
    }

    public ItemValue accept(Form form) {
        throw new UnsupportedOperationException();
    }

    public ItemValue accept(Representation entity) {

        ItemValue itemValue = null;

        if (entity.isAvailable()) {
            try {
                Document<Entry> doc = AtomFeed.getInstance().parse(entity.getStream());
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

                itemValue = resource.getAcceptor(MediaType.TEXT_PLAIN).accept(form);

            } catch (IOException e) {
                log.warn("accept() - Caught IOException: " + e.getMessage(), e);
            }
        } else {
            log.warn("accept() - entity not available");
        }

        return itemValue;
    }

}