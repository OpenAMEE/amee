package gc.carbon.test.data;

import org.restlet.resource.DomRepresentation;
import org.junit.Test;
import org.w3c.dom.Document;
import org.joda.time.DateTime;

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
public class DataCategoryTest extends BaseDataTest {

    public DataCategoryTest(String name) throws Exception {
        super(name);
    }

    @Test
    public void testSupercededNotReturned() throws Exception {

        DateTime startDate = new DateTime();

        String before_and_ongoing_named = create(startDate.minusDays(2), null);
        String before_and_ongoing2_named = create(startDate.minusDays(1), null);
        String on_and_ongoing_named = create(startDate, null);
        String inside_and_ongoing_named = create(startDate.plusDays(1), null);
        String inside_and_ongoing2_named = create(startDate.plusDays(2), null);

        System.out.println("before_and_ongoing_named  : " + before_and_ongoing_named);
        System.out.println("before_and_ongoing1_named : " + before_and_ongoing2_named);
        System.out.println("on_and_ongoing_named      : " + on_and_ongoing_named);
        System.out.println("inside_and_ongoing_named  : " + inside_and_ongoing_named);
        System.out.println("inside_and_ongoing2_named : " + inside_and_ongoing2_named);

        client.addQueryParameter("startDate",startDate.toString(FMT));
        DomRepresentation rep = client.get().getEntityAsDom();
        Document doc = rep.getDocument();
        assertXpathNotExists("//DataItem[@uid='" + before_and_ongoing_named + "']", doc);
        assertXpathNotExists("//DataItem[@uid='" + before_and_ongoing2_named + "']", doc);
        assertXpathExists("//DataItem[@uid='" + on_and_ongoing_named + "']", doc);
        assertXpathExists("//DataItem[@uid='" + inside_and_ongoing_named + "']", doc);
        assertXpathExists("//DataItem[@uid='" + inside_and_ongoing2_named + "']", doc);
    }
}
