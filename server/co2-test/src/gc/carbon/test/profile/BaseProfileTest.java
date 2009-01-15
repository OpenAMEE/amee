package gc.carbon.test.profile;

import gc.carbon.test.APITestCase;
import gc.carbon.test.TestClient;

import java.text.SimpleDateFormat;

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
public class BaseProfileTest extends APITestCase {

    protected static final String KNOWN_AMOUNT_FOR_1000_KM_PER_MONTH = "264.500";
    protected static final String KNOWN_AMOUNT_FOR_1000_MI_PER_MONTH = "425.671";
    protected static final String KNOWN_AMOUNT_FOR_1000_KM_PER_YEAR = "22.042";
    protected static final String KNOWN_AMOUNT_FOR_1000_MI_PER_YEAR = "35.473";

    protected static final String PROFILE = "B74EC806243F";
    protected static final String CATEGORY = "/transport/car/generic/";
    protected static final String DATA_CATEGORY_UID = "4F6CBCEE95F7";

    protected TestClient client;

    protected String ISO_DATE = "yyyy-MM-dd'T'HH:mmZ";
    protected SimpleDateFormat FMT = new SimpleDateFormat(ISO_DATE);

    public BaseProfileTest() {
        super();    
    }

    public BaseProfileTest(String s) throws Exception {
        super(s);
        client = new TestClient(PROFILE, CATEGORY);
    }

}
