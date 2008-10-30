/**
* This file is part of AMEE.
*
* AMEE is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
*
* AMEE is free software and is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* Created by http://www.dgen.net.
* Website http://www.amee.cc
*/
package gc.carbon.data;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.BaseResource;
import gc.carbon.path.PathItemService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class DataUploadResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    // threshold above which temporary files will go to disk
    public static final int SIZE_THRESHOLD = 100000; // TODO: what should this be?
    // max uploaded file size
    public static final long SIZE_MAX = 800000; // TODO: what should this be?

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataBrowser dataBrowser;

    @Autowired
    private CarbonDataLoader carbonDataLoader;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private PathItemService pathItemService;

    // TODO: Springify
    @Autowired
    private Environment environment;

    FileItemFactory fileItemFactory;

    public DataUploadResource() {
        super();
    }

    public DataUploadResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        dataBrowser.setDataCategoryUid(request.getResourceRef().getQueryAsForm().getFirstValue("categoryUid"));
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(SIZE_THRESHOLD);
        fileItemFactory = diskFileItemFactory;
        setPage(request);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_DATA_UPLOAD;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("environmentPIG", pathItemService.getPathItemGroup(environment));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        return new JSONObject();
    }

    @Override
    public Element getElement(Document document) {
        return document.createElement("DataUploadResource");
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (dataBrowser.isAllowDataUpload()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (dataBrowser.isAllowDataUpload()) {
            boolean carbonDataLoaded = false;
            DataCategory dataCategory = dataBrowser.getDataCategory();
            try {
                RestletFileUpload rfu = new RestletFileUpload(fileItemFactory);
                rfu.setSizeMax(SIZE_MAX);
                List<FileItem> fileItems = rfu.parseRequest(getRequest());
                for (FileItem fileItem : fileItems) {
                    if (fileItem.getFieldName().equalsIgnoreCase("carbonDataFile")) {
                        carbonDataLoaded = carbonDataLoader.loadCarbonDataValues(fileItem, dataCategory);
                    }
                }
            } catch (FileUploadException e) {
                log.error("caught FileUploadException: " + e);
                badRequest();
            }
            if (carbonDataLoaded) {
                pathItemService.removePathItemGroup(dataCategory.getEnvironment());
                dataSheetService.removeSheet(dataCategory);
                success("/data/upload");
            } else {
                log.error("Carbon data load failed");
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }
}
