package gc.carbon.builder;

import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.restlet.data.Form;

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
public class APIVersion {

    public static final APIVersion ONE_ZERO = new APIVersion("1.0");
    public static final APIVersion TWO_ZERO = new APIVersion("2.0");

    private String version;

    private APIVersion(String version) {
        this.version = version;
    }

    public static APIVersion get(Form form) {
        if ("2.0".equals(form.getFirstValue("v"))) {
            return APIVersion.TWO_ZERO;
        } else {
            return APIVersion.ONE_ZERO;
        }
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("v", version);
        return obj;
    }

    public Element getElement(Document document) {
        return APIUtils.getElement(document, "APIVersion", version);
    }

}

