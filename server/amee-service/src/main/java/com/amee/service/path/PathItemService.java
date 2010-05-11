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
package com.amee.service.path;

import com.amee.base.transaction.TransactionController;
import com.amee.domain.ObjectType;
import com.amee.domain.cache.CacheHelper;
import com.amee.domain.data.DataCategory;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.invalidation.InvalidationMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class PathItemService implements ApplicationListener {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    private CacheHelper cacheHelper = CacheHelper.getInstance();

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof InvalidationMessage) {
            log.debug("onApplicationEvent() Handling InvalidationMessage.");
            InvalidationMessage invalidationMessage = (InvalidationMessage) event;
            if (invalidationMessage.getObjectType().equals(ObjectType.DC)) {
                transactionController.begin(false);
                Environment environment = environmentService.getEnvironmentByName("AMEE");
                DataCategory dataCategory = dataService.getDataCategoryByUid(invalidationMessage.getEntityUid(), true);
                if (dataCategory != null) {
                    update(environment, dataCategory);
                } else {
                    remove(environment, invalidationMessage.getEntityUid());
                }
                transactionController.end();
            }
        }
    }

    public void update(Environment environment, DataCategory dataCategory) {
        PathItemGroup pig = getPathItemGroup(environment);
        PathItem pathItem = pig.findByUId(dataCategory.getUid());
        if (pathItem != null) {
            pathItem.update(dataCategory);
        } else {
            // TODO: Can we add a new DataCategory without clearing the cache?
            removePathItemGroup(environment);
        }
    }

    public void remove(Environment environment, String uid) {
        PathItemGroup pig = getPathItemGroup(environment);
        pig.remove(pig.findByUId(uid));
    }

    public PathItemGroup getPathItemGroup(Environment environment) {
        EnvironmentPIGFactory environmentPIGFactory = new EnvironmentPIGFactory(dataService, environment);
        return (PathItemGroup) cacheHelper.getCacheable(environmentPIGFactory);
    }

    public void removePathItemGroup(Environment environment) {
        cacheHelper.remove("EnvironmentPIGs", environment.getUid());
    }
}
