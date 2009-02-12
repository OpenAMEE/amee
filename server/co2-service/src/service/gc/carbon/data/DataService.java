package gc.carbon.data;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import gc.carbon.data.DataServiceDAO;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.path.PathItemService;
import gc.carbon.definition.DefinitionServiceDAO;
import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.User;
import com.jellymold.sheet.Sheet;
import com.jellymold.sheet.Choices;
import com.jellymold.utils.ThreadBeanHolder;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.math.BigDecimal;

/**
 * Primary service interface to Data Resources.
 *
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
@Service
public class DataService {

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
    private Calculator calculator;

    public void clearCaches(DataCategory dc) {
        pathItemService.removePathItemGroup(dc.getEnvironment());
        dataSheetService.removeSheet(dc);
    }

    public DataCategory getDataCategory(String dataCategoryUid) {
        return dao.getDataCategory(dataCategoryUid);
    }

    public DataItem getDataItem(String dataItemUid) {
        return dao.getDataItem(dataItemUid);
    }

    public List<DataItem> getDataItems(Environment env) {
        return dao.getDataItems(env);
    }

    public List<DataCategory> getDataCategories(Environment env) {
        return dao.getDataCategories(env);
    }

    public List<DataItem> getDataItems(DataCategory dc, StartEndDate startDate) {
        return getDataItems(dc, startDate, null);
    }

    public List<DataItem> getDataItems(DataCategory dc, StartEndDate startDate, StartEndDate endDate) {
        return dao.getDataItems(dc, startDate, endDate);
    }

    public List<DataItem> getDataItems(DataCategory dc) {
        return dao.getDataItems(dc);
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

    public Choices getUserValueChoices(DataItem di) {
        return dao.getUserValueChoices(di);    
    }

    public BigDecimal calculate(DataItem di, Choices userValueChoices) {
        //TODO - Need to remove ad-hoc usage of ThreadBeanHolder
        User user = (User) ThreadBeanHolder.get("user");
        return calculator.calculate(di, userValueChoices, user.getApiVersion());
    }

    public Sheet getSheet(DataBrowser browser) {
       return dataSheetService.getSheet(browser);
    }

    public ItemDefinition getItemDefinition(Environment env, String itemDefinitionUid) {
        return definitionServiceDAO.getItemDefinition(env, itemDefinitionUid);
    }
}
