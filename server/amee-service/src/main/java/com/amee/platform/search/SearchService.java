package com.amee.platform.search;

import com.amee.base.transaction.TransactionController;
import com.amee.domain.AMEEEntity;
import com.amee.domain.ObjectType;
import com.amee.domain.data.DataCategory;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.invalidation.InvalidationMessage;
import com.amee.service.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SearchService implements ApplicationListener {

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
    private TransactionController transactionController;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    // Events

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof InvalidationMessage) {
            log.debug("onApplicationEvent() Handling InvalidationMessage.");
            InvalidationMessage invalidationMessage = (InvalidationMessage) event;
            if (invalidationMessage.getObjectType().equals(ObjectType.DC)) {
                transactionController.begin(false);
                DataCategory dataCategory = dataService.getDataCategoryByUid(invalidationMessage.getEntityUid(), true);
                update(dataCategory);
                transactionController.end();
            }
        }
    }

    // Index & Document management.

    public void build() {
        try {
            log.debug("build() Building...");
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
            log.debug("build() Building... DONE");
        } catch (IOException e) {
            throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
        }
    }

    protected void update(DataCategory dataCategory) {
        log.debug("update() DataCategory: " + dataCategory.getUid());
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
        Document doc = getDocument((AMEEEntity) dataCategory);
        doc.add(new Field("name", dataCategory.getName(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("path", dataCategory.getPath(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (pathItem != null) {
            doc.add(new Field("fullPath", pathItem.getFullPath(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
        doc.add(new Field("wikiName", dataCategory.getWikiName(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("wikiDoc", dataCategory.getWikiDoc(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("provenance", dataCategory.getProvenance(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("authority", dataCategory.getAuthority(), Field.Store.NO, Field.Index.ANALYZED));
        if (dataCategory.getDataCategory() != null) {
            doc.add(new Field("parentUid", dataCategory.getDataCategory().getUid(), Field.Store.NO, Field.Index.NOT_ANALYZED));
            doc.add(new Field("parentWikiName", dataCategory.getDataCategory().getWikiName(), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (dataCategory.getItemDefinition() != null) {
            doc.add(new Field("itemDefinitionUid", dataCategory.getItemDefinition().getUid(), Field.Store.NO, Field.Index.NOT_ANALYZED));
            doc.add(new Field("itemDefinitionName", dataCategory.getItemDefinition().getName(), Field.Store.NO, Field.Index.ANALYZED));
        }
        return doc;
    }

    private Document getDocument(AMEEEntity entity) {
        Document doc = new Document();
        doc.add(new Field("type", entity.getObjectType().getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("id", entity.getId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("uid", entity.getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }

    // DataCategory Search.

    public List<DataCategory> getDataCategories(DataCategoryFilter filter) {
        // Filter based on an allowed query parameter.
        if (!filter.getQueries().isEmpty()) {
            BooleanQuery query = new BooleanQuery();
            for (Query q : filter.getQueries().values()) {
                query.add(q, BooleanClause.Occur.MUST);
            }
            return getDataCategories(query);
        } else {
            // Just get a simple list of Data Categories.
            return dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"));
        }
    }

    public List<DataCategory> getDataCategories(String key, String value) {
        Set<Long> dataCategoryIds = new HashSet<Long>();
        for (Document document : new LuceneIndexWrapper().doSearch(key, value)) {
            dataCategoryIds.add(new Long(document.getField("id").stringValue()));
        }
        return dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"), dataCategoryIds);
    }

    public List<DataCategory> getDataCategories(Query query) {
        Set<Long> dataCategoryIds = new HashSet<Long>();
        for (Document document : new LuceneIndexWrapper().doSearch(query)) {
            dataCategoryIds.add(new Long(document.getField("id").stringValue()));
        }
        return dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"), dataCategoryIds);
    }
}
