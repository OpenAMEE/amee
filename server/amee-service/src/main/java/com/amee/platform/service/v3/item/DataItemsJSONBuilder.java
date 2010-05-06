package com.amee.platform.service.v3.item;

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
public class DataItemsJSONBuilder implements ResourceBuilder<JSONObject> {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataItemJSONBuilder dataItemJSONBuilder;

    @Transactional(readOnly = true)
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            JSONObject o = new JSONObject();
            String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
            if (categoryIdentifier != null) {
                DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                        environmentService.getEnvironmentByName("AMEE"), categoryIdentifier);
                if (dataCategory != null) {
                    o.put("categories", getDataItemsJSONArray(requestWrapper, dataCategory));
                    o.put("status", "OK");
                } else {
                    o.put("status", "NOT_FOUND");
                }
            } else {
                o.put("status", "ERROR");
                o.put("error", "The categoryIdentifier was missing.");
            }
            return o;
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected JSONArray getDataItemsJSONArray(RequestWrapper requestWrapper, DataCategory dataCategory) throws JSONException {
        JSONArray a = new JSONArray();
//        for (DataCategory dataCategory : dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
//            categories.put(dataItemJSONBuilder.getDataCategoryJSONObject(requestWrapper, dataCategory));
//        }
        return a;
    }

    public String getMediaType() {
        return "application/json";
    }
}