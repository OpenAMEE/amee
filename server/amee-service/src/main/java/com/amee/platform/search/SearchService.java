package com.amee.platform.search;

import com.amee.base.resource.RequestWrapper;
import com.amee.domain.data.DataCategory;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SearchService {

    private final Log log = LogFactory.getLog(getClass());

    public final static String[] DATA_CATEGORY_FIELDS_ARR = {
            "uid",
            "name",
            "wikiName",
            "path",
            "fullPath",
            "parentWikiName",
            "wikiDoc",
            "provenance",
            "authority",
            "parentUid",
            "parentWikiName",
            "itemDefinitionUid",
            "itemDefinitionName"};
    public final static Set<String> DATA_CATEGORY_FIELDS = new HashSet<String>(Arrays.asList(DATA_CATEGORY_FIELDS_ARR));

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    public void buildSearchIndex() {
        try {
            log.debug("buildSearchIndex() Building...");
            // Get a LuceneWrapper to use in this thread.
            LuceneIndexWrapper index = new LuceneIndexWrapper();
            // Ensure we have an empty Lucene index.
            index.clearIndex();
            // Get an IndexWriter to work with.
            IndexWriter indexWriter = index.getIndexWriter();
            // Add all DataCategories to the index.
            for (DataCategory dataCategory :
                    dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
                indexWriter.addDocument(getDocument(dataCategory));
            }
            // Ensure IndexWriter is closed.
            index.closeIndexWriter();
            log.debug("buildSearchIndex() Building... DONE");
        } catch (IOException e) {
            throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
        }
    }

    // TODO: Hook this into DataCategory invalidation message.
    protected void updateIndexEntry(DataCategory dataCategory) {
        log.debug("updateIndexEntry() DataCategory: " + dataCategory.getUid());
        try {
            LuceneIndexWrapper index = new LuceneIndexWrapper();
            IndexWriter indexWriter = index.getIndexWriter();
            Document document = getDocument(dataCategory);
            indexWriter.updateDocument(new Term("uid", dataCategory.getUid()), document, index.getAnalyzer());
            index.closeIndexWriter();
        } catch (IOException e) {
            throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
        }
    }

    protected Document getDocument(DataCategory dataCategory) {
        PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataCategory.getEnvironment());
        PathItem pathItem = pathItemGroup.findByUId(dataCategory.getUid());
        Document doc = new Document();
        doc.add(new Field("type", dataCategory.getObjectType().getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("id", dataCategory.getId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("uid", dataCategory.getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("name", dataCategory.getName(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("path", dataCategory.getPath(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (pathItem != null) {
            doc.add(new Field("fullPath", pathItem.getFullPath(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
        doc.add(new Field("wikiName", dataCategory.getWikiName(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.add(new Field("wikiDoc", dataCategory.getWikiDoc(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("provenance", dataCategory.getProvenance(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("authority", dataCategory.getAuthority(), Field.Store.NO, Field.Index.ANALYZED));
        if (dataCategory.getDataCategory() != null) {
            doc.add(new Field("parentUid", dataCategory.getDataCategory().getUid(), Field.Store.NO, Field.Index.NOT_ANALYZED));
            doc.add(new Field("parentWikiName", dataCategory.getDataCategory().getWikiName(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
        if (dataCategory.getItemDefinition() != null) {
            doc.add(new Field("itemDefinitionUid", dataCategory.getItemDefinition().getUid(), Field.Store.NO, Field.Index.NOT_ANALYZED));
            doc.add(new Field("itemDefinitionName", dataCategory.getItemDefinition().getName(), Field.Store.NO, Field.Index.ANALYZED));
        }
        return doc;
    }

    // DataCategory Search.

    public List<DataCategory> getDataCategories(RequestWrapper requestWrapper) {
        // First attempt - Filter based on an allowed query parameter.
        for (String field : DATA_CATEGORY_FIELDS) {
            if (requestWrapper.getQueryParameters().containsKey(field)) {
                return getDataCategories(field, requestWrapper.getQueryParameters().get(field));
            }
        }
        // Second attempt - Just get a simple list of Data Categories.
        return dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"));
    }

    public List<DataCategory> getDataCategories(String key, String value) {
        Set<Long> dataCategoryIds = new HashSet<Long>();
        for (Document document : new LuceneIndexWrapper().doSearch(key, value)) {
            dataCategoryIds.add(new Long(document.getField("id").stringValue()));
        }
        return dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"), dataCategoryIds);
    }
}
