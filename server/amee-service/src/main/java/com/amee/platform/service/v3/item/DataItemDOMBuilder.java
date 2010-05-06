package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import org.jdom.Document;
import org.jdom.Element;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class DataItemDOMBuilder implements ResourceBuilder<Document> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    @Transactional(readOnly = true)
    public Document handle(RequestWrapper requestWrapper) {
        Element e = new Element("Representation");
        // Get DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                    environmentService.getEnvironmentByName("AMEE"), dataCategoryIdentifier);
            if (dataCategory != null) {
                // Get DataItem identifier.
                String dataItemIdentifier = requestWrapper.getAttributes().get("itemIdentifier");
                if (dataItemIdentifier != null) {
                    // Get DataItem.
                    DataItem dataItem = dataService.getDataItemByUid(dataCategory, dataItemIdentifier);
                    if (dataItem != null) {
                        // Get DataItem Element.
                        e.addContent(getDataItemElement(requestWrapper, dataItem));
                        e.addContent(new Element("Status").setText("OK"));
                    } else {
                        e.addContent(new Element("Status").setText("NOT_FOUND"));
                    }
                } else {
                    e.addContent(new Element("Status").setText("ERROR"));
                    e.addContent(new Element("Error").setText("The itemIdentifier was missing."));
                }
            } else {
                e.addContent(new Element("Status").setText("NOT_FOUND"));
            }
        } else {
            e.addContent(new Element("Status").setText("ERROR"));
            e.addContent(new Element("Error").setText("The categoryIdentifier was missing."));
        }
        return new Document(e);
    }

    protected Element getDataItemElement(RequestWrapper requestWrapper, DataItem dataItem) {

        Element e = new Element("Item");
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean name = requestWrapper.getMatrixParameters().containsKey("name");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        e.setAttribute("uid", dataItem.getUid());

        // Optional attributes.
        if (name || full) {
            e.addContent(new Element("Name").setText(dataItem.getName()));
            e.addContent(new Element("CategoryWikiName").setText(dataItem.getDataCategory().getWikiName()));
        }
        if (path || full) {
            // Get PathItem.
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataItem.getEnvironment());
            PathItem pathItem = pathItemGroup.findByUId(dataItem.getDataCategory().getUid());
            // Add Paths.
            e.addContent(new Element("Path").setText(dataItem.getPath()));
            if (pathItem != null) {
                e.addContent(new Element("FullPath").setText(pathItem.getFullPath() + "/" + dataItem.getDisplayPath()));
            }
        }
        if (audit || full) {
            e.setAttribute("status", dataItem.getStatus().getName());
            e.setAttribute("created", FMT.print(dataItem.getCreated().getTime()));
            e.setAttribute("modified", FMT.print(dataItem.getModified().getTime()));
        }
        if (wikiDoc || full) {
            e.addContent(new Element("WikiDoc").setText(dataItem.getWikiDoc()));
        }
        if (provenance || full) {
            e.addContent(new Element("Provenance").setText(dataItem.getProvenance()));
        }
        if ((itemDefinition || full) && (dataItem.getItemDefinition() != null)) {
            ItemDefinition id = dataItem.getItemDefinition();
            Element e2 = new Element("ItemDefinition");
            e.addContent(e2);
            e2.setAttribute("uid", id.getUid());
            e2.addContent(new Element("Name").setText(id.getName()));
        }

        return e;
    }

    public String getMediaType() {
        return "application/xml";
    }
}