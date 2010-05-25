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

    // Alpha numerics & underscore.
    private final static String PATH_PATTERN_STRING = "^[a-zA-Z0-9_\\-]*$";
    private final static String WIKI_NAME_PATTERN_STRING = PATH_PATTERN_STRING;

    @Autowired
    private DataService dataService;

    private ValidationSpecification nameSpec;
    private ValidationSpecification pathSpec;
    private ValidationSpecification wikiNameSpec;
    private ValidationSpecification wikiDocSpec;
    private ValidationSpecification provenanceSpec;
    private ValidationSpecification authoritySpec;

    public DataCategoryValidator() {
        super();
        // name
        nameSpec = new ValidationSpecification();
        nameSpec.setName("name");
        nameSpec.setMinSize(DataCategory.NAME_MIN_SIZE);
        nameSpec.setMaxSize(DataCategory.NAME_MAX_SIZE);
        // path
        pathSpec = new ValidationSpecification();
        pathSpec.setName("path");
        pathSpec.setMinSize(DataCategory.PATH_MIN_SIZE);
        pathSpec.setMaxSize(DataCategory.PATH_MAX_SIZE);
        pathSpec.setFormat(PATH_PATTERN_STRING);
        pathSpec.setAllowEmpty(true);
        // wikiName
        wikiNameSpec = new ValidationSpecification();
        wikiNameSpec.setName("wikiName");
        wikiNameSpec.setMinSize(DataCategory.WIKI_NAME_MIN_SIZE);
        wikiNameSpec.setMaxSize(DataCategory.WIKI_NAME_MAX_SIZE);
        wikiNameSpec.setFormat(WIKI_NAME_PATTERN_STRING);
        // wikiDoc
        wikiDocSpec = new ValidationSpecification();
        wikiDocSpec.setName("wikiDoc");
        wikiDocSpec.setMaxSize(DataCategory.WIKI_DOC_MAX_SIZE);
        wikiDocSpec.setAllowEmpty(true);
        // provenance
        provenanceSpec = new ValidationSpecification();
        provenanceSpec.setName("provenance");
        provenanceSpec.setMaxSize(DataCategory.PROVENANCE_MAX_SIZE);
        provenanceSpec.setAllowEmpty(true);
        // authority
        authoritySpec = new ValidationSpecification();
        authoritySpec.setName("authority");
        authoritySpec.setMaxSize(DataCategory.AUTHORITY_MAX_SIZE);
        authoritySpec.setAllowEmpty(true);
    }

    public boolean supports(Class clazz) {
        return DataCategory.class.isAssignableFrom(clazz);
    }

    public void validate(Object o, Errors e) {
        DataCategory datacategory = (DataCategory) o;
        // name
        nameSpec.validate(datacategory.getName(), e);
        // path
        // TODO: This must be unique amongst peers.
        pathSpec.validate(datacategory.getPath(), e);
        // wikiName
        // TODO: This must be unique.
        wikiNameSpec.validate(datacategory.getWikiName(), e);
        // wikiDoc
        wikiDocSpec.validate(datacategory.getWikiDoc(), e);
        // provenance
        provenanceSpec.validate(datacategory.getProvenance(), e);
        // authority
        authoritySpec.validate(datacategory.getAuthority(), e);
    }
}

