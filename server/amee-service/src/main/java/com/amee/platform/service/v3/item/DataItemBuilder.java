package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.auth.AuthenticationService;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class DataItemBuilder<E> implements ResourceBuilder<E> {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    @Transactional(readOnly = true)
    protected void handle(RequestWrapper requestWrapper, DataItemRenderer renderer) {
        // Get Environment.
        Environment environment = environmentService.getEnvironmentByName("AMEE");
//        // Authenticate - Create sample User.
//        User sampleUser = new User();
//        sampleUser.setEnvironment(environment);
//        sampleUser.setUsername(requestWrapper.getAttributes().get("username"));
//        sampleUser.setPasswordInClear(requestWrapper.getAttributes().get("password"));
//        // Authenticate - Check sample User.
//        User authUser = authenticationService.authenticate(sampleUser);
//        if (authUser != null) {
        // Get DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(environment, dataCategoryIdentifier);
            if (dataCategory != null) {
                // Get DataItem identifier.
                String dataItemIdentifier = requestWrapper.getAttributes().get("itemIdentifier");
                if (dataItemIdentifier != null) {
                    // Get DataItem.
                    DataItem dataItem = dataService.getDataItemByUid(dataCategory, dataItemIdentifier);
                    if (dataItem != null) {
                        // Handle the DataItem.
                        this.handle(requestWrapper, dataItem, renderer);
                        renderer.ok();
                    } else {
                        renderer.notFound();
                    }
                } else {
                    renderer.itemIdentifierMissing();
                }
            } else {
                renderer.notFound();
            }
        } else {
            renderer.categoryIdentifierMissing();
        }
//        } else {
//            renderer.notAuthenticated();
//        }
    }

    protected void handle(
            RequestWrapper requestWrapper,
            DataItem dataItem,
            DataItemRenderer renderer) {

        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean name = requestWrapper.getMatrixParameters().containsKey("name");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // New DataItem & basic.
        renderer.newDataItem(dataItem);
        renderer.addBasic();

        // Optionals.
        if (name || full) {
            renderer.addName();
        }
        if (path || full) {
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataItem.getEnvironment());
            renderer.addPath(pathItemGroup.findByUId(dataItem.getDataCategory().getUid()));
        }
        if (audit || full) {
            renderer.addAudit();
        }
        if (wikiDoc || full) {
            renderer.addWikiDoc();
        }
        if (provenance || full) {
            renderer.addProvenance();
        }
        if ((itemDefinition || full) && (dataItem.getItemDefinition() != null)) {
            ItemDefinition id = dataItem.getItemDefinition();
            renderer.addItemDefinition(id);
        }
    }

    public interface DataItemRenderer {

        public void ok();

        public void notFound();

        public void notAuthenticated();

        public void itemIdentifierMissing();

        public void categoryIdentifierMissing();

        public void newDataItem(DataItem dataItem);

        public void addBasic();

        public void addName();

        public void addPath(PathItem pathItem);

        public void addAudit();

        public void addWikiDoc();

        public void addProvenance();

        public void addItemDefinition(ItemDefinition id);
    }
}