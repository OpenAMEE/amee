package gc.carbon.test.profile;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.restlet.data.Form;
import org.restlet.resource.DomRepresentation;
import org.testng.annotations.Test;
import gc.carbon.test.TestClient;

import java.util.ArrayList;
import java.util.List;

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
public class MeterReadingTest extends BaseProfileCategoryTest {

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd'T'HHmm");

    private static List<String> readings = new ArrayList<String>();

    private static DateTime startDate = new DateTime();
    private static DateTime endDate = startDate.plusHours(1);

    private static boolean initialised = false;

    public MeterReadingTest(String s) throws Exception {
        super(s);
        setUpProfileItems();
    }

    private void setUpProfileItems() throws Exception {
        if (initialised)
            return;

        initDB();

        client = new TestClient(PROFILE,"/home/energy/electricity");
        client.addQueryParameter("v","2.0");

        DateTime readingDate = startDate.minusHours(1);
        while(readingDate.isBefore(endDate.plusHours(1))) {
            readings.add(create(readingDate));
            readingDate = readingDate.plusMinutes(30);
        }

        initialised = true;
    }

    private String create(DateTime startDate) throws Exception {
        Form data = new Form();
        data.add("dataItemUid","35178D0DB2FD");
        data.add("startDate",startDate.toString(fmt));
        data.add("kWh","30");
        data.add("kWhPerUnit","none");
        data.add("duration","PT30M");
        data.add("name", System.currentTimeMillis()+"");
        return client.createProfileItem(data);
    }

    @Test
    public void test() throws Exception {
        client.addQueryParameter("startDate", startDate.toString(fmt));
        DomRepresentation rep = client.get().getEntityAsDom();
        rep.write(System.out);
    }

}
