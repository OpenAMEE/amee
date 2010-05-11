package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.domain.data.DataCategory;
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
public class DataCategoryJSONBuilder extends DataCategoryBuilder<JSONObject> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    public JSONObject handle(RequestWrapper requestWrapper) {
        DataCategoryJSONRenderer renderer = new DataCategoryJSONRenderer();
        super.handle(requestWrapper, renderer);
        return renderer.getObject();
    }

    public String getMediaType() {
        return "application/json";
    }

    public static class DataCategoryJSONRenderer implements DataCategoryBuilder.DataCategoryRenderer {

        private DataCategory dataCategory;
        private JSONObject rootObj;
        private JSONObject dataCategoryObj;

        public DataCategoryJSONRenderer() {
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

        public void categoryIdentifierMissing() {
            put(rootObj, "status", "ERROR");
            put(rootObj, "error", "The categoryIdentifier was missing.");
        }

        public void newDataCategory(DataCategory dataCategory) {
            this.dataCategory = dataCategory;
            dataCategoryObj = new JSONObject();
            if (rootObj != null) {
                put(rootObj, "category", dataCategoryObj);
            }
        }

        public void addBasic() {
            put(dataCategoryObj, "uid", dataCategory.getUid());
            put(dataCategoryObj, "name", dataCategory.getName());
            put(dataCategoryObj, "wikiName", dataCategory.getWikiName());
            if (dataCategory.getDataCategory() != null) {
                put(dataCategoryObj, "parentWikiName", dataCategory.getDataCategory().getWikiName());
            }
        }

        public void addPath(PathItem pathItem) {
            put(dataCategoryObj, "path", dataCategory.getPath());
            if (pathItem != null) {
                put(dataCategoryObj, "fullPath", pathItem.getFullPath() + "/" + dataCategory.getDisplayPath());
            }
        }

        public void addAudit() {
            put(dataCategoryObj, "status", dataCategory.getStatus().getName());
            put(dataCategoryObj, "created", FMT.print(dataCategory.getCreated().getTime()));
            put(dataCategoryObj, "modified", FMT.print(dataCategory.getModified().getTime()));
        }

        public void addAuthority() {
            put(dataCategoryObj, "authority", dataCategory.getAuthority());
        }

        public void addWikiDoc() {
            put(dataCategoryObj, "wikiDoc", dataCategory.getWikiDoc());
        }

        public void addProvenance() {
            put(dataCategoryObj, "provenance", dataCategory.getProvenance());
        }

        public void addItemDefinition(ItemDefinition itemDefinition) {
            JSONObject itemDefinitionObj = new JSONObject();
            put(itemDefinitionObj, "uid", itemDefinition.getUid());
            put(itemDefinitionObj, "name", itemDefinition.getName());
            put(dataCategoryObj, "itemDefinition", itemDefinitionObj);
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getDataCategoryObject() {
            return dataCategoryObj;
        }

        public JSONObject getObject() {
            return rootObj;
        }
    }
}