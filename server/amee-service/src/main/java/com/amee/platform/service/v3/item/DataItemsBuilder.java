package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.base.validation.ValidationException;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.environment.Environment;
import com.amee.platform.search.DataItemFilter;
import com.amee.platform.search.DataItemFilterValidationHelper;
import com.amee.platform.search.QueryFilter;
import com.amee.platform.search.SearchService;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class DataItemsBuilder<E> implements ResourceBuilder<E> {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataItemFilterValidationHelper validationHelper;

    @Transactional(readOnly = true)
    protected void handle(RequestWrapper requestWrapper, DataItemsRenderer renderer) {
        renderer.start();
        // Get Environment.
        Environment environment = environmentService.getEnvironmentByName("AMEE");
        // Get DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(environment, dataCategoryIdentifier);
            if ((dataCategory != null) && (dataCategory.getItemDefinition() != null)) {
                // Create filter and do search.
                DataItemFilter filter = new DataItemFilter(dataCategory.getItemDefinition());
                validationHelper.setDataItemFilter(filter);
                if (validationHelper.isValid(requestWrapper.getQueryParameters())) {
                    handle(requestWrapper, dataCategory, filter, renderer);
                    renderer.ok();
                } else {
                    throw new ValidationException(validationHelper.getValidationResult());
                }
            } else {
                renderer.notFound();
            }
        } else {
            renderer.categoryIdentifierMissing();
        }
    }

    protected void handle(
            RequestWrapper requestWrapper,
            DataCategory dataCategory,
            QueryFilter filter,
            DataItemsRenderer renderer) {
        for (DataItem dataItem : searchService.getDataItems(dataCategory, filter)) {
            getDataItemBuilder().handle(requestWrapper, dataItem, renderer.getDataItemRenderer());
            renderer.newDataItem();
        }
    }

    public abstract DataItemBuilder getDataItemBuilder();

    public interface DataItemsRenderer {

        public void ok();

        public void notFound();

        public void notAuthenticated();

        public void categoryIdentifierMissing();

        public void start();

        public void newDataItem();

        public DataItemBuilder.DataItemRenderer getDataItemRenderer();
    }
}