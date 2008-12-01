package gc.carbon.test.data;

import gc.carbon.test.APITestCase;
import gc.carbon.test.TestClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.restlet.data.Form;
import org.restlet.resource.DomRepresentation;

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
public class BaseDataTest extends APITestCase {

    protected static final DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyyMMdd'T'HHmm");
    protected static final String CATEGORY = "/data/home/energy/quantity";

    protected TestClient client;

    public BaseDataTest(String s) throws Exception {
        super(s);
        client = new TestClient(CATEGORY);
    }

    protected String create(DateTime startDate, DateTime endDate) throws Exception {
        Form data = new Form();
        data.add("newObjectType","DI");
        data.add("type","diesel");
        data.add("startDate",startDate.toString(FMT));
        if (endDate != null)
            data.add("endDate",endDate.toString(FMT));
        return client.createDateItem(data);
    }
}
