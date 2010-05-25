package com.amee.platform.service.v3.item;

import com.amee.base.resource.MissingAttributeException;
import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RendererHelper;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValue;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.auth.AuthenticationService;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
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
public class DataItemBuilder implements ResourceBuilder {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    private final static Map<String, Class> RENDERERS = new HashMap<String, Class>() {
        {
            put("application/json", DataItemJSONRenderer.class);
            put("application/xml", DataItemDOMRenderer.class);
        }
    };

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    private DataItemRenderer renderer;

    @Transactional(readOnly = true)
    public Object handle(RequestWrapper requestWrapper) {
        // Get Renderer.
        renderer = new RendererHelper<DataItemRenderer>().getRenderer(requestWrapper, RENDERERS);
        // Get Environment.
        Environment environment = environmentService.getEnvironmentByName("AMEE");
//        // Authenticate - Create sample User.
//        User sampleUser = new User();
//        sampleUser.setEnvironment(environment);
//        sampleUser.setUsername(requestWrapper.getAttributes().get("username"));
//        sampleUser.setPasswordInClear(requestWrapper.getAttributes().get("password"));
//        // Authenticate - Check sample User.
//        User authUser = authenticationService.authenticate(sampleUser);
//        if (authUser != null) {
        // Get DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(environment, dataCategoryIdentifier);
            if (dataCategory != null) {
                // Get DataItem identifier.
                String dataItemIdentifier = requestWrapper.getAttributes().get("itemIdentifier");
                if (dataItemIdentifier != null) {
                    // Get DataItem.
                    DataItem dataItem = dataService.getDataItemByUid(dataCategory, dataItemIdentifier);
                    if (dataItem != null) {
                        // Handle the DataItem.
                        this.handle(requestWrapper, dataItem, renderer);
                        renderer.ok();
                    } else {
                        throw new NotFoundException();
                    }
                } else {
                    throw new MissingAttributeException("itemIdentifier");
                }
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new MissingAttributeException("categoryIdentifier");
        }
//        } else {
//            renderer.notAuthenticated();
//        }
        return renderer.getObject();
    }

    public void handle(
            RequestWrapper requestWrapper,
            DataItem dataItem,
            DataItemRenderer renderer) {

        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean name = requestWrapper.getMatrixParameters().containsKey("name");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean parent = requestWrapper.getMatrixParameters().containsKey("parent");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");
        boolean values = requestWrapper.getMatrixParameters().containsKey("values");

        // New DataItem & basic.
        renderer.newDataItem(dataItem);
        renderer.addBasic();

        // Optionals.
        if (name || full) {
            renderer.addName();
        }
        if (path || full) {
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataItem.getEnvironment());
            renderer.addPath(pathItemGroup.findByUId(dataItem.getDataCategory().getUid()));
        }
        if (parent || full) {
            renderer.addParent();
        }
        if (audit || full) {
            renderer.addAudit();
        }
        if (wikiDoc || full) {
            renderer.addWikiDoc();
        }
        if (provenance || full) {
            renderer.addProvenance();
        }
        if ((itemDefinition || full) && (dataItem.getItemDefinition() != null)) {
            ItemDefinition id = dataItem.getItemDefinition();
            renderer.addItemDefinition(id);
        }
        if (values || full) {
            renderer.addValues();
        }
    }

    public interface DataItemRenderer {

        public void start();

        public void ok();

        public void newDataItem(DataItem dataItem);

        public void addBasic();

        public void addName();

        public void addPath(PathItem pathItem);

        public void addParent();

        public void addAudit();

        public void addWikiDoc();

        public void addProvenance();

        public void addItemDefinition(ItemDefinition id);

        public void addValues();

        public Object getObject();
    }

    public static class DataItemJSONRenderer implements DataItemBuilder.DataItemRenderer {

        private DataItem dataItem;
        private JSONObject rootObj;
        private JSONObject dataItemObj;

        public DataItemJSONRenderer() {
            this(true);
        }

        public DataItemJSONRenderer(boolean start) {
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

        public Object getObject() {
            return rootObj;
        }
    }

    public static class DataItemDOMRenderer implements DataItemRenderer {

        private DataItem dataItem;
        private Element rootElem;
        private Element dataItemElem;

        public DataItemDOMRenderer() {
            this(true);
        }

        public DataItemDOMRenderer(boolean start) {
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

        public void newDataItem(DataItem dataItem) {
            this.dataItem = dataItem;
            dataItemElem = new Element("Item");
            if (rootElem != null) {
                rootElem.addContent(dataItemElem);
            }
        }

        public void addBasic() {
            dataItemElem.setAttribute("uid", dataItem.getUid());
        }

        public void addName() {
            dataItemElem.addContent(new Element("Name").setText(dataItem.getName()));
        }

        public void addPath(PathItem pathItem) {
            dataItemElem.addContent(new Element("Path").setText(dataItem.getPath()));
            if (pathItem != null) {
                dataItemElem.addContent(new Element("FullPath").setText(pathItem.getFullPath() + "/" + dataItem.getDisplayPath()));
            }
        }

        public void addParent() {
            dataItemElem.addContent(new Element("CategoryUid").setText(dataItem.getDataCategory().getUid()));
            dataItemElem.addContent(new Element("CategoryWikiName").setText(dataItem.getDataCategory().getWikiName()));
        }

        public void addAudit() {
            dataItemElem.setAttribute("status", dataItem.getStatus().getName());
            dataItemElem.setAttribute("created", FMT.print(dataItem.getCreated().getTime()));
            dataItemElem.setAttribute("modified", FMT.print(dataItem.getModified().getTime()));
        }

        public void addWikiDoc() {
            dataItemElem.addContent(new Element("WikiDoc").setText(dataItem.getWikiDoc()));
        }

        public void addProvenance() {
            dataItemElem.addContent(new Element("Provenance").setText(dataItem.getProvenance()));
        }

        public void addItemDefinition(ItemDefinition itemDefinition) {
            Element e = new Element("ItemDefinition");
            dataItemElem.addContent(e);
            e.setAttribute("uid", itemDefinition.getUid());
            e.addContent(new Element("Name").setText(itemDefinition.getName()));
        }

        public void addValues() {
            Element valuesElem = new Element("Values");
            dataItemElem.addContent(valuesElem);
            for (ItemValue itemValue : dataItem.getItemValues()) {
                Element valueElem = new Element("Value");
                valueElem.addContent(new Element("Path").setText(itemValue.getPath()));
                valueElem.addContent(new Element("Value").setText(itemValue.getValue()));
                if (itemValue.hasUnit()) {
                    valueElem.addContent(new Element("Unit").setText(itemValue.getUnit().toString()));
                }
                if (itemValue.hasPerUnit()) {
                    valueElem.addContent(new Element("PerUnit").setText(itemValue.getPerUnit().toString()));
                    valueElem.addContent(new Element("CompoundUnit").setText(itemValue.getCompoundUnit().toString()));
                }
                valuesElem.addContent(valueElem);
            }
        }

        public Element getDataItemElement() {
            return dataItemElem;
        }

        public Object getObject() {
            return new Document(rootElem);
        }
    }
}