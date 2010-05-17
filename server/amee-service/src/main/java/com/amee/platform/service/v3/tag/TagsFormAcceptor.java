package com.amee.platform.service.v3.tag;

import com.amee.base.resource.NotFoundException;
import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceAcceptor;
import com.amee.base.validation.ValidationException;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.data.DataCategory;
import com.amee.domain.tag.EntityTag;
import com.amee.domain.tag.Tag;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.tag.TagService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class TagsFormAcceptor implements ResourceAcceptor {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private TagService tagService;

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
                tagService.persist(tag);
            } else {
                // Use existing tag.
                tag = existingTag;
            }
            // Deal with an entity if present.
            IAMEEEntityReference entity = getEntity(requestWrapper);
            if (entity != null) {
                // Create and save EntityTag.
                EntityTag entityTag = new EntityTag(entity, tag);
                tagService.persist(entityTag);
            }
            // Woo!
            renderer.ok();
        } else {
            throw new ValidationException(validationHelper.getValidationResult());
        }
        return (JSONObject) renderer.getResult();
    }

    /**
     * Get the entity that Tags should belong to.
     * <p/>
     * This base implementation returns null.
     * <p/>
     * TODO: This is a cut-and-paste of code in TagsBuilder.
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

    public interface TagAcceptorRenderer {

        public void start();

        public void ok();

        public void notFound();

        public void notAuthenticated();

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

        public void notFound() {
            put(rootObj, "status", "NOT_FOUND");
        }

        public void notAuthenticated() {
            put(rootObj, "status", "NOT_AUTHENTICATED");
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