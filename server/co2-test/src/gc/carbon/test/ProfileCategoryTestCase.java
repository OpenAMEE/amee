package gc.carbon.test;

import org.testng.annotations.Test;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.MediaType;
import org.restlet.resource.DomRepresentation;
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
public class ProfileCategoryTestCase extends BaseProfileCategoryTestCase {

    public ProfileCategoryTestCase(String name) {
        super(name);
    }

    @Test
    public void testPostWithExternalPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distancePerUnit","year");
        data.add("distance", "1000");
        data.add("v", "2.0");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("22.042", "/Resources/ProfileCategoryResource/ProfileItem/Amount", doc);
        assertXpathEvaluatesTo("1000", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Value", doc);
    }

    @Test
    public void testPostWithExternalUnitAsMile() throws Exception {
        Form data = new Form();
        data.add("distanceUnit","mi");
        data.add("distance", "1000");
        data.add("v", "2.0");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("22.042", "/Resources/ProfileCategoryResource/ProfileItem/Amount", doc);
        assertXpathEvaluatesTo("1000", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Value", doc);
    }

    @Test
    public void testPostWithExternalUnitAsMileAndPerUnitAsYear() throws Exception {
        Form data = new Form();
        data.add("distanceUnit","mi");
        data.add("distancePerUnit","year");
        data.add("distance", "1000");
        data.add("v", "2.0");
        Document doc = doPost(data).getEntityAsDom().getDocument();
        assertXpathEvaluatesTo("22.042", "/Resources/ProfileCategoryResource/ProfileItem/Amount", doc);
        assertXpathEvaluatesTo("1000", "/Resources/ProfileCategoryResource/ProfileItem/ItemValues/ItemValue/Value", doc);
    }
}