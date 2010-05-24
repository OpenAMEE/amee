package com.amee.platform.service.v3.item;

import com.amee.base.resource.MissingAttributeException;
import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.environment.Environment;
import com.amee.platform.search.DataItemFilter;
import com.amee.platform.search.DataItemFilterValidationHelper;
import com.amee.platform.search.QueryFilter;
import com.amee.platform.search.SearchService;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import org.jdom.Document;
import org.jdom.Element;
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
public class DataItemsBuilder implements ResourceBuilder {

    private final static Map<String, Class> RENDERERS = new HashMap<String, Class>() {
        {
            put("application/json", DataItemsJSONRenderer.class);
            put("application/xml", DataItemsDOMRenderer.class);
        }
    };

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataItemBuilder dataItemBuilder;

    @Autowired
    private DataItemFilterValidationHelper validationHelper;

    private DataItemsRenderer renderer;

    @Transactional(readOnly = true)
    public Object handle(RequestWrapper requestWrapper) {
        renderer = getRenderer(requestWrapper);
        // Get Environment.
        Environment environment = environmentService.getEnvironmentByName("AMEE");
        // Get DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(environment, dataCategoryIdentifier);
            if ((dataCategory != null) && (dataCategory.getItemDefinition() != null)) {
                // Create filter and do search.
                DataItemFilter filter = new DataItemFilter(dataCategory.getItemDefinition());
                validationHelper.setDataItemFilter(filter);
                if (validationHelper.isValid(requestWrapper.getQueryParameters())) {
                    handle(requestWrapper, dataCategory, filter, renderer);
                    renderer.ok();
                } else {
                    throw new ValidationException(validationHelper.getValidationResult());
                }
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new MissingAttributeException("categoryIdentifier");
        }
        return renderer.getObject();
    }

    protected void handle(
            RequestWrapper requestWrapper,
            DataCategory dataCategory,
            QueryFilter filter,
            DataItemsRenderer renderer) {
        for (DataItem dataItem : searchService.getDataItems(dataCategory, filter)) {
            dataItemBuilder.handle(requestWrapper, dataItem, renderer.getDataItemRenderer());
            renderer.newDataItem();
        }
    }

    public DataItemsRenderer getRenderer(RequestWrapper requestWrapper) {
        try {
            for (String acceptedMediaType : requestWrapper.getAcceptedMediaTypes()) {
                if (RENDERERS.containsKey(acceptedMediaType)) {
                    return (DataItemsRenderer) RENDERERS.get(acceptedMediaType).newInstance();
                }
            }
        } catch (InstantiationException e) {
            // TODO
        } catch (IllegalAccessException e) {
            // TODO
        }
        throw new RuntimeException("TODO");
    }

    public String getMediaType() {
        throw new RuntimeException("Woo!");
    }

    public interface DataItemsRenderer {

        public void ok();

        public void start();

        public void newDataItem();

        public DataItemBuilder.DataItemRenderer getDataItemRenderer();

        public Object getObject();
    }

    public static class DataItemsJSONRenderer implements DataItemsBuilder.DataItemsRenderer {

        private DataItemBuilder.DataItemJSONRenderer dataItemRenderer;
        private JSONObject rootObj;
        private JSONArray itemsArr;

        public DataItemsJSONRenderer() {
            super();
            start();
        }

        public void start() {
            dataItemRenderer = new DataItemBuilder.DataItemJSONRenderer(false);
            rootObj = new JSONObject();
            itemsArr = new JSONArray();
            put(rootObj, "items", itemsArr);
        }

        public void ok() {
            put(rootObj, "status", "OK");
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

        public Object getObject() {
            return rootObj;
        }
    }

    public static class DataItemsDOMRenderer implements DataItemsBuilder.DataItemsRenderer {

        private DataItemBuilder.DataItemDOMRenderer dataItemRenderer;
        private Element rootElem;
        private Element ItemsElem;

        public DataItemsDOMRenderer() {
            super();
            dataItemRenderer = new DataItemBuilder.DataItemDOMRenderer(false);
            start();
        }

        public void start() {
            dataItemRenderer = new DataItemBuilder.DataItemDOMRenderer(false);
            rootElem = new Element("Representation");
            ItemsElem = new Element("Items");
            rootElem.addContent(ItemsElem);
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void newDataItem() {
            ItemsElem.addContent(dataItemRenderer.getDataItemElement());
        }

        public DataItemBuilder.DataItemRenderer getDataItemRenderer() {
            return dataItemRenderer;
        }

        public Object getObject() {
            return new Document(rootElem);
        }
    }
}