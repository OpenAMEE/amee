package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.platform.search.DataCategoryFilter;
import com.amee.platform.search.DataCategoryFilterValidationHelper;
import com.amee.platform.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class DataCategoriesBuilder<E> implements ResourceBuilder<E> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataCategoryDOMBuilder dataCategoryDOMBuilder;

    @Autowired
    private DataCategoryFilterValidationHelper validationHelper;

    @Transactional(readOnly = true)
    protected void handle(RequestWrapper requestWrapper, DataCategoriesRenderer renderer) {
        DataCategoryFilter filter = new DataCategoryFilter();
        validationHelper.setDataCategoryFilter(filter);
        if (validationHelper.isValid(requestWrapper.getQueryParameters())) {
            renderer.start();
            handle(requestWrapper, filter, renderer);
            renderer.ok();
        } else {
            throw new ValidationException(validationHelper.getValidationResult());
        }
    }

    protected void handle(
            RequestWrapper requestWrapper,
            DataCategoryFilter filter,
            DataCategoriesRenderer renderer) {
        for (DataCategory dataCategory : searchService.getDataCategories(filter)) {
            dataCategoryDOMBuilder.handle(requestWrapper, dataCategory, renderer.getDataCategoryRenderer());
            renderer.newDataCategory();
        }
    }

    public interface DataCategoriesRenderer {

        public void ok();

        public void notAuthenticated();

        public void start();

        public void newDataCategory();

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer();
    }
}