package com.amee.platform.service.v3.category;

import com.amee.base.resource.Renderer;
import com.amee.base.resource.RendererHelper;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.platform.search.DataCategoryFilter;
import com.amee.platform.search.DataCategoryFilterValidationHelper;
import com.amee.platform.search.SearchService;
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
public class DataCategoriesBuilder implements ResourceBuilder {

    private final static Map<String, Class> RENDERERS = new HashMap<String, Class>() {
        {
            put("application/json", DataCategoriesJSONRenderer.class);
            put("application/xml", DataCategoriesDOMRenderer.class);
        }
    };

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataCategoryFilterValidationHelper validationHelper;

    @Autowired
    private DataCategoryBuilder dataCategoryBuilder;

    private DataCategoriesRenderer renderer;

    @Transactional(readOnly = true)
    public Object handle(RequestWrapper requestWrapper) {
        renderer = new RendererHelper<DataCategoriesRenderer>().getRenderer(requestWrapper, RENDERERS);
        DataCategoryFilter filter = new DataCategoryFilter();
        validationHelper.setDataCategoryFilter(filter);
        if (validationHelper.isValid(requestWrapper.getQueryParameters())) {
            handle(requestWrapper, filter);
            renderer.ok();
        } else {
            throw new ValidationException(validationHelper.getValidationResult());
        }
        return renderer.getObject();
    }

    protected void handle(
            RequestWrapper requestWrapper,
            DataCategoryFilter filter) {
        for (DataCategory dataCategory : searchService.getDataCategories(filter)) {
            dataCategoryBuilder.handle(requestWrapper, dataCategory, renderer.getDataCategoryRenderer());
            renderer.newDataCategory();
        }
    }

    public interface DataCategoriesRenderer<E> extends Renderer<E> {

        public void ok();

        public void start();

        public void newDataCategory();

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer();

        public E getObject();
    }

    public static class DataCategoriesJSONRenderer implements DataCategoriesBuilder.DataCategoriesRenderer {

        private DataCategoryBuilder.DataCategoryJSONRenderer dataCategoryRenderer;
        private JSONObject rootObj;
        private JSONArray categoriesArr;

        public DataCategoriesJSONRenderer() {
            super();
            this.dataCategoryRenderer = new DataCategoryBuilder.DataCategoryJSONRenderer(false);
            start();
        }

        public void start() {
            rootObj = new JSONObject();
            categoriesArr = new JSONArray();
            put(rootObj, "categories", categoriesArr);
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public void newDataCategory() {
            categoriesArr.put(dataCategoryRenderer.getDataCategoryJSONObject());
        }

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer() {
            return dataCategoryRenderer;
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getObject() {
            return rootObj;
        }
    }

    public static class DataCategoriesDOMRenderer implements DataCategoriesBuilder.DataCategoriesRenderer {

        private DataCategoryBuilder.DataCategoryDOMRenderer dataCategoryRenderer;
        private Element rootElem;
        private Element categoriesElem;

        public DataCategoriesDOMRenderer() {
            super();
            this.dataCategoryRenderer = new DataCategoryBuilder.DataCategoryDOMRenderer(false);
            start();
        }

        public void start() {
            rootElem = new Element("Representation");
            categoriesElem = new Element("Categories");
            rootElem.addContent(categoriesElem);
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void newDataCategory() {
            categoriesElem.addContent(dataCategoryRenderer.getDataCategoryElement());
        }

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer() {
            return dataCategoryRenderer;
        }

        public Document getObject() {
            return new Document(rootElem);
        }
    }
}