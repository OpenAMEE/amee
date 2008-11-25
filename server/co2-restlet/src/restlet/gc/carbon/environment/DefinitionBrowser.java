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
package gc.carbon.environment;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.ResourceActions;
import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.BaseBrowser;
import gc.carbon.definition.DefinitionService;
import gc.carbon.domain.ValueDefinition;
import gc.carbon.domain.data.Algorithm;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValueDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
@Scope("prototype")
public class DefinitionBrowser extends BaseBrowser {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private DefinitionService definitionService;

    // Environments
    private String environmentUid = null;
    private Environment environment = null;
    private ResourceActions environmentActions = new ResourceActions("environment");

    // Environment Sites
    private String siteUid = null;
    private Site site = null;
    private ResourceActions siteActions = new ResourceActions("site");

    // ItemDefinitions
    private String valueDefinitionUid = null;
    private ValueDefinition valueDefinition = null;
    private ResourceActions valueDefinitionActions = new ResourceActions("valueDefinition");

    // Algorithms
    private String algorithmUid = null;
    private Algorithm algorithm = null;
    private ResourceActions algorithmActions = new ResourceActions("algorithm");

    // ItemDefinitions
    private String itemDefinitionUid = null;
    private ItemDefinition itemDefinition = null;
    private ResourceActions itemDefinitionActions = new ResourceActions("itemDefinition");

    // ItemValueDefinitions
    private String itemValueDefinitionUid = null;
    private ItemValueDefinition itemValueDefinition = null;
    private ResourceActions itemValueDefinitionActions = new ResourceActions("itemValueDefinition");

    public DefinitionBrowser() {
        super();
    }

    // Environments

    public String getEnvironmentUid() {
        return environmentUid;
    }

    public void setEnvironmentUid(String environmentUid) {
        this.environmentUid = environmentUid;
    }

    public Environment getEnvironment() {
        if (environment == null) {
            if (environmentUid != null) {
                environment = environmentService.getEnvironmentByUid(getEnvironmentUid());
            }
        }
        return environment;
    }

    public ResourceActions getEnvironmentActions() {
        return environmentActions;
    }

    // Environment Sites

    public String getSiteUid() {
        return siteUid;
    }

    public void setSiteUid(String siteUid) {
        this.siteUid = siteUid;
    }

    public Site getSite() {
        if (site == null) {
            if ((siteUid != null) && (getEnvironment() != null)) {
                site = siteService.getSiteByUid(environment, siteUid);
            }
        }
        return site;
    }

    public ResourceActions getSiteActions() {
        return siteActions;
    }

    // ValueDefinitions

    public String getValueDefinitionUid() {
        return valueDefinitionUid;
    }

    public void setValueDefinitionUid(String uid) {
        this.valueDefinitionUid = uid;
    }

    public ValueDefinition getValueDefinition() {
        if (valueDefinition == null) {
            if ((valueDefinitionUid != null) && (getEnvironment() != null)) {
                valueDefinition = definitionService.getValueDefinition(getEnvironment(), valueDefinitionUid);
            }
        }
        return valueDefinition;
    }

    public ResourceActions getValueDefinitionActions() {
        return valueDefinitionActions;
    }

    // Algorithms

    public String getAlgorithmUid() {
        return algorithmUid;
    }

    public void setAlgorithmUid(String algorithmId) {
        this.algorithmUid = algorithmId;
    }

    public Algorithm getAlgorithm() {
        if (algorithm == null) {
            if ((algorithmUid != null) && (getItemDefinition() != null)) {
                algorithm = definitionService.getAlgorithm(algorithmUid);
            }
        }
        return algorithm;
    }

    public ResourceActions getAlgorithmActions() {
        return algorithmActions;
    }

    // ItemDefinitions

    public String getItemDefinitionUid() {
        return itemDefinitionUid;
    }

    public void setItemDefinitionUid(String uid) {
        this.itemDefinitionUid = uid;
    }

    public ItemDefinition getItemDefinition() {
        if (itemDefinition == null) {
            if ((itemDefinitionUid != null) && (getEnvironment() != null)) {
                itemDefinition = definitionService.getItemDefinition(getEnvironment(), itemDefinitionUid);
            }
        }
        return itemDefinition;
    }

    public ResourceActions getItemDefinitionActions() {
        return itemDefinitionActions;
    }

    // ItemValueDefinitions

    public String getItemValueDefinitionUid() {
        return itemValueDefinitionUid;
    }

    public void setItemValueDefinitionUid(String itemValueDefinitionUid) {
        this.itemValueDefinitionUid = itemValueDefinitionUid;
    }

    public ItemValueDefinition getItemValueDefinition() {
        if (itemValueDefinition == null) {
            if ((itemValueDefinitionUid != null) && (getItemDefinition() != null)) {
                itemValueDefinition = definitionService.getItemValueDefinition(getItemDefinition(), itemValueDefinitionUid);
            }
        }
        return itemValueDefinition;
    }

    public ResourceActions getItemValueDefinitionActions() {
        return itemValueDefinitionActions;
    }
}
