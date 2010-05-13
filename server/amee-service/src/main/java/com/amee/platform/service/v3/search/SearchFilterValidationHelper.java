package com.amee.platform.service.v3.search;

import com.amee.base.validation.ValidationHelper;
import com.amee.platform.search.MultiFieldQueryParserEditor;
import com.amee.platform.search.ObjectTypesEditor;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
        String[] fields = {"name", "wikiName", "path", "provenance", "authority", "wikiDoc", "itemDefinitionName", "label"};
        Map<String, Float> boosts = new HashMap<String, Float>();
        boosts.put("wikiName", 10.0f);
        dataBinder.registerCustomEditor(Query.class, "q", new MultiFieldQueryParserEditor(fields, boosts));
        dataBinder.registerCustomEditor(Set.class, "types", new ObjectTypesEditor());
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
            allowedFields.add("types");
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