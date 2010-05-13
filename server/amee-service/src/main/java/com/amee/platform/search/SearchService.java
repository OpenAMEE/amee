package com.amee.platform.search;

import com.amee.base.transaction.TransactionController;
import com.amee.domain.AMEEEntity;
import com.amee.domain.ObjectType;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.invalidation.InvalidationMessage;
import com.amee.service.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SearchService implements ApplicationListener {

    private final Log log = LogFactory.getLog(getClass());

    public final static Analyzer STANDARD_ANALYZER = new StandardAnalyzer(Version.LUCENE_30);
    public final static Analyzer KEYWORD_ANALYZER = new KeywordAnalyzer();

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
                if (dataCategory != null) {
                    update(dataCategory);
                } else {
                    remove(invalidationMessage.getEntityUid());
                }
                transactionController.end();
            }
        }
    }

    // Index & Document management.

    public void build() {
        build(true, false);
    }

    public void build(boolean indexDataCategories, boolean indexDataItems) {
        try {
            log.debug("build() Building...");
            // Get a LuceneWrapper to use in this thread.
            LuceneIndexWrapper index = new LuceneIndexWrapper();
            // Ensure we have an empty Lucene index.
            index.clearIndex();
            // Get an IndexWriter to work with.
            IndexWriter indexWriter = index.getIndexWriter();
            // Add DataCategories.
            if (indexDataCategories) {
                buildDataCategories(indexWriter);
            }
            // Add DataItems.
            if (indexDataItems) {
                buildDataItems(indexWriter);
            }
            // Ensure IndexWriter is closed.
            index.closeIndexWriter();
            log.debug("build() Building... DONE");
        } catch (IOException e) {
            throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
        }
    }

    /**
     * Add all DataCategories to the index.
     *
     * @param indexWriter
     * @throws IOException
     */
    protected void buildDataCategories(IndexWriter indexWriter) throws IOException {
        transactionController.begin(false);
        for (DataCategory dataCategory :
                dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
            log.debug("buildDataCategories() " + dataCategory.getName());
            indexWriter.addDocument(getDocument(dataCategory));
        }
        transactionController.end();
    }

    /**
     * Add all DataItems to the index.
     *
     * @param indexWriter
     * @throws IOException
     */
    protected void buildDataItems(IndexWriter indexWriter) throws IOException {
        transactionController.begin(false);
        int i = 0;
        for (DataCategory dataCategory :
                dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
            if (dataCategory.getItemDefinition() != null) {
                i++;
                log.debug("buildDataItems() " + dataCategory.getName());
                for (DataItem dataItem : dataService.getDataItems(dataCategory)) {
                    indexWriter.addDocument(getDocument(dataItem));
                }
                if (i > 30) {
                    break;
                }
            }
        }
        transactionController.end();
    }

    /**
     * Update index with a new Document for the DataCategory.
     * <p/>
     * TODO: Also need to update any 'child' Documents. Look up with the UID.
     *
     * @param dataCategory to update index with
     */
    protected void update(DataCategory dataCategory) {
        log.debug("update() DataCategory: " + dataCategory.getUid());
        try {
            LuceneIndexWrapper index = new LuceneIndexWrapper();
            IndexWriter indexWriter = index.getIndexWriter();
            Document document = getDocument(dataCategory);
            indexWriter.updateDocument(new Term("entityUid", dataCategory.getUid()), document, index.getAnalyzer());
            index.closeIndexWriter();
        } catch (IOException e) {
            throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
        }
    }

    /**
     * Removes a document from the index.
     * <p/>
     * TODO: This will need enhancing when we we index more than just DCs. We should be able to use a search.
     *
     * @param uid of document to remove.
     */
    protected void remove(String uid) {
        log.debug("remove() remove: " + uid);
        try {
            LuceneIndexWrapper index = new LuceneIndexWrapper();
            IndexWriter indexWriter = index.getIndexWriter();
            indexWriter.deleteDocuments(new Term("entityUid", uid));
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
            doc.add(new Field("definitionUid", dataCategory.getItemDefinition().getUid(), Field.Store.NO, Field.Index.NOT_ANALYZED));
            doc.add(new Field("definitionName", dataCategory.getItemDefinition().getName(), Field.Store.NO, Field.Index.ANALYZED));
        }
        return doc;
    }

    protected Document getDocument(DataItem dataItem) {
        PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataItem.getEnvironment());
        PathItem pathItem = pathItemGroup.findByUId(dataItem.getDataCategory().getUid());
        Document doc = getDocument((AMEEEntity) dataItem);
        doc.add(new Field("name", dataItem.getName(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("path", dataItem.getPath(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (pathItem != null) {
            doc.add(new Field("fullPath", pathItem.getFullPath() + "/" + dataItem.getDisplayPath(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
        doc.add(new Field("wikiDoc", dataItem.getWikiDoc(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("provenance", dataItem.getProvenance(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("categoryUid", dataItem.getDataCategory().getUid(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.add(new Field("categoryWikiName", dataItem.getDataCategory().getWikiName(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("definitionUid", dataItem.getItemDefinition().getUid(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.add(new Field("definitionName", dataItem.getItemDefinition().getName(), Field.Store.NO, Field.Index.ANALYZED));
        for (Object key : dataItem.getItemValuesMap().keySet()) {
            String path = (String) key;
            ItemValue itemValue = dataItem.getItemValuesMap().get(path);
            doc.add(new Field(path, itemValue.getValue(), Field.Store.NO, Field.Index.ANALYZED));
        }
        doc.add(new Field("label", dataItem.getLabel(), Field.Store.NO, Field.Index.ANALYZED));
        return doc;
    }

    private Document getDocument(AMEEEntity entity) {
        Document doc = new Document();
        doc.add(new Field("entityType", entity.getObjectType().getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("entityId", entity.getId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("entityUid", entity.getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
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
            dataCategoryIds.add(new Long(document.getField("entityId").stringValue()));
        }
        return dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"), dataCategoryIds);
    }

    public List<DataCategory> getDataCategories(Query query) {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("entityType", ObjectType.DC.getName())), BooleanClause.Occur.MUST);
        q.add(query, BooleanClause.Occur.MUST);
        Set<Long> dataCategoryIds = new HashSet<Long>();
        for (Document document : new LuceneIndexWrapper().doSearch(q)) {
            dataCategoryIds.add(new Long(document.getField("entityId").stringValue()));
        }
        return dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"), dataCategoryIds);
    }

    public List<DataItem> getDataItems(Query query) {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("entityType", ObjectType.DI.getName())), BooleanClause.Occur.MUST);
        q.add(query, BooleanClause.Occur.MUST);
        Set<Long> dataItemIds = new HashSet<Long>();
        for (Document document : new LuceneIndexWrapper().doSearch(q)) {
            dataItemIds.add(new Long(document.getField("entityId").stringValue()));
        }
        return dataService.getDataItems(environmentService.getEnvironmentByName("AMEE"), dataItemIds);
    }
}
