package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class DataItemsDOMBuilder implements ResourceBuilder<Document> {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataItemDOMBuilder dataItemDOMBuilder;

    @Transactional(readOnly = true)
    public Document handle(RequestWrapper requestWrapper) {
        Element e = new Element("Representation");
        // Get DataCategory identifier.
        String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (categoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                    environmentService.getEnvironmentByName("AMEE"), categoryIdentifier);
            if (dataCategory != null) {
                // Get DataItems.
                e.addContent(getDataItemsElement(requestWrapper, dataCategory));
                e.addContent(new Element("Status").setText("OK"));
            } else {
                e.addContent(new Element("Status").setText("NOT_FOUND"));
            }
        } else {
            e.addContent(new Element("Status").setText("ERROR"));
            e.addContent(new Element("Error").setText("The categoryIdentifier was missing."));
        }
        return new Document(e, new DocType("xml"));
    }

    protected Element getDataItemsElement(RequestWrapper requestWrapper, DataCategory dataCategory) {
        Element e = new Element("Items");
//        for (DataCategory dataCategory : dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
//            categoriesElem.addContent(categoryDOMBuilder.getDataCategoryElement(requestWrapper, dataCategory));
//        }
        return e;
    }

    public String getMediaType() {
        return "application/json";
    }
}