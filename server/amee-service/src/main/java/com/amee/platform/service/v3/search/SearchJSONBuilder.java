package com.amee.platform.service.v3.search;

import com.amee.base.resource.RequestWrapper;
import com.amee.platform.service.v3.category.DataCategoryBuilder;
import com.amee.platform.service.v3.category.DataCategoryJSONBuilder;
import com.amee.platform.service.v3.item.DataItemBuilder;
import com.amee.platform.service.v3.item.DataItemJSONBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SearchJSONBuilder extends SearchBuilder<JSONObject> {

    @Autowired
    private DataCategoryJSONBuilder dataCategoryJSONBuilder;

    @Autowired
    private DataItemJSONBuilder dataItemJSONBuilder;

    public JSONObject handle(RequestWrapper requestWrapper) {
        SearchJSONRenderer renderer =
                new SearchJSONRenderer(
                        new DataCategoryJSONBuilder.DataCategoryJSONRenderer(),
                        new DataItemJSONBuilder.DataItemJSONRenderer());
        super.handle(requestWrapper, renderer);
        return renderer.getJSONObject();
    }

    public String getMediaType() {
        return "application/json";
    }

    public DataCategoryBuilder getDataCategoryBuilder() {
        return dataCategoryJSONBuilder;
    }

    public DataItemBuilder getDataItemBuilder() {
        return dataItemJSONBuilder;
    }

    public class SearchJSONRenderer implements SearchRenderer {

        private DataCategoryJSONBuilder.DataCategoryJSONRenderer dataCategoryRenderer;
        private DataItemJSONBuilder.DataItemJSONRenderer dataItemRenderer;
        private JSONObject rootObj;
        private JSONArray resultsArr;

        public SearchJSONRenderer(
                DataCategoryJSONBuilder.DataCategoryJSONRenderer dataCategoryRenderer,
                DataItemJSONBuilder.DataItemJSONRenderer dataItemRenderer) {
            super();
            this.dataCategoryRenderer = dataCategoryRenderer;
            this.dataItemRenderer = dataItemRenderer;
        }

        public void start() {
            rootObj = new JSONObject();
            resultsArr = new JSONArray();
            put(rootObj, "results", resultsArr);
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public void notAuthenticated() {
            put(rootObj, "status", "NOT_AUTHENTICATED");
        }

        public void newDataCategory() {
            resultsArr.put(dataCategoryRenderer.getDataCategoryJSONObject());
        }

        public void newDataItem() {
            resultsArr.put(dataItemRenderer.getDataItemJSONObject());
        }

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer() {
            return dataCategoryRenderer;
        }

        public DataItemBuilder.DataItemRenderer getDataItemRenderer() {
            return dataItemRenderer;
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