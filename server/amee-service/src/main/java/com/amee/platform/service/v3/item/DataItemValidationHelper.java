package com.amee.platform.service.v3.item;

import com.amee.base.validation.ValidationHelper;
import com.amee.domain.data.DataItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import java.util.HashSet;
import java.util.Set;

@Service
@Scope("prototype")
public class DataItemValidationHelper extends ValidationHelper {

    @Autowired
    private DataItemValidator dataItemValidator;

    private DataItem dataItem;
    private Set<String> allowedFields;

    @Override
    public Object getObject() {
        return dataItem;
    }

    @Override
    protected Validator getValidator() {
        return dataItemValidator;
    }

    @Override
    public String getName() {
        return "dataItem";
    }

    @Override
    public String[] getAllowedFields() {
        if (allowedFields == null) {
            allowedFields = new HashSet<String>();
            allowedFields.add("name");
            allowedFields.add("path");
            allowedFields.add("wikiDoc");
            allowedFields.add("provenance");
        }
        return allowedFields.toArray(new String[]{});
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }
}