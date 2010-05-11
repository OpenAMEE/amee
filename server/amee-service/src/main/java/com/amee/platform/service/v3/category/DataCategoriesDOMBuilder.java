package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.platform.search.DataCategoryFilterValidationHelper;
import com.amee.platform.search.SearchService;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DataCategoriesDOMBuilder extends DataCategoriesBuilder<Document> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataCategoryDOMBuilder dataCategoryDOMBuilder;

    @Autowired
    private DataCategoryFilterValidationHelper validationHelper;

    public Document handle(RequestWrapper requestWrapper) {
        DataCategoriesDOMRenderer renderer =
                new DataCategoriesDOMRenderer(new DataCategoryDOMBuilder.DataCategoryDOMRenderer());
        super.handle(requestWrapper, renderer);
        return renderer.getDocument();
    }

    public String getMediaType() {
        return "application/json";
    }

    public class DataCategoriesDOMRenderer implements DataCategoriesBuilder.DataCategoriesRenderer {

        private DataCategoryDOMBuilder.DataCategoryDOMRenderer dataCategoryRenderer;
        private Element rootElem;
        private Element categoriesElem;

        public DataCategoriesDOMRenderer(DataCategoryDOMBuilder.DataCategoryDOMRenderer dataCategoryRenderer) {
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