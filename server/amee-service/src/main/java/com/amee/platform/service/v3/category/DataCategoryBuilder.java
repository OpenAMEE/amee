package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.environment.Environment;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.tag.Tag;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import com.amee.service.tag.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class DataCategoryBuilder<E> implements ResourceBuilder<E> {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    @Autowired
    private TagService tagService;

    @Transactional(readOnly = true)
    protected void handle(RequestWrapper requestWrapper, DataCategoryRenderer renderer) {
        renderer.start();
        // Get Environment.
        Environment environment = environmentService.getEnvironmentByName("AMEE");
        // Get the DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(environment, dataCategoryIdentifier);
            if (dataCategory != null) {
                // Handle the DataCategory.
                this.handle(requestWrapper, dataCategory, renderer);
                renderer.ok();
            } else {
                renderer.notFound();
            }
        } else {
            renderer.categoryIdentifierMissing();
        }
    }

    public void handle(
            RequestWrapper requestWrapper,
            DataCategory dataCategory,
            DataCategoryRenderer renderer) {

        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean parent = requestWrapper.getMatrixParameters().containsKey("parent");
        boolean authority = requestWrapper.getMatrixParameters().containsKey("authority");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");
        boolean tags = requestWrapper.getMatrixParameters().containsKey("tags");

        // New DataCategory & basic.
        renderer.newDataCategory(dataCategory);
        renderer.addBasic();

        // Optionals.
        if (path || full) {
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataCategory.getEnvironment());
            renderer.addPath(pathItemGroup.findByUId(dataCategory.getUid()));
        }
        if (parent || full) {
            renderer.addParent();
        }
        if (audit || full) {
            renderer.addAudit();
        }
        if (authority || full) {
            renderer.addAuthority();
        }
        if (wikiDoc || full) {
            renderer.addWikiDoc();
        }
        if (provenance || full) {
            renderer.addProvenance();
        }
        if ((itemDefinition || full) && (dataCategory.getItemDefinition() != null)) {
            ItemDefinition id = dataCategory.getItemDefinition();
            renderer.addItemDefinition(id);
        }
        if (tags || full) {
            renderer.startTags();
            for (Tag tag : tagService.getTags(dataCategory)) {
                renderer.newTag(tag);
            }
        }
    }

    public interface DataCategoryRenderer {

        public void start();

        public void ok();

        public void notFound();

        public void notAuthenticated();

        public void categoryIdentifierMissing();

        public void newDataCategory(DataCategory dataCategory);

        public void addBasic();

        public void addPath(PathItem pathItem);

        public void addParent();

        public void addAudit();

        public void addAuthority();

        public void addWikiDoc();

        public void addProvenance();

        public void addItemDefinition(ItemDefinition id);

        public void startTags();

        public void newTag(Tag tag);
    }
}