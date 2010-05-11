package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.platform.search.DataCategoryFilter;
import com.amee.platform.search.DataCategoryFilterValidationHelper;
import com.amee.platform.search.SearchService;
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
    private SearchService searchService;

    @Autowired
    private DataCategoryDOMBuilder dataCategoryDOMBuilder;

    @Autowired
    private DataCategoryFilterValidationHelper validationHelper;

    @Transactional(readOnly = true)
    public Document handle(RequestWrapper requestWrapper) {
        Element result = new Element("Representation");
        DataCategoryFilter filter = new DataCategoryFilter();
        validationHelper.setDataCategoryFilter(filter);
        if (validationHelper.isValid(requestWrapper.getQueryParameters())) {
            result.addContent(getDataCategoriesElement(requestWrapper, filter));
            result.addContent(new Element("Status").setText("OK"));
        } else {
            throw new ValidationException(validationHelper.getValidationResult());
        }
        return new Document(result, new DocType("xml"));
    }

    protected Element getDataCategoriesElement(RequestWrapper requestWrapper, DataCategoryFilter filter) {
        Element categoriesElem = new Element("Categories");
        for (DataCategory dataCategory : searchService.getDataCategories(filter)) {
            categoriesElem.addContent(dataCategoryDOMBuilder.getDataCategoryElement(requestWrapper, dataCategory));
        }
        return categoriesElem;
    }

    public String getMediaType() {
        return "application/json";
    }
}