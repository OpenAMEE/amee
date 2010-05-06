package com.amee.platform.service.v3.definition;

import com.amee.base.validation.ValidationHelper;
import com.amee.domain.data.ItemValueDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import java.util.HashSet;
import java.util.Set;

@Service
@Scope("prototype")
public class ItemValueDefinitionValidationHelper extends ValidationHelper {

    @Autowired
    private ItemValueDefinitionValidator itemValueDefinitionValidator;

    private ItemValueDefinition itemValueDefinition;
    private Set<String> allowedFields;

    @Override
    public Object getObject() {
        return itemValueDefinition;
    }

    @Override
    protected Validator getValidator() {
        return itemValueDefinitionValidator;
    }

    @Override
    public String getName() {
        return "itemValueDefinition";
    }

    @Override
    public String[] getAllowedFields() {
        if (allowedFields == null) {
            allowedFields = new HashSet<String>();
            allowedFields.add("name");
            allowedFields.add("path");
            allowedFields.add("wikiDoc");
        }
        return allowedFields.toArray(new String[]{});
    }

    public ItemValueDefinition getItemValueDefinition() {
        return itemValueDefinition;
    }

    public void setItemValueDefinition(ItemValueDefinition itemValueDefinition) {
        this.itemValueDefinition = itemValueDefinition;
    }
}