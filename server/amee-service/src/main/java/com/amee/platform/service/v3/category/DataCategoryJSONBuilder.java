package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class DataCategoryJSONBuilder implements ResourceBuilder<JSONObject> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    @Transactional(readOnly = true)
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            JSONObject representation = new JSONObject();
            String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
            if (categoryIdentifier != null) {
                DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                        environmentService.getEnvironmentByName("AMEE"), categoryIdentifier);
                if (dataCategory != null) {
                    representation.put("category", getDataCategoryJSONObject(requestWrapper, dataCategory));
                    representation.put("status", "OK");
                } else {
                    representation.put("status", "NOT_FOUND");
                }
            } else {
                representation.put("status", "ERROR");
                representation.put("error", "The categoryIdentifier was missing.");
            }
            return representation;
        } catch (Exception e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected JSONObject getDataCategoryJSONObject(RequestWrapper requestWrapper, DataCategory dataCategory) throws JSONException {

        JSONObject category = new JSONObject();
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean authority = requestWrapper.getMatrixParameters().containsKey("authority");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        category.put("uid", dataCategory.getUid());
        category.put("name", dataCategory.getName());
        category.put("wikiName", dataCategory.getWikiName());
        if (dataCategory.getDataCategory() != null) {
            category.put("parentWikiName", dataCategory.getDataCategory().getWikiName());
        }

        // Optional attributes.
        if (path || full) {
            // Get PathItem.
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataCategory.getEnvironment());
            PathItem pathItem = pathItemGroup.findByUId(dataCategory.getUid());
            // Add Paths.
            category.put("path", dataCategory.getPath());
            if (pathItem != null) {
                category.put("fullPath", pathItem.getFullPath());
            }
        }
        if (audit || full) {
            category.put("status", dataCategory.getStatus().getName());
            category.put("created", FMT.print(dataCategory.getCreated().getTime()));
            category.put("modified", FMT.print(dataCategory.getModified().getTime()));
        }
        if (authority || full) {
            category.put("authority", dataCategory.getAuthority());
        }
        if (wikiDoc || full) {
            category.put("wikiDoc", dataCategory.getWikiDoc());
        }
        if (provenance || full) {
            category.put("provenance", dataCategory.getProvenance());
        }
        if ((itemDefinition || full) && (dataCategory.getItemDefinition() != null)) {
            ItemDefinition id = dataCategory.getItemDefinition();
            category.put("itemDefinition", new JSONObject().put("uid", id.getUid()).put("name", id.getName()));
        }

        return category;
    }

    public String getMediaType() {
        return "application/json";
    }
}