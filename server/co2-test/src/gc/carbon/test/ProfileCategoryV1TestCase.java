package gc.carbon.test;

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
public class ProfileCategoryV1TestCase extends BaseProfileCategoryTestCase {

    public ProfileCategoryV1TestCase(String name) {
        super(name);
    }

    private void doAssertSimilarXML(Form data) throws Exception {
        Response response = doPost(data);
        setControl("post-transport-car-generic-validfrom_20500101-distanceKmPerMonth_1000.xml");
        assertXMLSimilar(response);
    }
                                
    @Test
    public void testPostWithValidFromAndDistanceKmPerMonth() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distanceKmPerMonth", "1000");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPostWithValidFromAndDistance() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPostWithValidFromAndDistanceAndUnit() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "km");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPostWithValidFromAndDistanceAndPerUnit() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distancePerUnit", "month");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPostWithValidFromAndDistanceAndUnitAndPerUnit() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "km");
        data.add("distancePerUnit", "month");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPostWithStartDateAndDistanceAndUnitAndPerUnit() throws Exception {
        Form data = new Form();
        data.add("startDate", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "km");
        data.add("distancePerUnit", "month");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPostWithStartDateAndDistanceAndUnitInMiAndPerUnit() throws Exception {
        Form data = new Form();
        data.add("startDate", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "month");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("distanceMiPerMonth", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Path", doc);
        assertXpathEvaluatesTo("Distance Mi Per Month", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Name", doc);
        assertXpathEvaluatesTo(KNOWN_AMOUNT_FOR_1000_MI_PER_MONTH, "/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth", doc);
    }

    @Test
    public void testPostWithStartDateAndDistanceAndUnitAndPerUnitInYear() throws Exception {
        Form data = new Form();
        data.add("startDate", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "km");
        data.add("distancePerUnit", "year");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("distanceKmPerYear", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Path", doc);
        assertXpathEvaluatesTo("Distance Km Per Year", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Name", doc);
        assertXpathEvaluatesTo(KNOWN_AMOUNT_FOR_1000_KM_PER_YEAR, "/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth", doc);
    }

    @Test
    public void testPostWithStartDateAndDistanceAndUnitInMiAndPerUnitInYear() throws Exception {
        Form data = new Form();
        data.add("startDate", "20500101");
        data.add("distance", "1000");
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "year");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("distanceMiPerYear", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Path", doc);
        assertXpathEvaluatesTo("Distance Mi Per Year", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Name", doc);
        assertXpathEvaluatesTo(KNOWN_AMOUNT_FOR_1000_MI_PER_YEAR, "/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth", doc);
    }
}
