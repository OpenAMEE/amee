package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.platform.search.DataCategoryFilter;
import com.amee.platform.search.DataCategoryFilterValidationHelper;
import com.amee.platform.search.SearchService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class DataCategoriesJSONBuilder implements ResourceBuilder<JSONObject> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataCategoryJSONBuilder dataCategoryJSONBuilder;

    @Autowired
    private DataCategoryFilterValidationHelper validationHelper;

    @Transactional(readOnly = true)
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            JSONObject o = new JSONObject();
            DataCategoryFilter filter = new DataCategoryFilter();
            validationHelper.setDataCategoryFilter(filter);
            if (validationHelper.isValid(requestWrapper.getQueryParameters())) {
                o.put("categories", getDataCategoriesJSONArray(requestWrapper, filter));
                o.put("status", "OK");
            } else {
                throw new ValidationException(validationHelper.getValidationResult());
            }
            return o;
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected JSONArray getDataCategoriesJSONArray(RequestWrapper requestWrapper, DataCategoryFilter filter) throws JSONException {
        JSONArray categories = new JSONArray();
        for (DataCategory dataCategory : searchService.getDataCategories(filter)) {
            categories.put(dataCategoryJSONBuilder.getDataCategoryJSONObject(requestWrapper, dataCategory));
        }
        return categories;
    }

    public String getMediaType() {
        return "application/json";
    }
}