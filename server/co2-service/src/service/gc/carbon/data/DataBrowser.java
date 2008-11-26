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
import com.jellymold.kiwi.auth.AuthService;
import gc.carbon.BaseBrowser;
import gc.carbon.definition.DefinitionService;
import gc.carbon.domain.data.*;
import gc.carbon.domain.profile.StartEndDate;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.restlet.data.Form;

import java.util.List;
import java.util.Date;
import java.util.Calendar;

@Name("dataBrowser")
@Scope(ScopeType.EVENT)
public class DataBrowser extends BaseBrowser {

    private final static Logger log = Logger.getLogger(DataBrowser.class);

    @In(create = true)
    private AuthService authService;

    @In(create = true)
    private DefinitionService definitionService;

    // DataCategories
    private String dataCategoryUid = null;
    private DataCategory dataCategory = null;
    private ResourceActions dataCategoryActions = new ResourceActions("dataCategory");

    // upload
    private Boolean allowDataUpload = null;

    // DataItems
    private String dataItemUid = null;
    private DataItem dataItem = null;
    private ResourceActions dataItemActions = new ResourceActions("dataItem");

    // ItemValues
    private String itemValueUid = null;
    private ItemValue itemValue = null;

    // ItemDefinitions
    private List<ItemDefinition> itemDefinitions = null;

    // General

    public String getFullPath() {
        if (pathItem != null) {
            return "/data" + pathItem.getFullPath();
        } else {
            return "/data";
        }
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
                dataCategory = dataService.getDataCategory(environment, dataCategoryUid);
            }
        }
        return dataCategory;
    }

    public void setDataCategory(DataCategory dataCategory) {
        this.dataCategory = dataCategory;    
    }

    public ResourceActions getDataCategoryActions() {
        return dataCategoryActions;
    }

    public boolean isAllowDataUpload() {
        if (allowDataUpload == null) {
            allowDataUpload = authService.isSuperUser() || authService.hasActions(DataConstants.ACTION_DATA_UPLOAD);
        }
        return allowDataUpload;
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
                Item item = dataService.getItem(environment, dataItemUid);
                if (item instanceof DataItem) {
                    dataItem = (DataItem) item;
                }
            }
        }
        return dataItem;
    }

    public ResourceActions getDataItemActions() {
        return dataItemActions;
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
            if ((itemValueUid != null) && (getDataItem() != null)) {
                itemValue = dataService.getItemValue(dataItem, itemValueUid);
            }
        }
        return itemValue;
    }

    // ItemDefinitions

    public List<ItemDefinition> getItemDefinitions() {
        if (itemDefinitions == null) {
            itemDefinitions = definitionService.getItemDefinitions(environment);
        }
        return itemDefinitions;
    }

}