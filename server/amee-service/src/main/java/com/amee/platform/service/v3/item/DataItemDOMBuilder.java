package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.path.PathItem;
import org.jdom.Document;
import org.jdom.Element;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DataItemDOMBuilder extends DataItemBuilder<Document> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    public Document handle(RequestWrapper requestWrapper) {
        DataItemDOMRenderer renderer = new DataItemDOMRenderer();
        super.handle(requestWrapper, renderer);
        return renderer.getDocument();
    }

    public String getMediaType() {
        return "application/xml";
    }

    public class DataItemDOMRenderer implements DataItemRenderer {

        private DataItem dataItem;
        private Element rootElem;
        private Element dataItemElem;

        public DataItemDOMRenderer() {
            super();
            rootElem = new Element("Representation");
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

        public void itemIdentifierMissing() {
            rootElem.addContent(new Element("Status").setText("ERROR"));
            rootElem.addContent(new Element("Error").setText("The itemIdentifier was missing."));
        }

        public void categoryIdentifierMissing() {
            rootElem.addContent(new Element("Status").setText("ERROR"));
            rootElem.addContent(new Element("Error").setText("The categoryIdentifier was missing."));
        }

        public void newDataItem(DataItem dataItem) {
            this.dataItem = dataItem;
            dataItemElem = new Element("Item");
            rootElem.addContent(dataItemElem);
        }

        public void addBasic() {
            dataItemElem.setAttribute("uid", dataItem.getUid());
        }

        public void addName() {
            dataItemElem.addContent(new Element("Name").setText(dataItem.getName()));
            dataItemElem.addContent(new Element("CategoryWikiName").setText(dataItem.getDataCategory().getWikiName()));
        }

        public void addPath(PathItem pathItem) {
            dataItemElem.addContent(new Element("Path").setText(dataItem.getPath()));
            if (pathItem != null) {
                dataItemElem.addContent(new Element("FullPath").setText(pathItem.getFullPath() + "/" + dataItem.getDisplayPath()));
            }
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

        public Document getDocument() {
            return new Document(rootElem);
        }
    }
}