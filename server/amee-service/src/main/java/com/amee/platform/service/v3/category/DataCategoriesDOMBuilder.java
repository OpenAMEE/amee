package com.amee.platform.service.v3.category;

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
public class DataCategoriesDOMBuilder implements ResourceBuilder<Document> {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataCategoryDOMBuilder dataCategoryDOMBuilder;

    @Transactional(readOnly = true)
    public Document handle(RequestWrapper requestWrapper) {
        Element representationElem = new Element("Representation");
        representationElem.addContent(getDataCategoriesElement(requestWrapper));
        representationElem.addContent(new Element("Status").setText("OK"));
        return new Document(representationElem, new DocType("xml"));
    }

    protected Element getDataCategoriesElement(RequestWrapper requestWrapper) {
        Element categoriesElem = new Element("Categories");
        for (DataCategory dataCategory : dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
            categoriesElem.addContent(dataCategoryDOMBuilder.getDataCategoryElement(requestWrapper, dataCategory));
        }
        return categoriesElem;
    }

    public String getMediaType() {
        return "application/json";
    }
}