package com.amee.platform.service.v3.definition;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceAcceptor;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.service.definition.DefinitionService;
import com.amee.service.environment.EnvironmentService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class ItemValueDefinitionFormAcceptor implements ResourceAcceptor {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private ItemValueDefinitionValidationHelper validationHelper;

    @Transactional(rollbackFor = {com.amee.base.validation.ValidationException.class})
    public JSONObject handle(RequestWrapper requestWrapper) throws ValidationException {
        try {
            JSONObject o = new JSONObject();
            // Get ItemDefinition identifier.
            String itemDefinitionIdentifier = requestWrapper.getAttributes().get("itemDefinitionIdentifier");
            if (itemDefinitionIdentifier != null) {
                // Get ItemDefinition.
                ItemDefinition itemDefinition = definitionService.getItemDefinitionByUid(
                        environmentService.getEnvironmentByName("AMEE"), itemDefinitionIdentifier);
                if (itemDefinition != null) {
                    // Get ItemValueDefinition identifier.
                    String itemValueDefinitionIdentifier = requestWrapper.getAttributes().get("itemValueDefinitionIdentifier");
                    if (itemValueDefinitionIdentifier != null) {
                        // Get ItemValueDefinition.
                        ItemValueDefinition itemValueDefinition = definitionService.getItemValueDefinitionByUid(itemValueDefinitionIdentifier);
                        if (itemValueDefinition != null) {
                            validationHelper.setItemValueDefinition(itemValueDefinition);
                            if (validationHelper.isValid(requestWrapper.getFormParameters())) {
                                o.put("status", "OK");
                            } else {
                                throw new ValidationException(validationHelper.getValidationResult());
                            }
                        } else {
                            o.put("status", "NOT_FOUND");
                        }
                    } else {
                        o.put("status", "ERROR");
                        o.put("error", "The itemDefinitionIdentifier was missing.");
                    }
                } else {
                    o.put("status", "NOT_FOUND");
                }
            } else {
                o.put("status", "ERROR");
                o.put("error", "The itemDefinitionIdentifier was missing.");
            }
            return o;
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }
}