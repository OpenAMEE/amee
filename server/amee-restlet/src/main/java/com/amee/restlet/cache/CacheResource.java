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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.Date;
import java.util.Map;

@Component
@Scope("prototype")
public class CacheResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private CacheAdmin cacheAdmin;

    private String cacheName;
    private CacheSort cacheSort;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        Form form = request.getResourceRef().getQueryAsForm();
        cacheName = request.getAttributes().get("cacheName").toString();
        CacheManager cacheManager = (CacheManager) CacheHelper.getInstance().getInternal();
        cacheAdmin.setCache(cacheManager.getEhcache(cacheName));
        setPage(request);
        cacheSort = cacheAdmin.getCacheSort(request, response, form);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (cacheAdmin.getCache() != null);
    }

    @Override
    public String getTemplatePath() {
        return "cache.ftl";
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        values.put("cache", cacheAdmin.getCache());
        values.put("stats", cacheAdmin.getCache().getStatistics());
        values.put("configuration", cacheAdmin.getCache().getCacheConfiguration());
        values.put("pager", pager);
        values.put("cacheSort", cacheSort);
        values.put("sheet", getSheet(cacheSort, getPage(), pager));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        JSONObject obj = new JSONObject();
        obj.put("sheet", getSheet(cacheSort, getPage(), pager).getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document, boolean detailed) {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        Element element = document.createElement("CacheResource");
        element.appendChild(getSheet(cacheSort, getPage(), pager).getElement(document, false));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (cacheAdmin.getCacheActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    public void acceptRepresentation(Representation entity) {
        log.debug("acceptRepresentation");
        if (cacheAdmin.getCacheActions().isAllowModify()) {
            Form form = getForm();
            if (form.getNames().contains("invalidate")) {
                log.debug("Emptying " + cacheName + " Cache");
                cacheAdmin.getCache().removeAll();
            }
            success();
        } else {
            notAuthorized();
        }
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        if (cacheName != null) {
            this.cacheName = cacheName;
        }
    }

    public void setCacheName(Object cacheName) {
        if ((cacheName != null) && (cacheName instanceof String)) {
            setCacheName((String) cacheName);
        }
    }

    protected Sheet getSheet(CacheSort cs, int page, Pager pager) {
        Sheet sheet = new Sheet();
        sheet.setKey("cacheSheet");
        sheet.setLabel("Cache Sheet");
        Column keyCol = new Column(sheet, "name", "Name");
        Column hitCount = new Column(sheet, "hitCount", "Hit Count");
        Column create = new Column(sheet, "createTime", "Creation Time");
        Column expires = new Column(sheet, "expires", "Expiry Time");
        Column lastAccessed = new Column(sheet, "lastAccessed", "Last Accessed");
        Column lastUpdated = new Column(sheet, "lastUpdated", "Last Updated");
        Column nextToLastAccess = new Column(sheet, "nextToLastAccess", "Next To Last Access");
        Column size = new Column(sheet, "size", "Serialised Size");
        Column tti = new Column(sheet, "tti", "Time To Idle");
        Column ttl = new Column(sheet, "ttl", "Time To Live");
        Ehcache cache = cacheAdmin.getCache();
        int i = 0;
        for (Object key : cache.getKeys()) {
            if (key != null) {
                i++;
                Row row = new Row(sheet);
                net.sf.ehcache.Element e = cache.get(key);
                new Cell(keyCol, row, getKey("" + key, i), ValueType.TEXT);
                if (e != null) {
                    new Cell(hitCount, row, "" + e.getHitCount(), ValueType.INTEGER);
                    new Cell(create, row, new Date(e.getCreationTime()), ValueType.DATE);
                    new Cell(expires, row, new Date(e.getExpirationTime()), ValueType.DATE);
                    new Cell(lastAccessed, row, new Date(e.getLastAccessTime()), ValueType.DATE);
                    new Cell(lastUpdated, row, new Date(e.getLastUpdateTime()), ValueType.DATE);
                    new Cell(nextToLastAccess, row, new Date(e.getNextToLastAccessTime()), ValueType.DATE);
                    new Cell(size, row, "" + e.getSerializedSize(), ValueType.INTEGER);
                    new Cell(tti, row, "" + e.getTimeToIdle(), ValueType.INTEGER);
                    new Cell(ttl, row, "" + e.getTimeToLive(), ValueType.INTEGER);
                } else {
                    new Cell(hitCount, row, "", ValueType.TEXT);
                    new Cell(create, row, "", ValueType.TEXT);
                    new Cell(expires, row, "", ValueType.TEXT);
                    new Cell(lastAccessed, row, "", ValueType.TEXT);
                    new Cell(lastUpdated, row, "", ValueType.TEXT);
                    new Cell(nextToLastAccess, row, "", ValueType.TEXT);
                    new Cell(size, row, "", ValueType.TEXT);
                    new Cell(tti, row, "", ValueType.TEXT);
                    new Cell(ttl, row, "", ValueType.TEXT);
                }
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

    private String getKey(String key, int i) {
        String ret;
        if (key.length() < 35) {
            ret = key;
        } else {
            ret = "<a href=\"#hoverbox_" + i + "_contents\" id=\"hoverbox_" + i + "\">" + StringEscapeUtils.escapeHtml(key.substring(0, 32)) + "...</a>\n";
            ret += "<div id=\"hoverbox_" + i + "_contents\">" + StringEscapeUtils.escapeHtml(key) + "</div>\n";
            ret += "<script>" +
                    "\tnew Control.Modal('hoverbox_" + i + "',{\n" +
                    "\thover: true,\n" +
                    "\tposition: 'relative',\n" +
                    "\toffsetLeft: 100\n" +
                    "\t});" +
                    "</script>\n";
        }
        return ret;
    }
}