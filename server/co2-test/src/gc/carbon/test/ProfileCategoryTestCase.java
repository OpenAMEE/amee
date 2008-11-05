package gc.carbon.test;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Before;
import org.restlet.Client;
import org.restlet.data.Response;
import org.restlet.data.Reference;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
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
public class ProfileCategoryTestCase extends XMLTestCase {

    public ProfileCategoryTestCase(String name) {
        super(name);
    }


    @Before
    public void setUp() throws IOException {

        Client client = new Client(Protocol.HTTP);

        Reference itemsUri = new Reference(
                "http://local.stage.co2.dgen.net/profiles/B74EC806243F/transport/car/generic?profileDate=201001");

        Response response = client.get(itemsUri);
         if (response.getStatus().isSuccess()) {
             if (response.isEntityAvailable()) {
                 response.getEntity().write(System.out);
             }
         }
     }


    public void testForEquality() throws Exception {
        String myControlXML = "<msg><uuid>0x00435A8C</uuid></msg>";
        String myTestXML = "<msg><localId>2376</localId></msg>";
        //assertXMLEqual("comparing test xml to control xml", myControlXML, myTestXML);

        assertXMLNotEqual("test xml not similar to control xml", myControlXML, myTestXML);
    }

}
