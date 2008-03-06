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
package gc.carbon.path;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.cache.Cacheable;
import com.jellymold.utils.cache.CacheableFactory;
import gc.carbon.data.DataCategory;
import gc.carbon.data.DataItem;
import gc.carbon.data.DataService;
import gc.carbon.definition.DefinitionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.List;

@Name("environmentPIGFactory")
@Scope(ScopeType.EVENT)
public class EnvironmentPIGFactory extends BasePIGFactory implements CacheableFactory {

    @In(create = true)
    private DefinitionService definitionService;

    @In(create = true)
    private DataService dataService;

    private Environment environment;

    public EnvironmentPIGFactory() {
        super();
    }

    // TODO: This uses up lots of memory, so what?
    public Cacheable createCacheable() {
        PathItemGroup pathItemGroup = null;
        List<DataCategory> dataCategories = dataService.getDataCategories(environment);
        DataCategory rootDataCategory = findRootDataCategory(dataCategories);
        if (rootDataCategory != null) {
            pathItemGroup = new PathItemGroup(new PathItem(rootDataCategory));
            while (!dataCategories.isEmpty()) {
                addDataCategories(pathItemGroup, dataCategories);
            }
            definitionService.getItemDefinitions(environment); // preload so we can iterate over ItemValues later
            List<DataItem> dataItems = dataService.getDataItems(environment);
            while (!dataItems.isEmpty()) {
                addDataItems(pathItemGroup, dataItems);
            }
        }
        return pathItemGroup;
    }

    public String getKey() {
        return environment.getUid();
    }

    public String getCacheName() {
        return "EnvironmentPIGs";
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}