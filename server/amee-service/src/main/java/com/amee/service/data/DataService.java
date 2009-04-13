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
package com.amee.service.data;

import com.amee.domain.APIVersion;
import com.amee.domain.UidGen;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValue;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.StartEndDate;
import com.amee.domain.sheet.Choices;
import com.amee.domain.sheet.Sheet;
import com.amee.service.definition.DefinitionServiceDAO;
import com.amee.service.path.PathItemService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Iterator;
import java.util.List;

/**
 * Primary service interface to Data Resources.
 */
@Service
public class DataService {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DataServiceDAO dao;

    @Autowired
    private DefinitionServiceDAO definitionServiceDAO;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private DrillDownService drillDownService;

    public void clearCaches(DataCategory dc) {
        log.debug("clearCaches()");
        drillDownService.clearDrillDownCache();
        pathItemService.removePathItemGroup(dc.getEnvironment());
        dataSheetService.removeSheet(dc);
    }

    public DataCategory getDataCategory(String dataCategoryUid) {
        return dao.getDataCategoryByUid(dataCategoryUid);
    }

    public DataItem getDataItem(String path) {
        DataItem dataItem = null;
        if (!StringUtils.isBlank(path)) {
            if (UidGen.isValid(path)) {
                dataItem = getDataItemByUid(path);
            }
            if (dataItem == null) {
                dataItem = getDataItemByPath(path);
            }
        }
        return dataItem;
    }

    public DataItem getDataItemByUid(String uid) {
        return dao.getDataItemByUid(uid);
    }

    public DataItem getDataItemByPath(String path) {
        return dao.getDataItemByPath(path);
    }

    public List<DataCategory> getDataCategories(Environment env) {
        return dao.getDataCategories(env);
    }

    public List<DataItem> getDataItems(DataCategory dc, StartEndDate startDate) {
        return getDataItems(dc, startDate, null);
    }

    //TODO: Date logic here should share code in com.amee.service.data.DrillDownDAO#isWithinTimeFrame
    public List<DataItem> getDataItems(DataCategory dc, StartEndDate startDate, StartEndDate endDate) {
        DataItem dataItem;
        List<DataItem> dataItems = dao.getDataItems(dc);
        Iterator<DataItem> i = dataItems.iterator();
        if (endDate != null) {
            while (i.hasNext()) {
                dataItem = i.next();
                if (!(dataItem.getStartDate().before(endDate) && ((dataItem.getEndDate() == null) || dataItem.getEndDate().after(startDate)))) {
                    i.remove();
                }
            }
        } else {
            while (i.hasNext()) {
                dataItem = i.next();
                if (!((dataItem.getEndDate() == null) || dataItem.getEndDate().after(startDate))) {
                    i.remove();
                }
            }
        }

        return dataItems;
    }

    public List<DataItem> getDataItems(DataCategory dc) {
        return dao.getDataItems(dc);
    }

    public ItemValue getItemValue(String uid) {
        return dao.getItemValueByUid(uid);
    }

    public void persist(DataCategory dc) {
        em.persist(dc);
    }

    public void persist(DataItem di) {
        em.persist(di);
        dao.checkDataItem(di);
    }

    public void remove(DataCategory dc) {
        dao.remove(dc);
    }

    public void remove(DataItem di) {
        dao.remove(di);
    }

    public Choices getUserValueChoices(DataItem di, APIVersion apiVersion) {
        return dao.getUserValueChoices(di, apiVersion);
    }

    public Sheet getSheet(DataBrowser browser) {
        return dataSheetService.getSheet(browser);
    }

    public ItemDefinition getItemDefinition(Environment env, String itemDefinitionUid) {
        return definitionServiceDAO.getItemDefinition(env, itemDefinitionUid);
    }
}
