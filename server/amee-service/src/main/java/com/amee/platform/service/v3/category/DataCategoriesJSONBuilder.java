package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
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
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataCategoryJSONBuilder dataCategoryJSONBuilder;

    @Transactional(readOnly = true)
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            JSONObject representation = new JSONObject();
            representation.put("categories", getDataCategoriesJSONArray(requestWrapper));
            representation.put("status", "OK");
            representation.put("version", requestWrapper.getVersion().toString());
            return representation;
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected JSONArray getDataCategoriesJSONArray(RequestWrapper requestWrapper) throws JSONException {
        JSONArray categories = new JSONArray();
        for (DataCategory dataCategory : dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
            categories.put(dataCategoryJSONBuilder.getDataCategoryJSONObject(requestWrapper, dataCategory));
        }
        return categories;
    }

    public String getMediaType() {
        return "application/json";
    }
}