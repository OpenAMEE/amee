package gc.carbon.test.profile.v1;

import org.testng.annotations.Test;
import org.restlet.data.Status;
import org.restlet.data.Form;
import org.restlet.resource.DomRepresentation;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.jdom.xpath.XPath;
import org.jdom.input.DOMBuilder;
import org.jdom.Element;
import gc.carbon.test.profile.BaseProfileCategoryTest;
import gc.carbon.test.TestClient;

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
public class ProfileCategoryGETTest extends BaseProfileCategoryTest {

    private DateTimeFormatter VALID_FROM_FMT = DateTimeFormat.forPattern("yyyyMMdd");
    private DateTimeFormatter START_DATE_FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mmZ");

    private static String username = "amee";
    private static String password = "amee4amee";
    private static String profile = "428353BFFA71";

    public ProfileCategoryGETTest(String name) throws Exception {
        super(name, username, password, profile);
    }

    public void setUp() throws Exception {
        super.setUp();
        initDB();
    }

    private String create(DateTime startDate) throws Exception {
         Form data = new Form();
         data.add("validFrom",startDate.toString(VALID_FROM_FMT));
         data.add("distanceKmPerMonth","1000");
         data.add("dataItemUid",DATA_CATEGORY_UID);
         return client.createProfileItem(data);
    }

    @Test
    public void testInValidEndDateRequest() throws Exception {
        client.addQueryParameter("endDate","20100401");
        Status status = client.get().getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testInValidDurationRequest() throws Exception {
        client.addQueryParameter("duration","PT30M");
        Status status = client.get().getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testInValidStartDateRequest() throws Exception {
        client.addQueryParameter("startDate", "20100401");
        Status status = client.get().getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

}