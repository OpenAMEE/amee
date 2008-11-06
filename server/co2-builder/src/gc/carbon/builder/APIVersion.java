package gc.carbon.builder;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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
public enum APIVersion {

    ONE("1.0"),
    TWO("2.0");

    private String version;

    private static final Map<String,APIVersion> versionStringToAPIVersionMap = new HashMap<String,APIVersion>();
    static {
        for (APIVersion v : EnumSet.allOf(APIVersion.class)) {
            versionStringToAPIVersionMap.put(v.version, v);
        }
    }

    private APIVersion(String version) {
        this.version = version;
    }

    public static APIVersion version(String version) {
        return versionStringToAPIVersionMap.get(version);
    }
}

