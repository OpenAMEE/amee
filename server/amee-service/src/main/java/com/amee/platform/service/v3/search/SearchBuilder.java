package com.amee.platform.service.v3.search;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.AMEEEntity;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.platform.search.SearchService;
import com.amee.platform.service.v3.category.DataCategoryBuilder;
import com.amee.platform.service.v3.item.DataItemBuilder;
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
public class SearchBuilder implements ResourceBuilder {

    private final static Map<String, Class> RENDERERS = new HashMap<String, Class>() {
        {
            put("application/json", SearchJSONRenderer.class);
            put("application/xml", SearchDOMRenderer.class);
        }
    };

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchFilterValidationHelper validationHelper;

    @Autowired
    private DataCategoryBuilder dataCategoryBuilder;

    @Autowired
    private DataItemBuilder dataItemBuilder;

    private SearchRenderer renderer;

    @Transactional(readOnly = true)
    public Object handle(RequestWrapper requestWrapper) {
        renderer = getRenderer(requestWrapper);
        renderer.start();
        SearchFilter filter = new SearchFilter();
        validationHelper.setSearchFilter(filter);
        if (validationHelper.isValid(requestWrapper.getQueryParameters())) {
            handle(requestWrapper, filter, renderer);
            renderer.ok();
        } else {
            throw new ValidationException(validationHelper.getValidationResult());
        }
        return renderer.getObject();
    }

    protected void handle(
            RequestWrapper requestWrapper,
            SearchFilter filter,
            SearchRenderer renderer) {
        for (AMEEEntity entity : searchService.getEntities(filter)) {
            switch (entity.getObjectType()) {
                case DC:
                    dataCategoryBuilder.handle(requestWrapper, (DataCategory) entity, renderer.getDataCategoryRenderer());
                    renderer.newDataCategory();
                    break;
                case DI:
                    dataItemBuilder.handle(requestWrapper, (DataItem) entity, renderer.getDataItemRenderer());
                    renderer.newDataItem();
                    break;
            }
        }
    }

    public SearchRenderer getRenderer(RequestWrapper requestWrapper) {
        try {
            for (String acceptedMediaType : requestWrapper.getAcceptedMediaTypes()) {
                if (RENDERERS.containsKey(acceptedMediaType)) {
                    return (SearchRenderer) RENDERERS.get(acceptedMediaType).newInstance();
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

    public interface SearchRenderer {

        public void ok();

        public void start();

        public void newDataCategory();

        public void newDataItem();

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer();

        public DataItemBuilder.DataItemRenderer getDataItemRenderer();

        public Object getObject();
    }

    public static class SearchJSONRenderer implements SearchRenderer {

        private DataCategoryBuilder.DataCategoryJSONRenderer dataCategoryRenderer;
        private DataItemBuilder.DataItemJSONRenderer dataItemRenderer;
        private JSONObject rootObj;
        private JSONArray resultsArr;

        public SearchJSONRenderer() {
            super();
            this.dataCategoryRenderer = new DataCategoryBuilder.DataCategoryJSONRenderer(false);
            this.dataItemRenderer = new DataItemBuilder.DataItemJSONRenderer();
        }

        public void start() {
            rootObj = new JSONObject();
            resultsArr = new JSONArray();
            put(rootObj, "results", resultsArr);
        }

        public void ok() {
            put(rootObj, "status", "OK");
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

        public Object getObject() {
            return rootObj;
        }
    }

    public static class SearchDOMRenderer implements SearchRenderer {

        private DataCategoryBuilder.DataCategoryDOMRenderer dataCategoryRenderer;
        private DataItemBuilder.DataItemDOMRenderer dataItemRenderer;
        private Element rootElem;
        private Element resultsElem;

        public SearchDOMRenderer() {
            super();
            this.dataCategoryRenderer = new DataCategoryBuilder.DataCategoryDOMRenderer(false);
            this.dataItemRenderer = new DataItemBuilder.DataItemDOMRenderer();
        }

        public void start() {
            rootElem = new Element("Representation");
            resultsElem = new Element("Results");
            rootElem.addContent(resultsElem);
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void newDataCategory() {
            resultsElem.addContent(dataCategoryRenderer.getDataCategoryElement());
        }

        public void newDataItem() {
            resultsElem.addContent(dataItemRenderer.getDataItemElement());
        }

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer() {
            return dataCategoryRenderer;
        }

        public DataItemBuilder.DataItemRenderer getDataItemRenderer() {
            return dataItemRenderer;
        }

        public Document getObject() {
            return new Document(rootElem);
        }
    }
}