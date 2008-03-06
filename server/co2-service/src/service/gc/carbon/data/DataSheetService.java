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

import com.jellymold.sheet.Sheet;
import com.jellymold.utils.cache.CacheHelper;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;

@Name("dataSheetService")
@Scope(ScopeType.EVENT)
public class DataSheetService implements Serializable {

    private final static Logger log = Logger.getLogger(DataSheetService.class);

    @In(create = true)
    private DataSheetFactory dataSheetFactory;

    private CacheHelper cacheHelper = CacheHelper.getInstance();

    public DataSheetService() {
        super();
    }

    public Sheet getSheet(DataCategory dataCategory) {
        dataSheetFactory.setDataCategory(dataCategory);
        return (Sheet) cacheHelper.getCacheable(dataSheetFactory);
    }

    public void removeSheet(DataCategory dataCategory) {
        cacheHelper.remove("DataSheets", "DataSheet_" + dataCategory.getUid());
    }
}