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
    private DefinitionServiceDAO definitionServiceDAO;

    @Autowired
    protected DataServiceDAO dataServiceDAO;

    @Autowired
    @Qualifier("dataCategoryActions")
    private ResourceActions dataCategoryActions;

    @Autowired
    @Qualifier("dataItemActions")
    private ResourceActions dataItemActions;

    // DataCategories
    private DataCategory dataCategory = null;
    private String dataCategoryUid = null;

    // DataItems
    private DataItem dataItem = null;
    private String dataItemUid = null;

    // ItemValues
    private String itemValueUid = null;
    private ItemValue itemValue = null;

    // ItemDefinitions
    private List<ItemDefinition> itemDefinitions = null;

    public DataBrowser() {
        super();
    }

    // General

    public String getFullPath() {
        if (pathItem != null) {
            return "/data" + pathItem.getFullPath();
        } else {
            return "/data";
        }
    }

    // Actions

    public ResourceActions getDataCategoryActions() {
        return dataCategoryActions;
    }

    public ResourceActions getDataItemActions() {
        return dataItemActions;
    }

    // DataCategories

    public String getDataCategoryUid() {
        return dataCategoryUid;
    }

    public void setDataCategoryUid(String dataCategoryUid) {
        this.dataCategoryUid = dataCategoryUid;
    }

    public DataCategory getDataCategory() {
        if (dataCategory == null) {
            if (dataCategoryUid != null) {
                dataCategory = dataServiceDAO.getDataCategory(dataCategoryUid);
            }
        }
        return dataCategory;
    }

    public void setDataCategory(DataCategory dataCategory) {
        this.dataCategory = dataCategory;
    }

    // DataItems

    public String getDataItemUid() {
        return dataItemUid;
    }

    public void setDataItemUid(String dataItemUid) {
        this.dataItemUid = dataItemUid;
    }

    public DataItem getDataItem() {
        if (dataItem == null) {
            if (dataItemUid != null) {
                dataItem = dataServiceDAO.getDataItem(dataItemUid);
            }
        }
        return dataItem;
    }

    // ItemValues

    public String getItemValueUid() {
        return itemValueUid;
    }

    public void setItemValueUid(String itemValueUid) {
        this.itemValueUid = itemValueUid;
    }

    public ItemValue getItemValue() {
        if (itemValue == null) {
            if (itemValueUid != null) {
                itemValue = dataServiceDAO.getItemValue(itemValueUid);
            }
        }
        return itemValue;
    }

    // ItemDefinitions

    public List<ItemDefinition> getItemDefinitions() {
        if (itemDefinitions == null) {
            itemDefinitions = definitionServiceDAO.getItemDefinitions(EnvironmentService.getEnvironment());
        }
        return itemDefinitions;
    }
}