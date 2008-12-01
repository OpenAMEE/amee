package gc.carbon.test.profile;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

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
public class ProfileItemPUTTest extends BaseProfileItemTest {

    public ProfileItemPUTTest(String name) throws Exception {
        super(name);
        initDB();
    }

    @Test
    public void testPutWithExternalPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distancePerUnit", "year");
        data.add("distance", "1000");
        data.add("v", "2.0");
        assertDistanceNode(data, "km", "year");
    }

    @Test
    public void testPutWithExternalUnitAsMile() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distance", "1000");
        data.add("v", "2.0");
        assertDistanceNode(data, "mi", "year");
    }

    @Test
    public void testPutWithExternalUnitAsMileAndPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "year");
        data.add("distance", "1000");
        data.add("v", "2.0");
        assertDistanceNode(data, "mi", "year");
    }

    @Test
    public void testPutWithStartDate() throws Exception {
        String startDate = "20100401T0000";
        Form data = new Form();
        data.add("distance", "1000");
        data.add("startDate", startDate);
        data.add("v", "2.0");
        assertDateNodes(data, startDate, null, "false");
    }

    @Test
    public void testPutWithStartDateAndEndDate() throws Exception {
        String startDate = "20100401T0000";
        String endDate = "20100402T0000";
        Form data = new Form();
        data.add("startDate", startDate);
        data.add("endDate", endDate);
        data.add("v", "2.0");
        assertDateNodes(data, startDate, endDate, "false");
    }

    @Test
    public void testPutWithEndDate() throws Exception {
        String endDate = "20100402T0000";
        Form data = new Form();
        data.add("endDate", endDate);
        data.add("v", "2.0");
        assertDateNodes(data, getDefaultDate(), endDate, "false");
    }

    @Test
    public void testPutWithStartDateAndDuration() throws Exception {
        String startDate = "20100401T0000";
        String endDate = "20100401T0030";
        Form data = new Form();
        data.add("startDate", startDate);
        data.add("duration", "PT30M");
        data.add("v", "2.0");
        assertDateNodes(data, startDate, endDate, "false");
    }

    @Test
    public void testPutWithEndAndEndDate() throws Exception {
        Form data = new Form();
        data.add("endDate", "20100401T0000");
        data.add("end", "true");
        data.add("v", "2.0");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutWithEndAndDuration() throws Exception {
        Form data = new Form();
        data.add("duration", "PT30M");
        data.add("end", "true");
        data.add("v", "2.0");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutWithEndDateAndDuration() throws Exception {
        Form data = new Form();
        data.add("duration", "PT30M");
        data.add("endDate", "20100401T0000");
        data.add("v", "2.0");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutEndDateBeforeStartDate() throws Exception {
        Form data = new Form();
        data.add("startDate", "20100402T0000");
        data.add("endDate", "20100401T0000");
        data.add("v", "2.0");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutWithValidFrom() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20100401");
        data.add("v", "2.0");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutWithEnd() throws Exception {
        Form data = new Form();
        data.add("end", "true");
        data.add("v", "2.0");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    private void assertDateNodes(Form data, String startDate, String endDate, String end) throws Exception {
        Document doc = doPut(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo(startDate, "/Resources/ProfileItemResource/ProfileItem/StartDate", doc);
        if (endDate != null) {
            assertXpathEvaluatesTo(endDate, "/Resources/ProfileItemResource/ProfileItem/EndDate", doc);
        } else {
            assertXpathEvaluatesTo("", "/Resources/ProfileItemResource/ProfileItem/EndDate", doc);
        }
    }

    private void assertDistanceNode(Form data, String unit, String perUnit) throws Exception {
        Document doc = doPut(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/Path", doc);
        assertXpathEvaluatesTo("Distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/Name", doc);
        assertXpathEvaluatesTo(perUnit, "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/PerUnit", doc);
        assertXpathEvaluatesTo(unit, "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/Unit", doc);
        assertXpathEvaluatesTo("distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Path", doc);
        assertXpathEvaluatesTo("Distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Name", doc);
        assertXpathEvaluatesTo("year", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/PerUnit", doc);
        assertXpathEvaluatesTo("km", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Unit", doc);

    }

    private String getDefaultDate() {
        String ISO_DATE = "yyyyMMdd'T'HHmm";
        return new SimpleDateFormat(ISO_DATE).format(new Date());
    }
}