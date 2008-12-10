package gc.carbon.test.profile.v2;

import org.restlet.data.Form;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
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
public class ProfileCategoryTest extends BaseProfileCategoryTest {

    public ProfileCategoryTest(String name) throws Exception {
        super(name);
    }

    @Test
    public void testPostWithExternalPerUnitAsMonth() throws Exception {
        Form data = new Form();
        data.add("distancePerUnit", "month");
        data.add("distance", "1000");
        client.addQueryParameter("v","2.0");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("3174.000", "/Resources/ProfileCategoryResource/ProfileItem/Amount", doc);
        assertXpathEvaluatesTo("1000", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Value", doc);
    }

    @Test
    public void testPostWithExternalUnitAsMile() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distance", "1000");
        client.addQueryParameter("v","2.0");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("425.671", "/Resources/ProfileCategoryResource/ProfileItem/Amount", doc);
        assertXpathEvaluatesTo("1000", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Value", doc);
    }

    @Test
    public void testPostWithExternalUnitAsMileAndPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distanceUnit", "mi");
        data.add("distancePerUnit", "month");
        data.add("distance", "1000");
        client.addQueryParameter("v","2.0");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("5108.058", "/Resources/ProfileCategoryResource/ProfileItem/Amount", doc);
        assertXpathEvaluatesTo("1000", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Value", doc);
    }
}