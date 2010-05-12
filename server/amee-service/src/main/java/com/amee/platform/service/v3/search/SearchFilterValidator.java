package com.amee.platform.service.v3.search;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@Scope("prototype")
public class SearchFilterValidator implements Validator {

    public SearchFilterValidator() {
        super();
    }

    public boolean supports(Class clazz) {
        return SearchFilter.class.isAssignableFrom(clazz);
    }

    public void validate(Object o, Errors e) {
        // Do nothing. The Editors do the validation.
    }
}