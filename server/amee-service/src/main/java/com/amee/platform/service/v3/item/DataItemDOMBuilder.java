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

@Service("dataItemDOMBuilder")
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
        Element representationElem = new Element("Representation");
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
                        representationElem.addContent(getDataItemElement(requestWrapper, dataItem));
                        representationElem.addContent(new Element("Status").setText("OK"));
                    } else {
                        representationElem.addContent(new Element("Status").setText("NOT_FOUND"));
                    }
                } else {
                    representationElem.addContent(new Element("Status").setText("ERROR"));
                    representationElem.addContent(new Element("Error").setText("The itemIdentifier was missing."));
                }
            } else {
                representationElem.addContent(new Element("Status").setText("NOT_FOUND"));
            }
        } else {
            representationElem.addContent(new Element("Status").setText("ERROR"));
            representationElem.addContent(new Element("Error").setText("The categoryIdentifier was missing."));
        }
        representationElem.addContent(new Element("Version").setText(requestWrapper.getVersion().toString()));
        return new Document(representationElem);
    }

    protected Element getDataItemElement(RequestWrapper requestWrapper, DataItem dataItem) {

        Element itemElem = new Element("Item");
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean name = requestWrapper.getMatrixParameters().containsKey("name");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        itemElem.setAttribute("uid", dataItem.getUid());

        // Optional attributes.
        if (path || full) {
            itemElem.addContent(new Element("Name").setText(dataItem.getName()));
            itemElem.addContent(new Element("CategoryWikiName").setText(dataItem.getDataCategory().getWikiName()));
        }
        if (path || full) {
            // Get PathItem.
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataItem.getEnvironment());
            PathItem pathItem = pathItemGroup.findByUId(dataItem.getDataCategory().getUid());
            // Add Paths.
            itemElem.addContent(new Element("Path").setText(dataItem.getPath()));
            if (pathItem != null) {
                itemElem.addContent(new Element("FullPath").setText(pathItem.getFullPath() + "/" + dataItem.getDisplayPath()));
            }
        }
        if (audit || full) {
            itemElem.setAttribute("status", dataItem.getStatus().getName());
            itemElem.setAttribute("created", FMT.print(dataItem.getCreated().getTime()));
            itemElem.setAttribute("modified", FMT.print(dataItem.getModified().getTime()));
        }
        if (wikiDoc || full) {
            itemElem.addContent(new Element("WikiDoc").setText("Not yet implemented."));
        }
        if (provenance || full) {
            itemElem.addContent(new Element("Provenance").setText("Not yet implemented."));
        }
        if ((itemDefinition || full) && (dataItem.getItemDefinition() != null)) {
            ItemDefinition id = dataItem.getItemDefinition();
            Element itemDefinitionElem = new Element("ItemDefinition");
            itemElem.addContent(itemDefinitionElem);
            itemDefinitionElem.setAttribute("uid", id.getUid());
            itemDefinitionElem.addContent(new Element("Name").setText(id.getName()));
        }

        return itemElem;
    }

    public String getMediaType() {
        return "application/xml";
    }
}