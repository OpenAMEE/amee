package gc.carbon;

import com.jellymold.utils.domain.APIUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class APIVersion {

    public static final APIVersion ONE_ZERO = new APIVersion("1.0");
    public static final APIVersion TWO_ZERO = new APIVersion("2.0");
    private static final List<APIVersion> versions = new ArrayList<APIVersion>();

    static {
        versions.add(ONE_ZERO);
        versions.add(TWO_ZERO);
    }

    private String version;

    /**
     * Default constructor (initialises version to ONE_ZERO)
     *
     * @see #ONE_ZERO
     */
    public APIVersion() {
        this.version = APIVersion.ONE_ZERO.version;
    }

    public APIVersion(String version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APIVersion)) return false;
        return o.toString().equals(version);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return version;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("v", version);
        return obj;
    }

    public Element getElement(Document document) {
        return APIUtils.getElement(document, "APIVersion", version);
    }

    public boolean isVersionOne() {
        return equals(ONE_ZERO);
    }

    public boolean isVersionTwo() {
        return equals(TWO_ZERO);
    }

    public static List<APIVersion> getVersions() {
        return versions;
    }
}