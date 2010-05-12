package com.amee.platform.service.v3.search;

import com.amee.base.resource.RequestWrapper;
import com.amee.platform.search.SearchService;
import com.amee.platform.service.v3.category.DataCategoryBuilder;
import com.amee.platform.service.v3.category.DataCategoryDOMBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SearchDOMBuilder extends SearchBuilder<Document> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataCategoryDOMBuilder dataCategoryDOMBuilder;

    @Autowired
    private SearchFilterValidationHelper validationHelper;

    public Document handle(RequestWrapper requestWrapper) {
        SearchDOMRenderer renderer =
                new SearchDOMRenderer(new DataCategoryDOMBuilder.DataCategoryDOMRenderer());
        super.handle(requestWrapper, renderer);
        return renderer.getDocument();
    }

    public String getMediaType() {
        return "application/json";
    }

    public DataCategoryBuilder getDataCategoryBuilder() {
        return dataCategoryDOMBuilder;
    }

    public class SearchDOMRenderer implements SearchRenderer {

        private DataCategoryDOMBuilder.DataCategoryDOMRenderer dataCategoryRenderer;
        private Element rootElem;
        private Element categoriesElem;

        public SearchDOMRenderer(DataCategoryDOMBuilder.DataCategoryDOMRenderer dataCategoryRenderer) {
            super();
            this.dataCategoryRenderer = dataCategoryRenderer;
        }

        public void start() {
            rootElem = new Element("Representation");
            categoriesElem = new Element("Categories");
            rootElem.addContent(categoriesElem);
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void notAuthenticated() {
            rootElem.addContent(new Element("Status").setText("NOT_AUTHENTICATED"));
        }

        public void newDataCategory() {
            categoriesElem.addContent(dataCategoryRenderer.getDataCategoryElement());
        }

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer() {
            return dataCategoryRenderer;
        }

        public Document getDocument() {
            return new Document(rootElem);
        }
    }
}