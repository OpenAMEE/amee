package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceAcceptor;
import com.amee.domain.data.DataCategory;
import com.amee.service.data.DataService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class DataCategoryFormAcceptor implements ResourceAcceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private DataCategoryValidationHelper validationHelper;

    @Transactional
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            JSONObject representation = new JSONObject();
            String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
            if (categoryIdentifier != null) {
                DataCategory dataCategory = dataService.getDataCategoryByUid(categoryIdentifier);
                if (dataCategory != null) {
                    validationHelper.setDataCategory(dataCategory);
                    if (validationHelper.isValid(requestWrapper.getFormParameters())) {
                        representation.put("status", "OK");
                    } else {
                        // TODO: Must cause a rollback here.
                        representation.put("validationResult", validationHelper.getValidationResult().getJSONObject());
                        representation.put("status", "INVALID");
                    }
                } else {
                    representation.put("status", "NOT_FOUND");
                }
            } else {
                representation.put("status", "ERROR");
                representation.put("error", "The categoryIdentifier was missing.");
            }
            return representation;
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }
}