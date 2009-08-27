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
package com.amee.restlet.environment;

import com.amee.domain.ValueDefinition;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.algorithm.AlgorithmContext;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.environment.Environment;
import com.amee.domain.site.Site;
import com.amee.service.BaseBrowser;
import com.amee.service.definition.DefinitionService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.environment.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class DefinitionBrowser extends BaseBrowser {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private DefinitionService definitionService;

    // Environments
    private String environmentUid = null;
    private Environment environment = null;

    // Environment Sites
    private String siteUid = null;
    private Site site = null;

    // ItemDefinitions
    private String valueDefinitionUid = null;
    private ValueDefinition valueDefinition = null;

    // Algorithms
    private String algorithmUid = null;
    private Algorithm algorithm = null;

    // Algorithm Contexts
    private String algorithmContextUid = null;
    private AlgorithmContext algorithmContext = null;

    // ItemDefinitions
    private String itemDefinitionUid = null;
    private ItemDefinition itemDefinition = null;

    // ItemValueDefinitions
    private String itemValueDefinitionUid = null;
    private ItemValueDefinition itemValueDefinition = null;

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

    // Algorithms
    public void setAlgorithmUid(String algorithmId) {
        this.algorithmUid = algorithmId;
    }

    public String getAlgorithmContextUid() {
        return algorithmContextUid;
    }

    public void setAlgorithmContextUid(String algorithmContextId) {
        this.algorithmContextUid = algorithmContextId;
    }

    public Algorithm getAlgorithm() {
        if (algorithm == null) {
            if ((algorithmUid != null) && (getItemDefinition() != null)) {
                algorithm = definitionService.getAlgorithmByUid(algorithmUid);
            }
        }
        return algorithm;
    }

    public AlgorithmContext getAlgorithmContext() {
        if (algorithmContext == null) {
            if (algorithmContextUid != null) {
                algorithmContext = definitionService.getAlgorithmContextByUid(getEnvironment(), algorithmContextUid);
            }
        }
        return algorithmContext;
    }

    public List<AlgorithmContext> getAlgorithmContexts() {
        return definitionService.getAlgorithmContexts(getEnvironment());
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
                itemValueDefinition = definitionService.getItemValueDefinitionByUid(itemValueDefinitionUid);
            }
        }
        return itemValueDefinition;
    }
}
