package com.amee.restlet;

import com.amee.core.ThreadBeanHolder;
import com.amee.domain.*;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.auth.User;
import com.amee.domain.environment.Environment;
import com.amee.domain.sheet.SortOrder;
import com.amee.domain.site.ISite;
import com.amee.restlet.site.FreeMarkerConfigurationService;
import com.amee.restlet.utils.APIFault;
import com.amee.restlet.utils.HeaderUtils;
import com.amee.restlet.utils.MediaTypeUtils;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DocumentImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public abstract class BaseResource extends Resource implements BeanFactoryAware {

    protected final Log log = LogFactory.getLog(getClass());

    private Form form;
    private int page = 1;
    private PagerSetType pagerSetType = PagerSetType.ALL;
    protected BeanFactory beanFactory;

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        initialise(context, request, response);
        setAvailable(isValid());
        setModifiable(isAvailable());
    }

    public void initialise(Context context, Request request, Response response) {
        log.debug("initialise() " + request.getResourceRef().toString());
        List<Variant> variants = super.getVariants();
        if (isStandardWebBrowser()) {
            variants.add(new Variant(MediaType.TEXT_HTML));
        } else {
            variants.add(new Variant(MediaType.APPLICATION_XML));
            variants.add(new Variant(MediaType.APPLICATION_JSON));
        }
    }

    /**
     * Determine if this resource is valid. All subsequent resource activity will assume that this validity
     * check has passed (returned true). Resources should check for existence of objects/entities required
     * for the resource and for consistency of these objects (hierarchy, for example).
     *
     * @return true if this resource is valid
     */
    public boolean isValid() {
        return true;
    }

    @Override
    public void handlePut() {
        try {
            super.handlePut();
        } catch (IllegalArgumentException iae) {
            log.warn("handlePut() " + iae.getMessage());
            badRequest(APIFault.INVALID_PARAMETERS, iae.getMessage());
        } catch (RuntimeException ex) {
            log.error("handlePut()", ex);
            error();
        }
    }

    @Override
    public void handlePost() {
        try {
            super.handlePost();
        } catch (IllegalArgumentException iae) {
            log.warn("handlePost() " + iae.getMessage());
            badRequest(APIFault.INVALID_PARAMETERS, iae.getMessage());
        } catch (RuntimeException ex) {
            log.error("handlePost()", ex);
            error();
        }
    }

    @Override
    public void handleGet() {
        try {
            super.handleGet();
        } catch (IllegalArgumentException iae) {
            log.warn("handleGet() " + iae.getMessage());
            badRequest(APIFault.INVALID_PARAMETERS, iae.getMessage());
        } catch (RuntimeException ex) {
            log.error("handleGet()", ex);
            error();
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {

        if (log.isDebugEnabled()) {
            log.debug("represent() - method: " + getRequest().getMethod() + ", parameters: " + getForm().getMatrixString());
        }

        Representation representation;
        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            representation = getHtmlRepresentation();
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            representation = getJsonRepresentation();
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_XML)) {
            representation = getDomRepresentation();
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_ATOM_XML)) {
            representation = getAtomRepresentation();
        } else {
            representation = super.represent(variant);
        }

        if (representation != null) {
            representation.setCharacterSet(CharacterSet.UTF_8);
            // TODO: need an option for this
            long oneDay = 1000L * 60L * 60L * 24L;
            representation.setExpirationDate(new Date(Calendar.getInstance().getTimeInMillis() - oneDay));
            representation.setModificationDate(Calendar.getInstance().getTime());
        }
        return representation;
    }

    protected Representation getHtmlRepresentation() {
        FreeMarkerConfigurationService freeMarkerConfigurationService =
                (FreeMarkerConfigurationService) beanFactory.getBean("freeMarkerConfigurationService");
        Configuration configuration = freeMarkerConfigurationService.getConfiguration();
        return new TemplateRepresentation(
                getTemplatePath(),
                configuration,
                getTemplateValues(),
                MediaType.TEXT_HTML);
    }

    protected Representation getJsonRepresentation() throws ResourceException {
        try {
            return new JsonRepresentation(getJSONObject());
        } catch (JSONException e) {
            log.error("Caught JSONException: " + e.getMessage());
            // TODO: return an error message as JSON
            return null;
        }
    }

    protected Representation getDomRepresentation() throws ResourceException {
        Document document = new DocumentImpl();
        Element element = document.createElement("Resources");
        element.appendChild(getElement(document));
        document.appendChild(element);
        return new DomRepresentation(MediaType.APPLICATION_XML, document);
    }

    // TODO: Needs to be replaced by getAtomRepresentation in AMEEResource or be merged.
    protected Representation getAtomRepresentation() throws ResourceException {
        final org.apache.abdera.model.Element atomElement = getAtomElement();
        return new WriterRepresentation(MediaType.APPLICATION_ATOM_XML) {
            public void write(Writer writer) throws IOException {
                atomElement.writeTo(writer);
            }
        };
    }

    public abstract String getTemplatePath();

    public Map<String, Object> getTemplateValues() {
        return getBaseTemplateValues();
    }

    public Map<String, Object> getBaseTemplateValues() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("path", getRequest().getResourceRef().getPath());
        // values below are mirrored in EngineStatusFilter
        values.put("activeUser", ThreadBeanHolder.get("activeUser"));
        // add request params
        values.put("Parameters", getRequest().getResourceRef().getQueryAsForm().getValuesMap());
        // add enums
        values.put("SortOrder", getEnumForTemplate(SortOrder.class));
        values.put("ObjectType", getEnumForTemplate(ObjectType.class));
        values.put("AMEEStatus", getEnumForTemplate(AMEEStatus.class));
        // add classes
        values.put("PermissionEntry", getStaticsForTemplate(PermissionEntry.class));
        values.put("AMEEEntityReference", getStaticsForTemplate(AMEEEntityReference.class));
        return values;
    }

    public TemplateHashModel getEnumForTemplate(Class clazz) {
        TemplateHashModel result = null;
        try {
            BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
            TemplateHashModel enumModels = wrapper.getEnumModels();
            result = (TemplateHashModel) enumModels.get(clazz.getName());
        } catch (TemplateModelException e) {
            // swallow
        }
        return result;
    }

    public TemplateHashModel getStaticsForTemplate(Class clazz) {
        TemplateHashModel result = null;
        try {
            BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
            TemplateHashModel staticModels = wrapper.getStaticModels();
            result = (TemplateHashModel) staticModels.get(clazz.getName());
        } catch (TemplateModelException e) {
            // swallow
        }
        return result;
    }

    public String getParentPath() {
        return getParentPath(false);
    }

    public String getParentPath(boolean goHigher) {
        String parentPath = "";
        Reference parentRef = getRequest().getResourceRef().getParentRef();
        if (parentRef != null) {
            if (goHigher) {
                parentRef = parentRef.getParentRef();
                if (parentRef != null) {
                    parentPath = parentRef.getPath();
                }
            } else {
                parentPath = parentRef.getPath();
            }
        }
        if (parentPath.endsWith("/")) {
            parentPath = parentPath.substring(0, parentPath.length() - 1);
        }
        return parentPath;
    }

    public org.apache.abdera.model.Element getAtomElement() throws ResourceException {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
    }

    public JSONObject getJSONObject() throws JSONException, ResourceException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException, ResourceException {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
    }

    public Element getElement(Document document) throws ResourceException {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) throws ResourceException {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
    }

    public int getItemsPerPage() {
        int itemsPerPage = getActiveEnvironment().getItemsPerPage();
        String itemsPerPageStr = getRequest().getResourceRef().getQueryAsForm().getFirstValue("itemsPerPage");
        if (itemsPerPageStr == null) {
            itemsPerPageStr = HeaderUtils.getHeaderFirstValue("ItemsPerPage", getRequest());
        }
        if (itemsPerPageStr != null) {
            itemsPerPage = Integer.parseInt(itemsPerPageStr);
        }
        return itemsPerPage;
    }

    /**
     * Get the current active Environment.
     *
     * @return the current active Environment
     */
    public Environment getActiveEnvironment() {
        return (Environment) getRequest().getAttributes().get("activeEnvironment");
    }

    /**
     * Get the current active ISite.
     *
     * @return the current active ISite
     */
    public ISite getActiveSite() {
        return (ISite) getRequest().getAttributes().get("activeSite");
    }

    /**
     * Get the current active signed-in User.
     *
     * @return the current active signed-in User
     */
    public User getActiveUser() {
        return (User) getRequest().getAttributes().get("activeUser");
    }

    /**
     * Get the current APIVersion for the active user.
     *
     * @return the current APIVersion
     */
    public APIVersion getAPIVersion() {
        return getActiveUser().getAPIVersion();
    }

    public void setPage(Request request) {
        String pageStr = request.getResourceRef().getQueryAsForm().getFirstValue("page");
        if (pageStr != null) {
            try {
                setPage(Integer.decode(pageStr));
            } catch (NumberFormatException e) {
                // swallow
            }
        }
    }

    public void setPagerSetType(Request request) {
        String pagerSetTypeStr = request.getResourceRef().getQueryAsForm().getFirstValue("pagerSetType");
        if (pagerSetTypeStr != null) {
            try {
                pagerSetType = PagerSetType.valueOf(pagerSetTypeStr);
            } catch (IllegalArgumentException e) {
                // swallow
                pagerSetType = PagerSetType.ALL;
            }
        }
    }

    public PagerSetType getPagerSetType() {
        return pagerSetType;
    }

    public Pager getPager() {
        Pager pager = new Pager(0, 0, getPage());
        pager.setPagerSetType(pagerSetType);
        return pager;
    }

    public Pager getPager(int itemsPerPage) {
        Pager pager = new Pager(0, itemsPerPage, getPage());
        pager.setPagerSetType(pagerSetType);
        return pager;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void success() {
        success(null);
    }

    public void success(String redirectUri) {
        if (isStandardWebBrowser()) {
            // redirect for HTML clients (POST-THEN-REDIRECT)
            getResponse().setStatus(Status.REDIRECTION_FOUND);
            if (redirectUri != null) {
                getResponse().setLocationRef(redirectUri);
            } else {
                getResponse().setLocationRef(getRequest().getResourceRef().getBaseRef());
            }
        } else {
            // response code for non HTML clients
            getResponse().setStatus(Status.SUCCESS_OK);
        }
    }

    public void notAuthorized() {
        status(Status.CLIENT_ERROR_FORBIDDEN, null);
    }

    public void notFound() {
        status(Status.CLIENT_ERROR_NOT_FOUND, null);
    }

    public void badRequest() {
        status(Status.CLIENT_ERROR_BAD_REQUEST, null);
    }

    public void conflict(APIFault fault) {
        status(Status.CLIENT_ERROR_CONFLICT, fault);
    }

    public void badRequest(APIFault fault, String detail) {
        status(Status.CLIENT_ERROR_BAD_REQUEST, fault, detail);
    }

    public void badRequest(APIFault fault) {
        status(Status.CLIENT_ERROR_BAD_REQUEST, fault);
    }

    public void error() {
        status(Status.SERVER_ERROR_INTERNAL, null);
    }

    public void deprecated() {
        getResponse().setStatus(Status.CLIENT_ERROR_GONE, "The requested resource has been deprecated");
    }

    public void deprecated(String deprecator) {
        getResponse().setStatus(Status.CLIENT_ERROR_GONE, "The requested resource has been deprecated by " + deprecator);
    }

    private void status(Status status, APIFault fault, String message) {
        log.warn("status() - status code " + status + " message: " + ((fault != null) ? fault.toString() : ""));

        String faultStr = "";

        if (fault != null) {
            faultStr = fault.toString();
        }

        if (message != null) {
            faultStr = faultStr + " " + message;
        }

        RequestContext ctx = (RequestContext) ThreadBeanHolder.get("ctx");
        ctx.setError(faultStr);
        getResponse().setStatus(status, faultStr);
        log.warn(ctx.toString());
    }

    private void status(Status status, APIFault fault) {
        status(status, fault, null);
    }

    public Form getForm() {
        if (form == null) {
            if (getRequest().getEntity().isAvailable()) {
                form = getRequest().getEntityAsForm();
            } else {
                form = getRequest().getResourceRef().getQueryAsForm();
            }
            RequestContext ctx = (RequestContext) ThreadBeanHolder.get("ctx");
            ctx.setForm(form);
        }
        return form;
    }

    public boolean isMultiPartForm() {
        return getRequest().getEntity().getMediaType().isCompatible(MediaType.MULTIPART_FORM_DATA);
    }

    public boolean isStandardWebBrowser() {
        return MediaTypeUtils.isStandardWebBrowser(getRequest());
    }

    public boolean isGet() {
        return getRequest().getMethod().equals(Method.GET);
    }

    public boolean isPost() {
        return getRequest().getMethod().equals(Method.POST);
    }

    public boolean isPut() {
        return getRequest().getMethod().equals(Method.PUT);
    }

    public boolean isPostOrPut() {
        return isPost() || isPut();
    }

    /**
     * Indicates if the response status is an error status (for example 4XX or 500 status codes).
     *
     * @return True if the status is an error status.
     */
    public boolean isError() {
        return getResponse().getStatus().isError();
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}