package gc.carbon.test.profile.v2;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

import gc.carbon.test.profile.BaseProfileCategoryTest;

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
public class ProfileCategoryPOSTTest extends BaseProfileCategoryTest {

    public ProfileCategoryPOSTTest(String name) throws Exception {
        super(name);
    }

    @Test
    public void testPostWithStartDate() throws Exception {
        String startDate = "2010-04-01T00:00+0000";
        Form data = new Form();
        data.add("distance", "1000");
        data.add("startDate", startDate);
        assertDateNodes(data, startDate, null, "false");
    }

    @Test
    public void testPostWithStartDateAndEndDate() throws Exception {
        String startDate = "2010-04-01T00:00+0000";
        String endDate = "2010-04-02T00:00+0000";
        Form data = new Form();
        data.add("startDate", startDate);
        data.add("endDate", endDate);
        assertDateNodes(data, startDate, endDate, "false");
    }

    @Test
    public void testPostWithEnd() throws Exception {
        Form data = new Form();
        data.add("end", "true");
        assertBadRequest(data);
    }

    @Test
    public void testPostWithStartDateAndDuration() throws Exception {
        String startDate = "2010-04-01T00:00+0000";
        String endDate = "2010-04-01T00:30+0000";
        Form data = new Form();
        data.add("startDate", startDate);
        data.add("duration", "PT30M");
        assertDateNodes(data, startDate, endDate, "false");
    }

    @Test
    public void testPostWithEndAndEndDate() throws Exception {
        Form data = new Form();
        data.add("endDate", "2010-04-01T00:00+0000");
        data.add("end", "true");
        assertBadRequest(data);
    }

    @Test
    public void testPostWithEndAndDuration() throws Exception {
        Form data = new Form();
        data.add("duration", "PT30M");
        data.add("end", "true");
        assertBadRequest(data);
    }

    @Test
    public void testPostEndDateBeforeStartDate() throws Exception {
        Form data = new Form();
        data.add("startDate", "2010-04-02T00:00+0000");
        data.add("endDate", "2010-04-01T00:00+0000");
        assertBadRequest(data);
    }

    @Test
    public void testPostWithExternalPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distancePerUnit", "year");
        data.add("distance", "1000");
        assertDistanceNode(data, "km", "year");
    }

    @Test
    public void testPostWithExternalUnitAsMile() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distance", "1000");
        assertDistanceNode(data, "mi", "year");
    }

    @Test
    public void testPostWithExternalUnitAsMileAndPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "year");
        data.add("distance", "1000");
        assertDistanceNode(data, "mi", "year");
    }


    @Test
    public void testPostWithPerUnitNone() throws Exception {
        Form data = new Form();
        data.add("distancePerUnit", "none");
        data.add("distance", "1000");
        assertDistanceNode(data, "km", "year");
    }

    private void assertBadRequest(Form data) throws Exception {
        Status status = doPost(data).getStatus();
        assertEquals("Should be Bad Request", 400, status.getCode());
    }

    private void assertDistanceNode(Form data, String unit, String perUnit) throws Exception {
        DomRepresentation rep = doPost(data).getEntityAsDom();
        rep.write(System.out);
        Document doc = rep.getDocument();
        assertXpathEvaluatesTo("distance", "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/Path", doc);
        assertXpathEvaluatesTo("Distance", "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/Name", doc);
        assertXpathEvaluatesTo(perUnit, "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/PerUnit", doc);
        assertXpathEvaluatesTo(unit, "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/Unit", doc);
        assertXpathEvaluatesTo("distance", "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Path", doc);
        assertXpathEvaluatesTo("Distance", "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Name", doc);
        assertXpathEvaluatesTo("year", "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/PerUnit", doc);
        assertXpathEvaluatesTo("km", "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/ItemValues/ItemValue[1]/ItemValueDefinition/Unit", doc);

    }

    private void assertDateNodes(Form data, String startDate, String endDate, String end) throws Exception {

        DomRepresentation rep = doPost(data).getEntityAsDom();
        rep.write(System.out);
        Document doc = rep.getDocument();
        assertXpathEvaluatesTo(FMT.format(FMT.parse(startDate)), "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/StartDate", doc);
        if (endDate != null) {
            assertXpathEvaluatesTo(FMT.format(FMT.parse(endDate)), "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/EndDate", doc);
        } else {
            assertXpathEvaluatesTo("", "/Resources/ProfileCategoryResource/ProfileItems/ProfileItem/EndDate", doc);
        }
    }

    private String getDefaultDate() {
        return FMT.format(new Date());
    }
}