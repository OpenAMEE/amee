package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
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
public class DataCategoryDOMBuilder implements ResourceBuilder<Document> {

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
        String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (categoryIdentifier != null) {
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                    environmentService.getEnvironmentByName("AMEE"), categoryIdentifier);
            if (dataCategory != null) {
                representationElem.addContent(getDataCategoryElement(requestWrapper, dataCategory));
                representationElem.addContent(new Element("Status").setText("OK"));
            } else {
                representationElem.addContent(new Element("Status").setText("NOT_FOUND"));
            }
        } else {
            representationElem.addContent(new Element("Status").setText("ERROR"));
            representationElem.addContent(new Element("Error").setText("The categoryIdentifier was missing."));
        }
        return new Document(representationElem);
    }

    protected Element getDataCategoryElement(RequestWrapper requestWrapper, DataCategory dataCategory) {

        Element e = new Element("Category");
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean authority = requestWrapper.getMatrixParameters().containsKey("authority");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        e.setAttribute("uid", dataCategory.getUid());
        e.addContent(new Element("Name").setText(dataCategory.getName()));
        e.addContent(new Element("WikiName").setText(dataCategory.getWikiName()));
        if (dataCategory.getDataCategory() != null) {
            e.addContent(new Element("ParentWikiName").setText(dataCategory.getDataCategory().getWikiName()));
        }

        // Optional attributes.
        if (path || full) {
            // Get PathItem.
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataCategory.getEnvironment());
            PathItem pathItem = pathItemGroup.findByUId(dataCategory.getUid());
            // Add Paths.
            e.addContent(new Element("Path").setText(dataCategory.getPath()));
            if (pathItem != null) {
                e.addContent(new Element("FullPath").setText(pathItem.getFullPath()));
            }
        }
        if (audit || full) {
            e.setAttribute("status", dataCategory.getStatus().getName());
            e.setAttribute("created", FMT.print(dataCategory.getCreated().getTime()));
            e.setAttribute("modified", FMT.print(dataCategory.getModified().getTime()));
        }
        if (authority || full) {
            e.addContent(new Element("Authority").setText(dataCategory.getAuthority()));
        }
        if (wikiDoc || full) {
            e.addContent(new Element("WikiDoc").setText(dataCategory.getWikiDoc()));
        }
        if (provenance || full) {
            e.addContent(new Element("Provenance").setText(dataCategory.getProvenance()));
        }
        if ((itemDefinition || full) && (dataCategory.getItemDefinition() != null)) {
            ItemDefinition id = dataCategory.getItemDefinition();
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
