package gc.carbon.profile.acceptor;

import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileCategoryResource;
import org.restlet.data.Form;
import org.restlet.resource.Representation;

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
public abstract class Acceptor {

    protected ProfileCategoryResource resource;

    public Acceptor(ProfileCategoryResource resource) {
        this.resource = resource;
    }

    public abstract List<ProfileItem> accept(Representation entity, Form form);
}
