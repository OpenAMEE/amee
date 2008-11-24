package gc.carbon.test.profile;

import org.junit.Test;
import org.restlet.data.*;
import org.w3c.dom.Document;

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
public class ProfileItemPUTV1 extends BaseProfileItemTestCase {

    public ProfileItemPUTV1(String name) {
        super(name);
    }

    private void doAssertSimilarXML(Form data) throws Exception {
        Response response = doPut(data);
        setControl("put-transport-car-generic.xml");
        assertXMLSimilar(response);
    }

    @Test
    public void testPutWithValidFromAndDistanceKmPerMonth() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distanceKmPerMonth", "1000");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPutWithValidFromAndDistance() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPutWithValidFromAndDistanceAndUnit() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "km");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPutWithValidFromAndDistanceAndPerUnit() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distancePerUnit", "month");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPutWithValidFromAndDistanceAndUnitAndPerUnit() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "km");
        data.add("distancePerUnit", "month");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPutWithValidFromAndDistanceAndUnitInMiAndPerUnit() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "month");
        Document doc = doPut(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("distanceMiPerMonth", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue/Path", doc);
        assertXpathEvaluatesTo("Distance Mi Per Month", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue/Name", doc);
        assertXpathEvaluatesTo(KNOWN_AMOUNT_FOR_1000_MI_PER_MONTH, "/Resources/ProfileItemResource/ProfileItem/AmountPerMonth", doc);
    }

    @Test
    public void testPutWithValidFromAndDistanceAndUnitAndPerUnitInYear() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "km");
        data.add("distancePerUnit", "year");
        Document doc = doPut(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("distanceKmPerYear", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue/Path", doc);
        assertXpathEvaluatesTo("Distance Km Per Year", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue/Name", doc);
        assertXpathEvaluatesTo(KNOWN_AMOUNT_FOR_1000_KM_PER_YEAR, "/Resources/ProfileItemResource/ProfileItem/AmountPerMonth", doc);
    }

    @Test
    public void testPutWithValidFromAndDistanceAndUnitInMiAndPerUnitInYear() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "year");
        Document doc = doPut(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("distanceMiPerYear", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue/Path", doc);
        assertXpathEvaluatesTo("Distance Mi Per Year", "/Resources/ProfileItemResource/ProfileItem/ItemValues/ItemValue/Name", doc);
        assertXpathEvaluatesTo(KNOWN_AMOUNT_FOR_1000_MI_PER_YEAR, "/Resources/ProfileItemResource/ProfileItem/AmountPerMonth", doc);
    }

    @Test
    public void testPutWithStartDate() throws Exception {
        Form data = new Form();
        data.add("startDate", "20100401T0000");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testPutWithEndDate() throws Exception {
        Form data = new Form();
        data.add("endDate", "20100401T0000");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testPutWithDuration() throws Exception {
        Form data = new Form();
        data.add("duration", "PT30M");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }


}