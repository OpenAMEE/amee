package gc.carbon.test.profile;

import org.restlet.data.Form;
import org.restlet.data.Response;
import com.jellymold.utils.domain.UidGen;

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
public class BaseProfileCategoryTest extends BaseProfileTest {

    public BaseProfileCategoryTest(String s) throws Exception {
        super(s);
    }

    public BaseProfileCategoryTest(String s, String user, String password, String profile) throws Exception {
        super(s, user, password, profile);
    }

    public Response doPost(Form data) throws Exception {

        Form form = new Form();
        form.add("dataItemUid", DATA_CATEGORY_UID);
        if (!data.getNames().contains("name"))
            form.add("name", UidGen.getUid());
        for (String parameter : data.getNames()) {
            form.add(parameter,data.getFirstValue(parameter));
        }

        return client.post(form);
    }


}
