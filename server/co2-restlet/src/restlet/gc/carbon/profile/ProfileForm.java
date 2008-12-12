package gc.carbon.profile;

import gc.carbon.APIVersion;
import gc.carbon.domain.mapper.LegacyDataMapper;
import gc.carbon.domain.mapper.LegacyItemValueMapper;
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
public class ProfileForm extends Form {

    private APIVersion apiVersion;

    private ProfileForm() {
        super();
    }

    private ProfileForm(Form form) {
        super(form.getQueryString());
    }

    public ProfileForm(APIVersion apiVersion) {
        this();
        init(apiVersion);
    }

    public ProfileForm(Form form, APIVersion apiVersion) {
        this(form);
        init(apiVersion);
    }

    private void init(APIVersion apiVersion) {
        this.apiVersion = apiVersion;
        if (apiVersion.isVersionOne()) {
            mapLegacyParameters();
        }
    }

    private void mapLegacyParameters() {
        for (String name : getNames()) {
            if (LegacyDataMapper.canMap(name)) {
                add(LegacyItemValueMapper.getCurrentPath(name), getFirstValue(name));
                add(LegacyItemValueMapper.getCurrentPath(name) + "Unit", LegacyDataMapper.getUnit(name));
                add(LegacyItemValueMapper.getCurrentPath(name) + "PerUnit", LegacyDataMapper.getPerUnit(name));
                removeFirst(name);
            }
        }
    }

    public APIVersion getVersion() {
        return apiVersion;
    }
}