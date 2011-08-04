package com.amee.restlet.environment;

import com.amee.domain.ValueDefinition;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.algorithm.AlgorithmContext;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.service.BaseBrowser;
import com.amee.service.definition.DefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class DefinitionBrowser extends BaseBrowser {

    @Autowired
    private DefinitionService definitionService;

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

    // ValueDefinitions

    public String getValueDefinitionUid() {
        return valueDefinitionUid;
    }

    public void setValueDefinitionUid(String uid) {
        this.valueDefinitionUid = uid;
    }

    public ValueDefinition getValueDefinition() {
        if (valueDefinition == null) {
            if ((valueDefinitionUid != null)) {
                valueDefinition = definitionService.getValueDefinition(valueDefinitionUid);
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
                algorithmContext = definitionService.getAlgorithmContextByUid(algorithmContextUid);
            }
        }
        return algorithmContext;
    }

    public List<AlgorithmContext> getAlgorithmContexts() {
        return definitionService.getAlgorithmContexts();
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
            if ((itemDefinitionUid != null)) {
                itemDefinition = definitionService.getItemDefinitionByUid(itemDefinitionUid);
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
