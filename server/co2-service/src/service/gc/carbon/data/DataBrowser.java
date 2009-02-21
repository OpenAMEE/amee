/**
 * This file is part of AMEE.
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package gc.carbon.data;

import com.jellymold.kiwi.ResourceActions;
import com.jellymold.kiwi.environment.EnvironmentService;
import gc.carbon.BaseBrowser;
import gc.carbon.definition.DefinitionServiceDAO;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dataBrowser")
@Scope("prototype")
public class DataBrowser extends BaseBrowser {

    @Autowired
    @Qualifier("dataCategoryActions")
    private ResourceActions dataCategoryActions;

    @Autowired
    @Qualifier("dataItemActions")
    private ResourceActions dataItemActions;

    // DataCategories
    private DataCategory dataCategory = null;

    public DataBrowser() {
        super();
    }

    // Actions
    public ResourceActions getDataCategoryActions() {
        return dataCategoryActions;
    }

    public ResourceActions getDataItemActions() {
        return dataItemActions;
    }

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(DataCategory dataCategory) {
        this.dataCategory = dataCategory;
    }
}