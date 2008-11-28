package gc.carbon.test.profile;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.MediaType;
import org.restlet.data.Response;

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
public class BaseProfileItemTestCase extends BaseProfileTestCase {

    public BaseProfileItemTestCase(String s) {
        super(s);
    }

    protected Response doPut(Form data) throws Exception {
        Form post = new Form();
        post.add("v",data.getFirstValue("v"));
        String uid = createProfileItem(post);
        Reference uri = new Reference(LOCAL_HOST_NAME + profileCategoryURI + uid);

        Form form = new Form();
        for (String parameter : data.getNames()) {
            form.add(parameter,data.getFirstValue(parameter));
        }
        setMediaType(MediaType.APPLICATION_XML);
        return put(uri, form);
    }
}