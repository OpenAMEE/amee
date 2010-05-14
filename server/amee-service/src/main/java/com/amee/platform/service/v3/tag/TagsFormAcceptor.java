package com.amee.platform.service.v3.tag;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceAcceptor;
import com.amee.base.validation.ValidationException;
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
        renderer.start();
        // Get DataCategory identifier.
        String dataCategoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
        if (dataCategoryIdentifier != null) {
            // Get DataCategory.
            DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                    environmentService.getEnvironmentByName("AMEE"), dataCategoryIdentifier);
            if (dataCategory != null) {
                // Create Tag.
                Tag tag = new Tag();
                validationHelper.setTag(tag);
                if (validationHelper.isValid(requestWrapper.getFormParameters())) {
                    // Save Tag.
                    tagService.persist(tag);
                    // Create and save EntityTag.
                    EntityTag entityTag = new EntityTag(dataCategory, tag);
                    tagService.persist(entityTag);
                    // Woo!
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
        return (JSONObject) renderer.getResult();
    }

    public interface TagAcceptorRenderer {

        public void start();

        public void ok();

        public void notFound();

        public void notAuthenticated();

        public void categoryIdentifierMissing();

        public Object getResult();
    }

    public static class TagAcceptorJSONRenderer implements TagAcceptorRenderer {

        private JSONObject rootObj;

        public TagAcceptorJSONRenderer() {
            super();
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

        public void categoryIdentifierMissing() {
            put(rootObj, "status", "ERROR");
            put(rootObj, "error", "The categoryIdentifier was missing.");
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