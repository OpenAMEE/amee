package com.amee.platform.service.v3.tag;

import com.amee.base.validation.ValidationHelper;
import com.amee.domain.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import java.util.HashSet;
import java.util.Set;

@Service
@Scope("prototype")
public class TagValidationHelper extends ValidationHelper {

    @Autowired
    private TagValidator tagValidator;

    private Tag tag;
    private Set<String> allowedFields;

    @Override
    public Object getObject() {
        return tag;
    }

    @Override
    protected Validator getValidator() {
        return tagValidator;
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String[] getAllowedFields() {
        if (allowedFields == null) {
            allowedFields = new HashSet<String>();
            allowedFields.add("tag");
        }
        return allowedFields.toArray(new String[]{});
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}