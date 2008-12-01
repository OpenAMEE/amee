package gc.carbon.test;

import gc.carbon.test.profile.BaseProfileCategoryTestCase;
import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
<<<<<<< HEAD:server/co2-test/src/gc/carbon/test/APITestCase.java
import org.restlet.data.*;
=======
import org.junit.Before;
import org.restlet.Client;
import org.restlet.data.*;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.Series;
>>>>>>> 63ca64fdd4369294fb820f22b718cd2f44c7ef2d:server/co2-test/src/gc/carbon/test/APITestCase.java
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
<<<<<<< HEAD:server/co2-test/src/gc/carbon/test/APITestCase.java
=======
import java.sql.DriverManager;
>>>>>>> 63ca64fdd4369294fb820f22b718cd2f44c7ef2d:server/co2-test/src/gc/carbon/test/APITestCase.java
import java.sql.PreparedStatement;

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
public class APITestCase extends XMLTestCase {

    private String controlFile;

    public APITestCase(String s) {
        super(s);
        XMLUnit.setIgnoreWhitespace(true);
    }

    protected void initDB() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/amee", "amee", "amee");
        PreparedStatement statement = conn.prepareStatement("DELETE FROM ITEM WHERE TYPE = 'PI'");
        statement.execute();
    }

    protected void setControl(String controlFile) {
        this.controlFile = controlFile;
    }

    protected void assertJSONIdentical(Response response) throws Exception {
    }

    protected void assertXMLSimilar(Response response) throws Exception {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(controlFile);

        String control = asString(is);
        String test = asString(response.getEntity().getStream());

        System.out.println("control - " + control);
        System.out.println("test    - " + test);

        DetailedDiff diff = new DetailedDiff(compareXML(control, test));

        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        diff.overrideDifferenceListener(new UIDDifferenceListener());
        assertTrue("XML are similar", diff.similar());

    }

    protected String asString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line.trim());
        }
        return sb.toString();
    }
}

