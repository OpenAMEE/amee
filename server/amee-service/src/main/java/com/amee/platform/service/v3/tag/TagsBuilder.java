package com.amee.platform.service.v3.tag;

import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.data.DataCategory;
import com.amee.domain.tag.Tag;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.tag.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class TagsBuilder<E> implements ResourceBuilder<E> {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private TagService tagService;

    @Transactional(readOnly = true)
    protected void handle(RequestWrapper requestWrapper, TagsRenderer renderer) {
        renderer.start();
        handle(renderer, getEntity(requestWrapper));
        renderer.ok();
    }

    protected void handle(TagsRenderer renderer, IAMEEEntityReference entity) {
        for (Tag tag : tagService.getTags(entity)) {
            renderer.newTag(tag);
        }
    }

    /**
     * Get the entity that Tags should belong to.
     * <p/>
     * This base implementation returns null.
     * <p/>
     * TODO: This is a cut-and-paste of code in TagsFormAcceptor.
     *
     * @param requestWrapper
     * @return IAMEEEntityReference entity reference
     */
    public IAMEEEntityReference getEntity(RequestWrapper requestWrapper) {
        if (requestWrapper.getAttributes().containsKey("categoryIdentifier")) {
            String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
            if (dataCategoryIdentifier != null) {
                DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                        environmentService.getEnvironmentByName("AMEE"),
                        dataCategoryIdentifier);
                if (dataCategory != null) {
                    return dataCategory;
                } else {
                    throw new NotFoundException();
                }
            }
        }
        return null;
    }

    public interface TagsRenderer {

        public void ok();

        public void notAuthenticated();

        public void start();

        public void newTag(Tag tag);
    }
}