package gc.carbon.test;

import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.restlet.Client;
import org.restlet.util.Series;
import org.restlet.resource.Representation;
import org.restlet.data.*;
import org.junit.Before;
import org.w3c.dom.Node;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

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
public class APITestCase extends XMLTestCase {

    protected static final String LOCAL_HOST_NAME = "http://local.stage.co2.dgen.net";
    protected static final String REMOTE_HOST_NAME = "http://stage.co2.dgen.net";

    protected Series<CookieSetting> cookieSettings;
    private String controlFile;
    private MediaType mediaType;
    public APITestCase(String s) {
        super(s);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Before
    public void setUp() throws IOException {
       cookieSettings = authenticate();
    }

    private Series<CookieSetting> authenticate() throws IOException {
        Client client = new Client(Protocol.HTTP);
        Reference uri = new Reference(LOCAL_HOST_NAME + "/auth/signIn?method=put");
        Form form = new Form();
        form.add("next", "auth");
        form.add("username", "admin");
        form.add("password", "r41n80w");
        Representation rep = form.getWebRepresentation();
        Response response = client.post(uri, rep);
        if (response.getStatus().isRedirection()) {
            return response.getCookieSettings();
        }
        return null;
    }

    private void addHeaders(Request request) {
        Form requestHeaders = new Form();
        requestHeaders.add("authToken",cookieSettings.getFirstValue("authToken"));
        request.getAttributes().put("org.restlet.http.headers", requestHeaders);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(mediaType));
    }

    protected Response get(Reference uri) {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.GET, uri);
        addHeaders(request);
        return client.handle(request);
    }

    protected Response post(Reference uri, Form form) {
        Client client = new Client(Protocol.HTTP);
        Representation rep = form.getWebRepresentation();
        Request request = new Request(Method.POST, uri, rep);
        addHeaders(request);
        return client.handle(request);
    }


    protected void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    protected void setControl(String controlFile) {
        this.controlFile = controlFile;
    }

    protected void assertJSONIdentical(Response response) throws Exception {
        String test = asString(response.getEntity().getStream());
        System.out.println(test);
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

class UIDDifferenceListener implements DifferenceListener {

    public int differenceFound(Difference difference) {

        Node node = difference.getControlNodeDetail().getNode();

        if (node.getNodeName().equals("uid")) {
            return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }

        if (node.getNodeType() == Node.TEXT_NODE &&
                node.getParentNode().getNodeName().equals("Name") &&
                node.getParentNode().getParentNode().getNodeName().equals("ProfileItem")) {
            return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }

        //System.out.println(node.getNodeName() + " => " + difference.getDescription() + " => " + node.getNodeValue());

        return RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(Node node, Node node1) {
    }
}
