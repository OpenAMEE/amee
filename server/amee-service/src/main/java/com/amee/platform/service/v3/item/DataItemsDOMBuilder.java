package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.platform.search.DataItemFilterValidationHelper;
import com.amee.platform.search.SearchService;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DataItemsDOMBuilder extends DataItemsBuilder<Document> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataItemDOMBuilder DataItemDOMBuilder;

    @Autowired
    private DataItemFilterValidationHelper validationHelper;

    public Document handle(RequestWrapper requestWrapper) {
        DataItemsDOMRenderer renderer =
                new DataItemsDOMRenderer(new DataItemDOMBuilder.DataItemDOMRenderer());
        super.handle(requestWrapper, renderer);
        return renderer.getDocument();
    }

    public String getMediaType() {
        return "application/json";
    }

    public DataItemBuilder getDataItemBuilder() {
        return DataItemDOMBuilder;
    }

    public class DataItemsDOMRenderer implements DataItemsBuilder.DataItemsRenderer {

        private DataItemDOMBuilder.DataItemDOMRenderer DataItemRenderer;
        private Element rootElem;
        private Element ItemsElem;

        public DataItemsDOMRenderer(DataItemDOMBuilder.DataItemDOMRenderer DataItemRenderer) {
            super();
            this.DataItemRenderer = DataItemRenderer;
        }

        public void start() {
            rootElem = new Element("Representation");
            ItemsElem = new Element("Items");
            rootElem.addContent(ItemsElem);
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void notFound() {
            rootElem.addContent(new Element("Status").setText("NOT_FOUND"));
        }

        public void notAuthenticated() {
            rootElem.addContent(new Element("Status").setText("NOT_AUTHENTICATED"));
        }

        public void categoryIdentifierMissing() {
            rootElem.addContent(new Element("Status").setText("ERROR"));
            rootElem.addContent(new Element("Error").setText("The categoryIdentifier was missing."));
        }

        public void newDataItem() {
            ItemsElem.addContent(DataItemRenderer.getDataItemElement());
        }

        public DataItemBuilder.DataItemRenderer getDataItemRenderer() {
            return DataItemRenderer;
        }

        public Document getDocument() {
            return new Document(rootElem);
        }
    }
}