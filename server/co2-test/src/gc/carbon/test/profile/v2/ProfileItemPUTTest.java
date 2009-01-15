package gc.carbon.test.profile.v2;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

import gc.carbon.test.profile.BaseProfileItemTest;

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
        assertDistanceNode(data, "km", "year");
    }

    @Test
    public void testPutWithExternalUnitAsMile() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distance", "1000");
        assertDistanceNode(data, "mi", "year");
    }

    @Test
    public void testPutWithExternalUnitAsMileAndPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "year");
        data.add("distance", "1000");
        assertDistanceNode(data, "mi", "year");
    }

    @Test
    public void testPutWithStartDate() throws Exception {
        String startDate = "2010-04-01T00:00+0000";
        Form data = new Form();
        data.add("distance", "1000");
        data.add("startDate", startDate);
        assertDateNodes(data, startDate, null, "false");
    }

    @Test
    public void testPutWithStartDateAndEndDate() throws Exception {
        String startDate = "2010-04-01T00:00+0000";
        String endDate = "2010-04-02T00:00+0000";
        Form data = new Form();
        data.add("startDate", startDate);
        data.add("endDate", endDate);
        assertDateNodes(data, startDate, endDate, "false");
    }

    @Test
    public void testPutWithEndDate() throws Exception {
        String endDate = "2010-04-02T00:00+0000";
        Form data = new Form();
        data.add("endDate", endDate);
        assertDateNodes(data, getDefaultDate(), endDate, "false");
    }

    @Test
    public void testPutWithStartDateAndDuration() throws Exception {
        String startDate = "2010-04-01T00:00+0000";
        String endDate = "2010-04-01T00:30+0000";
        Form data = new Form();
        data.add("startDate", startDate);
        data.add("duration", "PT30M");
        assertDateNodes(data, startDate, endDate, "false");
    }

    @Test
    public void testPutWithEndAndEndDate() throws Exception {
        Form data = new Form();

        data.add("endDate", "20100401T0000");
        data.add("end", "true");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutWithEndAndDuration() throws Exception {
        Form data = new Form();

        data.add("duration", "PT30M");
        data.add("end", "true");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutEndDateBeforeStartDate() throws Exception {
        Form data = new Form();

        data.add("startDate", "2010-04-02T00:00+0000");
        data.add("endDate", "2010-04-01T00:00+0000");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutWithValidFrom() throws Exception {
        Form data = new Form();

        data.add("validFrom", "20100401");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    @Test
    public void testPutWithEnd() throws Exception {
        Form data = new Form();

        data.add("end", "true");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    private void assertDateNodes(Form data, String startDate, String endDate, String end) throws Exception {

        DomRepresentation rep = doPut(data).getEntityAsDom();
        rep.write(System.out);
        Document doc = rep.getDocument();
        assertXpathEvaluatesTo(FMT.format(FMT.parse(startDate)), "/Resources/ProfileItemResource/ProfileItem/StartDate", doc);
        if (endDate != null) {
            assertXpathEvaluatesTo(FMT.format(FMT.parse(endDate)), "/Resources/ProfileItemResource/ProfileItem/EndDate", doc);
        } else {
            assertXpathEvaluatesTo("", "/Resources/ProfileItemResource/ProfileItem/EndDate", doc);
        }
    }

    private void assertDistanceNode(Form data, String unit, String perUnit) throws Exception {

        DomRepresentation rep = doPut(data).getEntityAsDom();
        rep.write(System.out);
        Document doc = rep.getDocument();
        assertXpathEvaluatesTo("distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/Path", doc);
        assertXpathEvaluatesTo("Distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/Name", doc);
        assertXpathEvaluatesTo(perUnit, "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/PerUnit", doc);
        assertXpathEvaluatesTo(unit, "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/Unit", doc);
        assertXpathEvaluatesTo("distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Path", doc);
        assertXpathEvaluatesTo("Distance", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Name", doc);
        assertXpathEvaluatesTo("year", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/PerUnit/InternalUnit", doc);
        assertXpathEvaluatesTo("km", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Unit/InternalUnit", doc);

    }

    private String getDefaultDate() {
        return FMT.format(new Date());
    }
}