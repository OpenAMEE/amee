 package gc.carbon.profile;

 import gc.carbon.builder.APIVersion;
 import gc.carbon.builder.mapper.LegacyDataMapper;
 import gc.carbon.builder.mapper.LegacyItemValueMapper;
 import gc.carbon.domain.profile.StartEndDate;
 import org.restlet.data.Form;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;

 import java.util.Date;

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
public class ProfileForm extends Form {

    //private APIVersion apiVersion = APIVersion.ONE;
    private APIVersion apiVersion;

    public ProfileForm() {
        super();
    }

    public ProfileForm(Form form) {
        super(form.getQueryString());

        // Read API version as a parameter - may move to a header
        apiVersion = APIVersion.get(form);

        if (apiVersion.equals(APIVersion.ONE_ZERO)) {
            mapLegacyParameters();
        }
    }

    private void mapLegacyParameters() {

        for (String name : getNames()) {
            if (LegacyDataMapper.canMap(name)) {

                add(LegacyItemValueMapper.getCurrentPath(name),getFirstValue(name));
                add(LegacyItemValueMapper.getCurrentPath(name)+"Unit", LegacyDataMapper.getUnit(name));
                add(LegacyItemValueMapper.getCurrentPath(name)+"PerUnit", LegacyDataMapper.getPerUnit(name));
                removeFirst(name);
            }
        }
    }

    public APIVersion getVersion() {
        return apiVersion;
    }

    public boolean isVersionOne() {
      return getVersion().equals(APIVersion.ONE_ZERO);
    }
}