package com.amee.platform.service.v3.definition;

import com.amee.base.resource.MissingAttributeException;
import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RendererHelper;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.environment.Environment;
import com.amee.service.definition.DefinitionService;
import com.amee.service.environment.EnvironmentService;
import org.jdom.Document;
import org.jdom.Element;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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
public class ItemValueDefinitionBuilder implements ResourceBuilder {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    private final static Map<String, Class> RENDERERS = new HashMap<String, Class>() {
        {
            put("application/json", ItemValueDefinitionJSONRenderer.class);
            put("application/xml", ItemValueDefinitionDOMRenderer.class);
        }
    };

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DefinitionService definitionService;

    private ItemValueDefinitionRenderer renderer;

    @Transactional(readOnly = true)
    public Object handle(RequestWrapper requestWrapper) {
        // Get Renderer.
        renderer = new RendererHelper<ItemValueDefinitionRenderer>().getRenderer(requestWrapper, RENDERERS);
        // Get Environment.
        Environment environment = environmentService.getEnvironmentByName("AMEE");
        Element e = new Element("Representation");
        // Get ItemDefinition identifier.
        String itemDefinitionIdentifier = requestWrapper.getAttributes().get("itemDefinitionIdentifier");
        if (itemDefinitionIdentifier != null) {
            // Get ItemDefinition.
            ItemDefinition itemDefinition = definitionService.getItemDefinitionByUid(
                    environment, itemDefinitionIdentifier);
            if (itemDefinition != null) {
                // Get ItemValueDefinition identifier.
                String itemValueDefinitionIdentifier = requestWrapper.getAttributes().get("itemValueDefinitionIdentifier");
                if (itemValueDefinitionIdentifier != null) {
                    // Get ItemValueDefinition.
                    ItemValueDefinition itemValueDefinition = definitionService.getItemValueDefinitionByUid(
                            itemDefinition, itemValueDefinitionIdentifier);
                    if (itemValueDefinition != null) {
                        // Handle the ItemValueDefinition.
                        handle(requestWrapper, itemValueDefinition, renderer);
                        renderer.ok();
                    } else {
                        throw new NotFoundException();
                    }
                } else {
                    throw new MissingAttributeException("itemValueDefinitionIdentifier");
                }
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new MissingAttributeException("itemDefinitionIdentifier");
        }
        return renderer.getObject();
    }

    protected void handle(
            RequestWrapper requestWrapper,
            ItemValueDefinition itemValueDefinition,
            ItemValueDefinitionRenderer renderer) {

        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean name = requestWrapper.getMatrixParameters().containsKey("name");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // New ItemValueDefinition & basic.
        renderer.newItemValueDefinition(itemValueDefinition);
        renderer.addBasic();

        // Optional attributes.
        if (name || full) {
            renderer.addName();
        }
        if (path || full) {
            renderer.addPath();
        }
        if (audit || full) {
            renderer.addAudit();
        }
        if (wikiDoc || full) {
            renderer.addWikiDoc();
        }
        if ((itemDefinition || full) && (itemValueDefinition.getItemDefinition() != null)) {
            ItemDefinition id = itemValueDefinition.getItemDefinition();
            renderer.addItemDefinition(id);
        }
    }

    public interface ItemValueDefinitionRenderer {

        public void start();

        public void ok();

        public void newItemValueDefinition(ItemValueDefinition itemValueDefinition);

        public void addBasic();

        public void addName();

        public void addPath();

        public void addAudit();

        public void addWikiDoc();

        public void addItemDefinition(ItemDefinition id);

        public Object getObject();
    }

    public static class ItemValueDefinitionJSONRenderer implements ItemValueDefinitionRenderer {

        private ItemValueDefinition itemValueDefinition;
        private JSONObject rootObj;
        private JSONObject itemValueDefinitionObj;

        public ItemValueDefinitionJSONRenderer() {
            this(true);
        }

        public ItemValueDefinitionJSONRenderer(boolean start) {
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

        public void newItemValueDefinition(ItemValueDefinition itemValueDefinition) {
            this.itemValueDefinition = itemValueDefinition;
            itemValueDefinitionObj = new JSONObject();
            if (rootObj != null) {
                put(rootObj, "itemValueDefinition", itemValueDefinitionObj);
            }
        }

        public void addBasic() {
            put(itemValueDefinitionObj, "uid", itemValueDefinition.getUid());
            put(itemValueDefinitionObj, "type", itemValueDefinition.getObjectType().getName());
        }

        public void addName() {
            put(itemValueDefinitionObj, "name", itemValueDefinition.getName());
        }

        public void addPath() {
            put(itemValueDefinitionObj, "path", itemValueDefinition.getPath());
        }

        public void addAudit() {
            put(itemValueDefinitionObj, "status", itemValueDefinition.getStatus().getName());
            put(itemValueDefinitionObj, "created", FMT.print(itemValueDefinition.getCreated().getTime()));
            put(itemValueDefinitionObj, "modified", FMT.print(itemValueDefinition.getModified().getTime()));
        }

        public void addWikiDoc() {
            put(itemValueDefinitionObj, "wikiDoc", itemValueDefinition.getWikiDoc());
        }

        public void addItemDefinition(ItemDefinition itemDefinition) {
            JSONObject itemDefinitionObj = new JSONObject();
            put(itemDefinitionObj, "uid", itemDefinition.getUid());
            put(itemDefinitionObj, "name", itemDefinition.getName());
            put(itemValueDefinitionObj, "itemDefinition", itemDefinitionObj);
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getItemValueDefinitionJSONObject() {
            return itemValueDefinitionObj;
        }

        public Object getObject() {
            return rootObj;
        }
    }

    public static class ItemValueDefinitionDOMRenderer implements ItemValueDefinitionRenderer {

        private ItemValueDefinition itemValueDefinition;
        private Element rootElem;
        private Element itemValueDefinitionElem;

        public ItemValueDefinitionDOMRenderer() {
            this(true);
        }

        public ItemValueDefinitionDOMRenderer(boolean start) {
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

        public void newItemValueDefinition(ItemValueDefinition itemValueDefinition) {
            this.itemValueDefinition = itemValueDefinition;
            itemValueDefinitionElem = new Element("ItemValueDefinition");
            if (rootElem != null) {
                rootElem.addContent(itemValueDefinitionElem);
            }
        }

        public void addBasic() {
            itemValueDefinitionElem.setAttribute("uid", itemValueDefinition.getUid());
        }

        public void addName() {
            itemValueDefinitionElem.addContent(new Element("Name").setText(itemValueDefinition.getName()));
        }

        public void addPath() {
            itemValueDefinitionElem.addContent(new Element("Path").setText(itemValueDefinition.getPath()));
        }

        public void addAudit() {
            itemValueDefinitionElem.setAttribute("status", itemValueDefinition.getStatus().getName());
            itemValueDefinitionElem.setAttribute("created", FMT.print(itemValueDefinition.getCreated().getTime()));
            itemValueDefinitionElem.setAttribute("modified", FMT.print(itemValueDefinition.getModified().getTime()));
        }

        public void addWikiDoc() {
            itemValueDefinitionElem.addContent(new Element("WikiDoc").setText(itemValueDefinition.getWikiDoc()));
        }

        public void addItemDefinition(ItemDefinition itemDefinition) {
            Element e = new Element("ItemDefinition");
            itemValueDefinitionElem.addContent(e);
            e.setAttribute("uid", itemDefinition.getUid());
            e.addContent(new Element("Name").setText(itemDefinition.getName()));
        }

        public Element getItemValueDefinitionElement() {
            return itemValueDefinitionElem;
        }

        public Object getObject() {
            return new Document(rootElem);
        }
    }
}