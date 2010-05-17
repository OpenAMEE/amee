package com.amee.platform.service.v3.tag;

import com.amee.base.validation.ValidationSpecification;
import com.amee.domain.tag.Tag;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@Scope("prototype")
public class TagValidator implements Validator {

    // Alpha numerics & underscore.
    private final static String TAG_PATTERN_STRING = "^[a-zA-Z0-9_]*$";

    private ValidationSpecification tagSpec;

    public TagValidator() {
        super();
        // tag
        tagSpec = new ValidationSpecification();
        tagSpec.setName("tag");
        tagSpec.setMinSize(Tag.TAG_MIN_SIZE);
        tagSpec.setMaxSize(Tag.TAG_MAX_SIZE);
        tagSpec.setFormat(TAG_PATTERN_STRING);
    }

    public boolean supports(Class clazz) {
        return Tag.class.isAssignableFrom(clazz);
    }

    public void validate(Object o, Errors e) {
        Tag tag = (Tag) o;
        // tag
        tagSpec.validate(tag.getTag(), e);
    }
}