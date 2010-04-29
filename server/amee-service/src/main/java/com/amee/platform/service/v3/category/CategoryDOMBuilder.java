package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.service.data.DataService;
import org.jdom.Document;
import org.jdom.Element;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("categoryDOMBuilder")
@Scope("prototype")
public class CategoryDOMBuilder implements ResourceBuilder<Document> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    private DataService dataService;

    @Transactional(readOnly = true)
    public Document handle(RequestWrapper requestWrapper) {
        Element representationElem = new Element("Representation");
        String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (categoryIdentifier != null) {
            // TODO: Need to handle WikiName too.
            // TODO: Needs to be Environment sensitive.
            DataCategory dataCategory = dataService.getDataCategoryByUid(categoryIdentifier);
            if (dataCategory != null) {
                representationElem.addContent(getCategoryElement(requestWrapper, dataCategory));
                representationElem.addContent(new Element("Status").setText("OK"));
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

    protected Element getCategoryElement(RequestWrapper requestWrapper, DataCategory dataCategory) {

        Element categoryElem = new Element("Category");
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean authority = requestWrapper.getMatrixParameters().containsKey("authority");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        categoryElem.setAttribute("uid", dataCategory.getUid());
        categoryElem.addContent(new Element("Name").setText(dataCategory.getName()));
        categoryElem.addContent(new Element("WikiName").setText(dataCategory.getWikiName()));
        if (dataCategory.getDataCategory() != null) {
            categoryElem.addContent(new Element("ParentWikiName").setText(dataCategory.getDataCategory().getWikiName()));
        }

        // Optional attributes.
        if (path || full) {
            categoryElem.addContent(new Element("Path").setText(dataCategory.getPath()));
            categoryElem.addContent(new Element("FullPath").setText("/not/yet/implemented"));
        }
        if (audit || full) {
            categoryElem.setAttribute("status", dataCategory.getStatus().getName());
            categoryElem.setAttribute("created", FMT.print(dataCategory.getCreated().getTime()));
            categoryElem.setAttribute("modified", FMT.print(dataCategory.getModified().getTime()));
        }
        if (authority || full) {
            categoryElem.addContent(new Element("Authority").setText("Not yet implemented."));
        }
        if (wikiDoc || full) {
            categoryElem.addContent(new Element("WikiDoc").setText("Not yet implemented."));
        }
        if (provenance || full) {
            categoryElem.addContent(new Element("Provenance").setText("Not yet implemented."));
        }
        if ((itemDefinition || full) && (dataCategory.getItemDefinition() != null)) {
            ItemDefinition id = dataCategory.getItemDefinition();
            Element itemDefinitionElem = new Element("ItemDefinition");
            categoryElem.addContent(itemDefinitionElem);
            itemDefinitionElem.setAttribute("uid", id.getUid());
            itemDefinitionElem.addContent(new Element("Name").setText(id.getName()));
        }

        return categoryElem;
    }

    public String getMediaType() {
        return "application/xml";
    }
}
