package com.amee.platform.service.v3.category;

import com.amee.base.resource.MissingAttributeException;
import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RendererHelper;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.tag.Tag;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import com.amee.service.tag.TagService;
import org.jdom.Document;
import org.jdom.Element;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Scope("prototype")
public class DataCategoryBuilder implements ResourceBuilder {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    private final static Map<String, Class> RENDERERS = new HashMap<String, Class>() {
        {
            put("application/json", DataCategoryJSONRenderer.class);
            put("application/xml", DataCategoryDOMRenderer.class);
        }
    };

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private TagService tagService;

    DataCategoryRenderer renderer;

    @Transactional(readOnly = true)
    public Object handle(RequestWrapper requestWrapper) {
        // Get Renderer.
        renderer = new RendererHelper<DataCategoryRenderer>().getRenderer(requestWrapper, RENDERERS);
        // Get Environment.
        Environment environment = environmentService.getEnvironmentByName("AMEE");
        // Get the DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(environment, dataCategoryIdentifier);
            if (dataCategory != null) {
                // Handle the DataCategory.
                this.handle(requestWrapper, dataCategory, renderer);
                renderer.ok();
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new MissingAttributeException("categoryIdentifier");
        }
        return renderer.getObject();
    }

    public void handle(
            RequestWrapper requestWrapper,
            DataCategory dataCategory,
            DataCategoryRenderer renderer) {

        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean parent = requestWrapper.getMatrixParameters().containsKey("parent");
        boolean authority = requestWrapper.getMatrixParameters().containsKey("authority");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");
        boolean tags = requestWrapper.getMatrixParameters().containsKey("tags");

        // New DataCategory & basic.
        renderer.newDataCategory(dataCategory);
        renderer.addBasic();

        // Optionals.
        if (path || full) {
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataCategory.getEnvironment());
            renderer.addPath(pathItemGroup.findByUId(dataCategory.getUid()));
        }
        if (parent || full) {
            renderer.addParent();
        }
        if (audit || full) {
            renderer.addAudit();
        }
        if (authority || full) {
            renderer.addAuthority();
        }
        if (wikiDoc || full) {
            renderer.addWikiDoc();
        }
        if (provenance || full) {
            renderer.addProvenance();
        }
        if ((itemDefinition || full) && (dataCategory.getItemDefinition() != null)) {
            ItemDefinition id = dataCategory.getItemDefinition();
            renderer.addItemDefinition(id);
        }
        if (tags || full) {
            renderer.startTags();
            for (Tag tag : tagService.getTags(dataCategory)) {
                renderer.newTag(tag);
            }
        }
    }

    public interface DataCategoryRenderer {

        public void start();

        public void ok();

        public void newDataCategory(DataCategory dataCategory);

        public void addBasic();

        public void addPath(PathItem pathItem);

        public void addParent();

        public void addAudit();

        public void addAuthority();

        public void addWikiDoc();

        public void addProvenance();

        public void addItemDefinition(ItemDefinition id);

        public void startTags();

        public void newTag(Tag tag);

        public Object getObject();
    }

    public static class DataCategoryJSONRenderer implements DataCategoryBuilder.DataCategoryRenderer {

        private DataCategory dataCategory;
        private JSONObject rootObj;
        private JSONObject dataCategoryObj;
        private JSONArray tagsArr;

        public DataCategoryJSONRenderer() {
            this(true);
        }

        public DataCategoryJSONRenderer(boolean start) {
            super();
            if (start) {
                start();
            }
        }

        public void start() {
            rootObj = new JSONObject();
        }

        public void ok() {
            put(rootObj, "status", "OK");
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
            put(dataCategoryObj, "type", dataCategory.getObjectType().getName());
            put(dataCategoryObj, "name", dataCategory.getName());
            put(dataCategoryObj, "wikiName", dataCategory.getWikiName());
        }

        public void addPath(PathItem pathItem) {
            put(dataCategoryObj, "path", dataCategory.getPath());
            if (pathItem != null) {
                put(dataCategoryObj, "fullPath", pathItem.getFullPath() + "/" + dataCategory.getDisplayPath());
            }
        }

        public void addParent() {
            if (dataCategory.getDataCategory() != null) {
                put(dataCategoryObj, "parentUid", dataCategory.getDataCategory().getUid());
                put(dataCategoryObj, "parentWikiName", dataCategory.getDataCategory().getWikiName());
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

        public void startTags() {
            tagsArr = new JSONArray();
            put(dataCategoryObj, "tags", tagsArr);
        }

        public void newTag(Tag tag) {
            JSONObject tagObj = new JSONObject();
            put(tagObj, "tag", tag.getTag());
            tagsArr.put(tagObj);
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getDataCategoryJSONObject() {
            return dataCategoryObj;
        }

        public JSONObject getObject() {
            return rootObj;
        }
    }

    public static class DataCategoryDOMRenderer implements DataCategoryBuilder.DataCategoryRenderer {

        private DataCategory dataCategory;
        private Element rootElem;
        private Element dataCategoryElem;
        private Element tagsElem;

        public DataCategoryDOMRenderer() {
            this(true);
        }

        public DataCategoryDOMRenderer(boolean start) {
            super();
            if (start) {
                start();
            }
        }

        public void start() {
            rootElem = new Element("Representation");
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void newDataCategory(DataCategory dataCategory) {
            this.dataCategory = dataCategory;
            dataCategoryElem = new Element("Category");
            if (rootElem != null) {
                rootElem.addContent(dataCategoryElem);
            }
        }

        public void addBasic() {
            dataCategoryElem.setAttribute("uid", dataCategory.getUid());
            dataCategoryElem.addContent(new Element("Name").setText(dataCategory.getName()));
            dataCategoryElem.addContent(new Element("WikiName").setText(dataCategory.getWikiName()));
        }

        public void addPath(PathItem pathItem) {
            dataCategoryElem.addContent(new Element("Path").setText(dataCategory.getPath()));
            if (pathItem != null) {
                dataCategoryElem.addContent(new Element("FullPath").setText(pathItem.getFullPath()));
            }
        }

        public void addParent() {
            if (dataCategory.getDataCategory() != null) {
                dataCategoryElem.addContent(new Element("ParentUid").setText(dataCategory.getDataCategory().getUid()));
                dataCategoryElem.addContent(new Element("ParentWikiName").setText(dataCategory.getDataCategory().getWikiName()));
            }
        }

        public void addAudit() {
            dataCategoryElem.setAttribute("status", dataCategory.getStatus().getName());
            dataCategoryElem.setAttribute("created", FMT.print(dataCategory.getCreated().getTime()));
            dataCategoryElem.setAttribute("modified", FMT.print(dataCategory.getModified().getTime()));
        }

        public void addAuthority() {
            dataCategoryElem.addContent(new Element("Authority").setText(dataCategory.getAuthority()));
        }

        public void addWikiDoc() {
            dataCategoryElem.addContent(new Element("WikiDoc").setText(dataCategory.getWikiDoc()));
        }

        public void addProvenance() {
            dataCategoryElem.addContent(new Element("Provenance").setText(dataCategory.getProvenance()));
        }

        public void addItemDefinition(ItemDefinition itemDefinition) {
            Element e = new Element("ItemDefinition");
            dataCategoryElem.addContent(e);
            e.setAttribute("uid", itemDefinition.getUid());
            e.addContent(new Element("Name").setText(itemDefinition.getName()));
        }

        public void startTags() {
            tagsElem = new Element("Tags");
            dataCategoryElem.addContent(tagsElem);
        }

        public void newTag(Tag tag) {
            Element tagElem = new Element("Tag");
            tagsElem.addContent(tagElem);
            tagElem.addContent(new Element("Tag").setText(tag.getTag()));
        }

        public Element getDataCategoryElement() {
            return dataCategoryElem;
        }

        public Document getObject() {
            return new Document(rootElem);
        }
    }
}