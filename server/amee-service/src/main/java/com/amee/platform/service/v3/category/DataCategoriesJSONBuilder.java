package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.platform.search.DataCategoryFilterValidationHelper;
import com.amee.platform.search.SearchService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DataCategoriesJSONBuilder extends DataCategoriesBuilder<JSONObject> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataCategoryDOMBuilder dataCategoryDOMBuilder;

    @Autowired
    private DataCategoryFilterValidationHelper validationHelper;

    public JSONObject handle(RequestWrapper requestWrapper) {
        DataCategoriesJSONRenderer renderer =
                new DataCategoriesJSONRenderer(new DataCategoryJSONBuilder.DataCategoryJSONRenderer());
        super.handle(requestWrapper, renderer);
        return renderer.getJSONObject();
    }

    public String getMediaType() {
        return "application/json";
    }

    public class DataCategoriesJSONRenderer implements DataCategoriesBuilder.DataCategoriesRenderer {

        private DataCategoryJSONBuilder.DataCategoryJSONRenderer dataCategoryRenderer;
        private JSONObject rootObj;
        private JSONArray categoriesArr;

        public DataCategoriesJSONRenderer(DataCategoryJSONBuilder.DataCategoryJSONRenderer dataCategoryRenderer) {
            super();
            this.dataCategoryRenderer = dataCategoryRenderer;
        }

        public void start() {
            rootObj = new JSONObject();
            categoriesArr = new JSONArray();
            put(rootObj, "categories", categoriesArr);
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public void notAuthenticated() {
            put(rootObj, "status", "NOT_AUTHENTICATED");
        }

        public void newDataCategory() {
            categoriesArr.put(dataCategoryRenderer.getDataCategoryObject());
        }

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer() {
            return dataCategoryRenderer;
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getJSONObject() {
            return rootObj;
        }
    }
}