package com.amee.platform.service.v3.definition;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.service.definition.DefinitionService;
import com.amee.service.environment.EnvironmentService;
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
public class ItemValueDefinitionDOMBuilder implements ResourceBuilder<Document> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DefinitionService definitionService;

    @Transactional(readOnly = true)
    public Document handle(RequestWrapper requestWrapper) {
        Element e = new Element("Representation");
        // Get ItemDefinition identifier.
        String itemDefinitionIdentifier = requestWrapper.getAttributes().get("itemDefinitionIdentifier");
        if (itemDefinitionIdentifier != null) {
            // Get ItemDefinition.
            ItemDefinition itemDefinition = definitionService.getItemDefinitionByUid(
                    environmentService.getEnvironmentByName("AMEE"), itemDefinitionIdentifier);
            if (itemDefinition != null) {
                // Get ItemValueDefinition identifier.
                String itemValueDefinitionIdentifier = requestWrapper.getAttributes().get("itemValueDefinitionIdentifier");
                if (itemValueDefinitionIdentifier != null) {
                    // Get ItemValueDefinition.
                    ItemValueDefinition itemValueDefinition = definitionService.getItemValueDefinitionByUid(
                            itemDefinition, itemValueDefinitionIdentifier);
                    if (itemValueDefinition != null) {
                        // Get ItemValueDefinition Element.
                        e.addContent(getItemValueDefinitionElement(requestWrapper, itemValueDefinition));
                        e.addContent(new Element("Status").setText("OK"));
                    } else {
                        e.addContent(new Element("Status").setText("NOT_FOUND"));
                    }
                } else {
                    e.addContent(new Element("Status").setText("ERROR"));
                    e.addContent(new Element("Error").setText("The itemValueDefinitionIdentifier was missing."));
                }
            } else {
                e.addContent(new Element("Status").setText("NOT_FOUND"));
            }
        } else {
            e.addContent(new Element("Status").setText("ERROR"));
            e.addContent(new Element("Error").setText("The itemDefinitionIdentifier was missing."));
        }
        return new Document(e);
    }

    protected Element getItemValueDefinitionElement(RequestWrapper requestWrapper, ItemValueDefinition itemValueDefinition) {

        Element e = new Element("ItemValueDefinition");
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean name = requestWrapper.getMatrixParameters().containsKey("name");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        e.setAttribute("uid", itemValueDefinition.getUid());

        // Optional attributes.
        if (name || full) {
            e.addContent(new Element("Name").setText(itemValueDefinition.getName()));
        }
        if (path || full) {
            e.addContent(new Element("Path").setText(itemValueDefinition.getPath()));
        }
        if (audit || full) {
            e.setAttribute("status", itemValueDefinition.getStatus().getName());
            e.setAttribute("created", FMT.print(itemValueDefinition.getCreated().getTime()));
            e.setAttribute("modified", FMT.print(itemValueDefinition.getModified().getTime()));
        }
        if (wikiDoc || full) {
            e.addContent(new Element("WikiDoc").setText(itemValueDefinition.getWikiDoc()));
        }
        if ((itemDefinition || full) && (itemValueDefinition.getItemDefinition() != null)) {
            ItemDefinition id = itemValueDefinition.getItemDefinition();
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