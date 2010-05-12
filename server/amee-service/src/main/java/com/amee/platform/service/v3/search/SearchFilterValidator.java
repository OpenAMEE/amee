package com.amee.platform.service.v3.search;

import com.amee.base.validation.ValidationSpecification;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@Scope("prototype")
public class SearchFilterValidator implements Validator {

    private ValidationSpecification qSpec;

    public SearchFilterValidator() {
        super();
        qSpec = new ValidationSpecification();
        qSpec.setName("q");

    }

    public boolean supports(Class clazz) {
        return SearchFilter.class.isAssignableFrom(clazz);
    }

    public void validate(Object o, Errors e) {
        SearchFilter searchFilter = (SearchFilter) o;
        qSpec.validate(searchFilter.getQ(), e);
    }
}