package com.amee.platform.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

import java.beans.PropertyEditorSupport;

public class MultiFieldQueryParserEditor extends PropertyEditorSupport {

    private String[] fields;
    private Analyzer analyzer;

    public MultiFieldQueryParserEditor(String[] fields) {
        setFields(fields);
        setAnalyzer(SearchService.STANDARD_ANALYZER);
    }

    public MultiFieldQueryParserEditor(String[] fields, Analyzer analyzer) {
        setFields(fields);
        setAnalyzer(analyzer);
    }

    @Override
    public void setAsText(String text) {
        if (text != null) {
            try {
                QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, getFields(), getAnalyzer());
                setValue(parser.parse(text));
            } catch (ParseException e) {
                throw new IllegalArgumentException("Cannot parse query (" + e.getMessage() + ").", e);
            }
        } else {
            setValue(null);
        }
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }
}