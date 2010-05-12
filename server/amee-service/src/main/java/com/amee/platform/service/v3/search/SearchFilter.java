package com.amee.platform.service.v3.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SearchFilter implements Serializable {

    public final static Analyzer STANDARD_ANALYZER = new StandardAnalyzer(Version.LUCENE_30);

    private Map<String, Query> queries = new HashMap<String, Query>();

    public SearchFilter() {
        super();
    }

    public Query getQ() {
        return getQueries().get("q");
    }

    public void setQ(Query uid) {
        getQueries().put("q", uid);
    }

    public Map<String, Query> getQueries() {
        return queries;
    }

    public void setQueries(Map<String, Query> queries) {
        this.queries = queries;
    }
}