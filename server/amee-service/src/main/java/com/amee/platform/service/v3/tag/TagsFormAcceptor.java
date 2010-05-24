package com.amee.platform.service.v3.tag;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceAcceptor;
import com.amee.base.validation.ValidationException;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.tag.EntityTag;
import com.amee.domain.tag.Tag;
import com.amee.service.tag.TagService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class TagsFormAcceptor implements ResourceAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private TagService tagService;

    @Autowired
    private TagResourceService tagResourceService;

    @Autowired
    private TagValidationHelper validationHelper;

    @Transactional(rollbackFor = {ValidationException.class})
    public JSONObject handle(RequestWrapper requestWrapper) throws ValidationException {
        TagAcceptorRenderer renderer = new TagAcceptorJSONRenderer();
        // Create new Tag.
        Tag tag = new Tag();
        validationHelper.setTag(tag);
        if (validationHelper.isValid(requestWrapper.getFormParameters())) {
            // Swap tag with existing tag if it exists.
            Tag existingTag = tagService.getTag(tag.getTag());
            if (existingTag == null) {
                // Save new Tag.
                log.debug("handle() Use new Tag.");
                tagService.persist(tag);
            } else {
                // Use existing tag.
                log.debug("handle() Use existing Tag.");
                tag = existingTag;
            }
            // Deal with an entity if present.
            IAMEEEntityReference entity = tagResourceService.getEntity(requestWrapper);
            if (entity != null) {
                // Find existing EntityTag.
                EntityTag entityTag = tagService.getEntityTag(entity, tag.getTag());
                if (entityTag == null) {
                    // Create and save EntityTag.
                    log.debug("handle() Use new EntityTag.");
                    entityTag = new EntityTag(entity, tag);
                    tagService.persist(entityTag);
                } else {
                    log.debug("handle() EntityTag already exists.");
                }
            } else {
                log.debug("handle() No entity.");
            }
            // Woo!
            renderer.ok();
        } else {
            throw new ValidationException(validationHelper.getValidationResult());
        }
        return (JSONObject) renderer.getResult();
    }

    public interface TagAcceptorRenderer {

        public void start();

        public void ok();

        public Object getResult();
    }

    public static class TagAcceptorJSONRenderer implements TagAcceptorRenderer {

        private JSONObject rootObj;

        public TagAcceptorJSONRenderer() {
            super();
            start();
        }

        public void start() {
            rootObj = new JSONObject();
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public JSONObject getResult() {
            return rootObj;
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }
    }
}