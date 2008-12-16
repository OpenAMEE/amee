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
package gc.carbon.domain.mapper;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.domain.APIObject;
import gc.carbon.domain.data.builder.BuildableItemValueDefinition;

import java.util.Date;

public class LegacyItemValueDefinitionMapper extends LegacyDataMapper {

    private BuildableItemValueDefinition itemValueDefinition;

    public LegacyItemValueDefinitionMapper(BuildableItemValueDefinition itemValueDefinition) {
        this.itemValueDefinition = itemValueDefinition;
    }

    public String getPath() {
        String legacyPath = getLegacyPath(itemValueDefinition.getPath(),
                itemValueDefinition.getUnit().toString(), itemValueDefinition.getPerUnit().toString());
        if (legacyPath == null) {
            return itemValueDefinition.getPath();
        } else {
            return legacyPath;
        }
    }

    public String getName() {
        String legacyName = getLegacyName(itemValueDefinition.getPath(),
                itemValueDefinition.getUnit().toString(), itemValueDefinition.getPerUnit().toString());
        if (legacyName == null) {
            return itemValueDefinition.getName();
        } else {
            return legacyName;
        }
    }

    public String getUid() {
        return itemValueDefinition.getUid();
    }

    public String getValue() {
        return itemValueDefinition.getValue();
    }

    public Date getModified() {
        return itemValueDefinition.getModified();
    }

    public Date getCreated() {
        return itemValueDefinition.getCreated();
    }

    public boolean isFromProfile() {
        return itemValueDefinition.isFromProfile();
    }

    public boolean isFromData() {
        return itemValueDefinition.isFromData();
    }

    public String getChoices() {
        return itemValueDefinition.getChoices();
    }

    public String getAllowedRoles() {
        return itemValueDefinition.getAllowedRoles();
    }

    public APIObject getValueDefinition() {
        return itemValueDefinition.getValueDefinition();
    }

    public Environment getEnvironment() {
        return itemValueDefinition.getEnvironment();
    }

    public APIObject getItemDefinition() {
        return itemValueDefinition.getItemDefinition();
    }
}
