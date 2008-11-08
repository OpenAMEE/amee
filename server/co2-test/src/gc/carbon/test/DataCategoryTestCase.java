package gc.carbon.test;

import org.restlet.data.Form;
import org.restlet.data.Response;
import org.restlet.data.Reference;
import org.restlet.data.MediaType;
import org.junit.Test;

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
public class DataCategoryTestCase extends APITestCase {

    public DataCategoryTestCase(String name) {
        super(name);
    }

    protected Response doGet() throws Exception {
        Reference uri = new Reference(LOCAL_HOST_NAME + "/data/home/heating");
        setMediaType(MediaType.APPLICATION_XML);
        setControl("get-data-home-heating.xml");
        return get(uri);
    }

    private void doAssertSimilarXML() throws Exception {
        Response response = doGet();
        assertXMLSimilar(response);
    }

    @Test
    public void testGetHomeHeating() throws Exception {
        doAssertSimilarXML();
    }

}
