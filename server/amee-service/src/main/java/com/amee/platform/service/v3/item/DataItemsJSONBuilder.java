package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.platform.search.DataItemFilterValidationHelper;
import com.amee.platform.search.SearchService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DataItemsJSONBuilder extends DataItemsBuilder<JSONObject> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataItemJSONBuilder dataItemJSONBuilder;

    @Autowired
    private DataItemFilterValidationHelper validationHelper;

    public JSONObject handle(RequestWrapper requestWrapper) {
        DataItemsJSONRenderer renderer =
                new DataItemsJSONRenderer(new DataItemJSONBuilder.DataItemJSONRenderer());
        super.handle(requestWrapper, renderer);
        return renderer.getJSONObject();
    }

    public String getMediaType() {
        return "application/json";
    }

    public DataItemBuilder getDataItemBuilder() {
        return dataItemJSONBuilder;
    }

    public class DataItemsJSONRenderer implements DataItemsBuilder.DataItemsRenderer {

        private DataItemJSONBuilder.DataItemJSONRenderer dataItemRenderer;
        private JSONObject rootObj;
        private JSONArray itemsArr;

        public DataItemsJSONRenderer(DataItemJSONBuilder.DataItemJSONRenderer dataItemRenderer) {
            super();
            this.dataItemRenderer = dataItemRenderer;
        }

        public void start() {
            rootObj = new JSONObject();
            itemsArr = new JSONArray();
            put(rootObj, "items", itemsArr);
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public void notFound() {
            put(rootObj, "status", "NOT_FOUND");
        }

        public void notAuthenticated() {
            put(rootObj, "status", "NOT_AUTHENTICATED");
        }

        public void categoryIdentifierMissing() {
            put(rootObj, "status", "ERROR");
            put(rootObj, "error", "The categoryIdentifier was missing.");
        }

        public void newDataItem() {
            itemsArr.put(dataItemRenderer.getDataItemJSONObject());
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