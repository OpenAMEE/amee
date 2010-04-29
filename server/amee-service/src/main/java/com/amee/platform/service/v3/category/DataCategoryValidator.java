package com.amee.platform.service.v3.category;

import com.amee.base.validation.ValidationSpecification;
import com.amee.domain.data.DataCategory;
import com.amee.service.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@Scope("prototype")
public class DataCategoryValidator implements Validator {

    // Alpha numerics, dash & underscore.
    // private final static String NAME_PATTERN_STRING = "^[a-zA-Z0-9_\\-]*$";

    // Alpha numerics & underscore.
    private final static String WIKI_NAME_PATTERN_STRING = "^[a-zA-Z0-9_]*$";

    @Autowired
    private DataService dataService;

    private ValidationSpecification nameSpec;
    private ValidationSpecification wikiNameSpec;
//    private ValidationSpecification descriptionSpec;
//    private ValidationSpecification unitSpec;
//    private ValidationSpecification directionSpec;
//    private ValidationSpecification aggregationSpec;
//    private ValidationSpecification minPeriodSpec;

    public DataCategoryValidator() {
        // name
        nameSpec = new ValidationSpecification();
        nameSpec.setName("name");
        nameSpec.setMinSize(DataCategory.NAME_MIN_SIZE);
        nameSpec.setMaxSize(DataCategory.NAME_MAX_SIZE);
        // nameSpec.setFormat(NAME_PATTERN_STRING);
        // wikiName
        wikiNameSpec = new ValidationSpecification();
        wikiNameSpec.setName("wikiName");
        wikiNameSpec.setMinSize(DataCategory.WIKI_NAME_MIN_SIZE);
        wikiNameSpec.setMaxSize(DataCategory.WIKI_NAME_MAX_SIZE);
        wikiNameSpec.setFormat(WIKI_NAME_PATTERN_STRING);
    }

    public boolean supports(Class clazz) {
        return DataCategory.class.isAssignableFrom(clazz);
    }

    public void validate(Object o, Errors e) {
        DataCategory datacategory = (DataCategory) o;
        // name
        nameSpec.validate(datacategory.getName(), e);
//        if (datacategory.getId() == null) {
//            // New DataCategory.
//            if (dataService.getDataCategoryByName(datacategory.getName()) != null) {
//                e.rejectValue("name", "taken");
//            }
//        } else {
//            // Existing DataCategory.
//            if (!dataService.isDataCategoryUniqueOnName(datacategory)) {
//                e.rejectValue("name", "taken");
//            }
//        }
        // wikiName
        wikiNameSpec.validate(datacategory.getWikiName(), e);
    }
}

