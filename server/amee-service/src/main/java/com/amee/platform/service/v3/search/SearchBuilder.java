package com.amee.platform.service.v3.search;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.platform.search.SearchService;
import com.amee.platform.service.v3.category.DataCategoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class SearchBuilder<E> implements ResourceBuilder<E> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchFilterValidationHelper validationHelper;

    @Transactional(readOnly = true)
    protected void handle(RequestWrapper requestWrapper, SearchRenderer renderer) {
        SearchFilter filter = new SearchFilter();
        validationHelper.setSearchFilter(filter);
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
            SearchFilter filter,
            SearchRenderer renderer) {
        for (DataCategory dataCategory : searchService.getDataCategories(filter.getQ())) {
            getDataCategoryBuilder().handle(requestWrapper, dataCategory, renderer.getDataCategoryRenderer());
            renderer.newDataCategory();
        }
    }

    public abstract DataCategoryBuilder getDataCategoryBuilder();

    public interface SearchRenderer {

        public void ok();

        public void notAuthenticated();

        public void start();

        public void newDataCategory();

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer();
    }
}