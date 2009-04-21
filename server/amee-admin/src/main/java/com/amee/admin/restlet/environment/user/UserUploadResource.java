package com.amee.admin.restlet.environment.user;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.auth.User;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.service.environment.SiteService;
import com.amee.service.environment.UserLoader;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class UserUploadResource extends BaseResource implements Serializable {

    // threshold above which temporary files will go to disk
    public static final int SIZE_THRESHOLD = 100000; // TODO: what should this be?
    // max uploaded file size
    public static final long SIZE_MAX = 800000; // TODO: what should this be?

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private UserLoader userLoader;

    private FileItemFactory fileItemFactory;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(SIZE_THRESHOLD);
        fileItemFactory = diskFileItemFactory;
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER_UPLOAD;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        List<User> users = siteService.getUsers(environmentBrowser.getEnvironment());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("users", users);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        return new JSONObject();
    }

    @Override
    public Element getElement(Document document) {
        return document.createElement("UserUploadResource");
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.isAllowUserUpload()) {
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
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation");
        if (environmentBrowser.isAllowUserUpload()) {
            User cloneUser = null;
            boolean usersLoaded = false;
            try {
                RestletFileUpload rfu = new RestletFileUpload(fileItemFactory);
                rfu.setSizeMax(UserUploadResource.SIZE_MAX);
                List<FileItem> fileItems = rfu.parseRequest(getRequest());
                for (FileItem fileItem : fileItems) {
                    if (fileItem.getFieldName().equalsIgnoreCase("cloneUserUid")) {
                        String cloneUserUid = fileItem.getString();
                        cloneUser =
                                siteService.getUserByUid(
                                        environmentBrowser.getEnvironment(),
                                        cloneUserUid);
                    }
                    if (fileItem.getFieldName().equalsIgnoreCase("userDataFile")) {
                        usersLoaded = userLoader.loadUsers(fileItem, environmentBrowser.getEnvironment(), cloneUser);
                    }
                }
            } catch (FileUploadException e) {
                log.error("caught FileUploadException: " + e);
                badRequest();
            }
            if (usersLoaded) {
                success();
            } else {
                log.error("data load failed");
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }
}