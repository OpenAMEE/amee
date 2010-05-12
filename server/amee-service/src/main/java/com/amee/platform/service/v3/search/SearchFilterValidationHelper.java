package com.amee.platform.service.v3.search;

import com.amee.base.validation.ValidationHelper;
import com.amee.platform.search.MultiFieldQueryParserEditor;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

import java.util.HashSet;
import java.util.Set;

@Service
@Scope("prototype")
public class SearchFilterValidationHelper extends ValidationHelper {

    @Autowired
    private SearchFilterValidator validator;

    private SearchFilter searchFilter;
    private Set<String> allowedFields;

    @Override
    protected void registerCustomEditors(DataBinder dataBinder) {
        String[] fields = {"name", "wikiName", "path", "provenance", "authority", "wikiDoc", "definitionName", "label"};
        dataBinder.registerCustomEditor(Query.class, "q", new MultiFieldQueryParserEditor(fields));
    }

    @Override
    public Object getObject() {
        return searchFilter;
    }

    @Override
    protected Validator getValidator() {
        return validator;
    }

    @Override
    public String getName() {
        return "searchFilter";
    }

    @Override
    public String[] getAllowedFields() {
        if (allowedFields == null) {
            allowedFields = new HashSet<String>();
            allowedFields.add("q");
        }
        return allowedFields.toArray(new String[]{});
    }

    public SearchFilter getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(SearchFilter searchFilter) {
        this.searchFilter = searchFilter;
    }
}