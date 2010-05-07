package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.path.PathItem;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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

    public class DataItemJSONRenderer implements DataItemBuilder.DataItemRenderer {

        private DataItem dataItem;
        private JSONObject rootObj;
        private JSONObject dataItemObj;

        public DataItemJSONRenderer() {
            super();
            rootObj = new JSONObject();
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public void notFound() {
            put(rootObj, "status", "NOT_FOUND");
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
        }

        public void addBasic() {
            put(dataItemObj, "uid", dataItem.getUid());
        }

        public void addName() {
            put(dataItemObj, "name", dataItem.getName());
            put(dataItemObj, "categoryWikiName", dataItem.getDataCategory().getWikiName());
        }

        public void addPath(PathItem pathItem) {
            put(dataItemObj, "path", dataItem.getPath());
            if (pathItem != null) {
                put(dataItemObj, "fullPath", pathItem.getFullPath() + "/" + dataItem.getDisplayPath());
            }
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