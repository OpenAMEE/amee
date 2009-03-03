package com.amee.restlet.cache;

import com.amee.core.ValueType;
import com.amee.domain.Pager;
import com.amee.domain.cache.CacheHelper;
import com.amee.domain.sheet.Cell;
import com.amee.domain.sheet.Column;
import com.amee.domain.sheet.Row;
import com.amee.domain.sheet.Sheet;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentService;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class CachesResource extends BaseResource implements Serializable {

    @Autowired
    private CacheAdmin cacheAdmin;

    private CacheSort cacheSort;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        initialise(request, response);
    }

    protected void initialise(Request request, Response response) {
        Form form = request.getResourceRef().getQueryAsForm();
        setPage(request);
        cacheSort = cacheAdmin.getCacheSort(request, response, form);
        setAvailable(isValid());
    }

    @Override
    public String getTemplatePath() {
        return "caches.ftl";
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        values.put("pager", pager);
        values.put("cacheSort", cacheSort);
        values.put("sheet", getSheet(cacheSort, getPage(), pager));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        obj.put("sheet", getSheet(cacheSort, getPage(), pager).getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("CachesResource");
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        element.appendChild(getSheet(cacheSort, getPage(), pager).getElement(document, false));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (cacheAdmin.getCacheActions().isAllowList()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    public boolean allowPost() {
        return true;
    }

    public void acceptRepresentation(Representation entity) {
        log.debug("acceptRepresentation");
        if (cacheAdmin.getCacheActions().isAllowModify()) {
            CacheManager cacheManager = (CacheManager) CacheHelper.getInstance().getInternal();
            Form form = getForm();
            if (form.getNames().contains("invalidate")) {
                cacheManager.clearAll();
            }
            success();
        } else {
            notAuthorized();
        }
    }

    protected Sheet getSheet(CacheSort cs, int page, Pager pager) {
        CacheManager cacheManager = (CacheManager) CacheHelper.getInstance().getInternal();
        Sheet sheet = new Sheet();
        sheet.setKey("cacheSheet");
        sheet.setLabel("Cache Sheet");
        Column name = new Column(sheet, "name", "Name");
        Column size = new Column(sheet, "size", "Size");
        Column status = new Column(sheet, "status", "Status");
        Column hits = new Column(sheet, "hits", "Hits");
        Column misses = new Column(sheet, "misses", "Misses");
        Column objectCount = new Column(sheet, "objectCount", "Object count");
        Column timeToIdleSeconds = new Column(sheet, "timeToIdleSeconds", "Time to idle seconds");
        Column timeToLiveSeconds = new Column(sheet, "timeToLiveSeconds", "Time to live seconds");
        Column accuracy = new Column(sheet, "accuracy", "Accuracy");
        Column inMemoryHits = new Column(sheet, "inMemoryHits", "In memory hits");
        Column memoryStoreSize = new Column(sheet, "memoryStoreSize", "Memory store size");
        Column maxElementsInMemory = new Column(sheet, "maxElementsInMemory", "Max elements in memory");
        Column memoryStoreEvictionPolicy = new Column(sheet, "memoryStoreEvictionPolicy", "Memory store eviction policy");
        Column onDiskHits = new Column(sheet, "onDiskHits", "On disk hits");
        Column diskStoreSize = new Column(sheet, "diskStoreSize", "Disk store size");
        Column maxElementsOnDisk = new Column(sheet, "maxElementsOnDisk", "Max elements on disk");
        Column diskExpiryThreadIntervalSeconds = new Column(sheet, "diskExpiryThreadIntervalSeconds", "Disk expiry thread interval seconds");
        for (String cacheName : cacheManager.getCacheNames()) {
            Ehcache cache = cacheManager.getEhcache(cacheName);
            if (cache != null) {
                Row row = new Row(sheet);
                Statistics statistics = cache.getStatistics();
                new Cell(name, row, "<a href=\"/cache/" + cache.getName() + "\">" + cache.getName() + "</a>", ValueType.TEXT);
                new Cell(size, row, "" + cache.getSize(), ValueType.INTEGER);
                new Cell(status, row, cache.getStatus().toString(), ValueType.TEXT);
                new Cell(hits, row, "" + statistics.getCacheHits(), ValueType.INTEGER);
                new Cell(misses, row, "" + statistics.getCacheMisses(), ValueType.INTEGER);
                new Cell(objectCount, row, "" + statistics.getObjectCount(), ValueType.INTEGER);
                new Cell(timeToIdleSeconds, row, "" + cache.getTimeToIdleSeconds(), ValueType.INTEGER);
                new Cell(timeToLiveSeconds, row, "" + cache.getTimeToLiveSeconds(), ValueType.INTEGER);
                new Cell(accuracy, row, "" + statistics.getStatisticsAccuracy(), ValueType.INTEGER);
                new Cell(inMemoryHits, row, "" + statistics.getInMemoryHits(), ValueType.INTEGER);
                new Cell(memoryStoreSize, row, "" + cache.getMemoryStoreSize(), ValueType.INTEGER);
                new Cell(maxElementsInMemory, row, "" + cache.getMaxElementsInMemory(), ValueType.INTEGER);
                new Cell(memoryStoreEvictionPolicy, row, cache.getMemoryStoreEvictionPolicy().toString(), ValueType.INTEGER);
                new Cell(onDiskHits, row, "" + statistics.getOnDiskHits(), ValueType.INTEGER);
                new Cell(diskStoreSize, row, "" + cache.getDiskStoreSize(), ValueType.INTEGER);
                new Cell(maxElementsOnDisk, row, "" + cache.getMaxElementsOnDisk(), ValueType.INTEGER);
                new Cell(diskExpiryThreadIntervalSeconds, row, "" + cache.getDiskExpiryThreadIntervalSeconds(), ValueType.INTEGER);
            }
        }
        sheet.addSortBy(cs.getSortBy());
        Column column = sheet.getColumn(cs.getSortBy());
        if (column != null) {
            column.setSortOrder(cs.getSortOrder());
        }
        sheet.sortRows();
        pager.setCurrentPage(page);
        return Sheet.getCopy(sheet, pager);
    }
}