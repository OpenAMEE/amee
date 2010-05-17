package com.amee.platform.service.v3.tag;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.tag.Tag;
import com.amee.service.tag.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class TagsBuilder<E> implements ResourceBuilder<E> {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagResourceService tagResourceService;

    @Transactional(readOnly = true)
    protected void handle(RequestWrapper requestWrapper, TagsRenderer renderer) {
        renderer.start();
        handle(renderer, tagResourceService.getEntity(requestWrapper));
        renderer.ok();
    }

    protected void handle(TagsRenderer renderer, IAMEEEntityReference entity) {
        for (Tag tag : tagService.getTags(entity)) {
            renderer.newTag(tag);
        }
    }

    public interface TagsRenderer {

        public void ok();

        public void notAuthenticated();

        public void start();

        public void newTag(Tag tag);
    }
}