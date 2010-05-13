package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValue;
import com.amee.domain.path.PathItem;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DataItemJSONBuilder extends DataItemBuilder<JSONObject> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    public JSONObject handle(RequestWrapper requestWrapper) {
        DataItemJSONRenderer renderer = new DataItemJSONRenderer();
        super.handle(requestWrapper, renderer);
        return renderer.getJSONObject();
    }

    public String getMediaType() {
        return "application/json";
    }

    public static class DataItemJSONRenderer implements DataItemBuilder.DataItemRenderer {

        private DataItem dataItem;
        private JSONObject rootObj;
        private JSONObject dataItemObj;

        public DataItemJSONRenderer() {
            super();
        }

        public void start() {
            rootObj = new JSONObject();
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

        public void itemIdentifierMissing() {
            put(rootObj, "status", "ERROR");
            put(rootObj, "error", "The itemIdentifier was missing.");
        }

        public void categoryIdentifierMissing() {
            put(rootObj, "status", "ERROR");
            put(rootObj, "error", "The categoryIdentifier was missing.");
        }

        public void newDataItem(DataItem dataItem) {
            this.dataItem = dataItem;
            dataItemObj = new JSONObject();
            if (rootObj != null) {
                put(rootObj, "item", dataItemObj);
            }
        }

        public void addBasic() {
            put(dataItemObj, "uid", dataItem.getUid());
            put(dataItemObj, "type", dataItem.getObjectType().getName());
        }

        public void addName() {
            put(dataItemObj, "name", dataItem.getName());
        }

        public void addPath(PathItem pathItem) {
            put(dataItemObj, "path", dataItem.getPath());
            if (pathItem != null) {
                put(dataItemObj, "fullPath", pathItem.getFullPath() + "/" + dataItem.getDisplayPath());
            }
        }

        public void addParent() {
            put(dataItemObj, "categoryUid", dataItem.getDataCategory().getUid());
            put(dataItemObj, "categoryWikiName", dataItem.getDataCategory().getWikiName());
        }

        public void addAudit() {
            put(dataItemObj, "status", dataItem.getStatus().getName());
            put(dataItemObj, "created", FMT.print(dataItem.getCreated().getTime()));
            put(dataItemObj, "modified", FMT.print(dataItem.getModified().getTime()));
        }

        public void addWikiDoc() {
            put(dataItemObj, "wikiDoc", dataItem.getWikiDoc());
        }

        public void addProvenance() {
            put(dataItemObj, "provenance", dataItem.getProvenance());
        }

        public void addItemDefinition(ItemDefinition itemDefinition) {
            JSONObject itemDefinitionObj = new JSONObject();
            put(itemDefinitionObj, "uid", itemDefinition.getUid());
            put(itemDefinitionObj, "name", itemDefinition.getName());
            put(dataItemObj, "itemDefinition", itemDefinitionObj);
        }

        public void addValues() {
            JSONArray valuesArr = new JSONArray();
            put(dataItemObj, "values", valuesArr);
            for (ItemValue itemValue : dataItem.getItemValues()) {
                JSONObject valueObj = new JSONObject();
                put(valueObj, "path", itemValue.getPath());
                put(valueObj, "value", itemValue.getValue());
                if (itemValue.hasUnit()) {
                    put(valueObj, "unit", itemValue.getUnit().toString());
                }
                if (itemValue.hasPerUnit()) {
                    put(valueObj, "perUnit", itemValue.getPerUnit().toString());
                    put(valueObj, "compoundUnit", itemValue.getCompoundUnit().toString());
                }
                valuesArr.put(valueObj);
            }
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getDataItemJSONObject() {
            return dataItemObj;
        }

        public JSONObject getJSONObject() {
            return rootObj;
        }
    }
}