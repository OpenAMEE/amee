package gc.carbon.builder.mapper;

import gc.carbon.builder.mapper.LegacyDataMapper;
import gc.carbon.builder.domain.BuildableItem;
import gc.carbon.builder.domain.BuildableItemValue;
import gc.carbon.builder.domain.*;

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
public class LegacyItemValueMapper extends LegacyDataMapper {
 
    private BuildableItemValue itemValue;

    public LegacyItemValueMapper(BuildableItemValue itemValue) {
        this.itemValue = itemValue;
    }

    public String getPath() {
     return getLegacyPath(itemValue.getPath(),itemValue.getUnit(),itemValue.getPerUnit());
    }

    public String getName() {
        return getLegacyName(itemValue.getPath(),itemValue.getUnit(),itemValue.getPerUnit());
    }


    public BuildableItemValueDefinition getItemValueDefinition() {
        return itemValue.getItemValueDefinition();
    }

    public String getUid() {
        return itemValue.getUid();
    }

    public String getValue() {
        return itemValue.getValue();
    }

    public Date getModified() {
        return itemValue.getModified();
    }

    public Date getCreated() {
        return itemValue.getCreated();
    }

    public BuildableItem getItem() {
        return itemValue.getItem();
    }

}
