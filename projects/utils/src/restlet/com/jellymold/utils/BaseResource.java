package com.jellymold.utils;

import com.jellymold.utils.domain.APIObject;
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
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.context.ApplicationContext;

import java.util.*;

public abstract class BaseResource extends ComponentResource implements APIObject {

    protected final Log log = LogFactory.getLog(getClass());

    private Form form;
    private int page = 1;
    private PagerSetType pagerSetType = PagerSetType.ALL;

    public BaseResource() {
        super();
    }

    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        List<Variant> varients = super.getVariants();
        if (isStandardWebBrowser()) {
            varients.add(new Variant(MediaType.TEXT_HTML));
        } else {
            varients.add(new Variant(MediaType.APPLICATION_XML));
            varients.add(new Variant(MediaType.APPLICATION_JSON));
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation;
        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            representation = getHtmlRepresentation();
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            representation = getJsonRepresentation();
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_XML)) {
            representation = getDomRepresentation();
        } else {
            representation = super.represent(variant);
        }
        if (representation != null) {
            // TODO: not sure why we're forcing this
            representation.setCharacterSet(CharacterSet.UTF_8);
            // TODO: need an option for this
            long oneDay = 1000L * 60L * 60L * 24L;
            representation.setExpirationDate(new Date(Calendar.getInstance().getTimeInMillis() - oneDay));
            representation.setModificationDate(Calendar.getInstance().getTime());
        }
        return representation;
    }

    protected Representation getHtmlRepresentation() {
        Configuration configuration = (Configuration)
                getRequest().getAttributes().get("freeMarkerConfiguration");
        return new TemplateRepresentation(
                getTemplatePath(),
                configuration,
                getTemplateValues(),
                MediaType.TEXT_HTML);
    }

    protected Representation getJsonRepresentation() {
        try {
            return new JsonRepresentation(getJSONObject());
        } catch (JSONException e) {
            log.error("Caught JSONException: " + e.getMessage());
            // TODO: return an error message as JSON
            return null;
        }
    }

    protected Representation getDomRepresentation() {
        Document document = new DocumentImpl();
        Element element = document.createElement("Resources");
        element.appendChild(getElement(document));
        document.appendChild(element);
        return new DomRepresentation(MediaType.APPLICATION_XML, document);
    }

    public abstract String getTemplatePath();

    public Map<String, Object> getTemplateValues() {
        return getBaseTemplateValues();
    }

    public Map<String, Object> getBaseTemplateValues() {
        ApplicationContext springContext = (ApplicationContext) ThreadBeanHolder.get("springContext");
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("path", getRequest().getResourceRef().getPath());
        // values below are mirrored in SkinRenderResource and EngineStatusFilter
        values.put("authService", springContext.getBean("authService"));
        values.put("activeUser", ThreadBeanHolder.get("user"));
        values.put("activeGroup", ThreadBeanHolder.get("group"));
        values.put("activeSite", ThreadBeanHolder.get("site"));
        values.put("activeApp", ThreadBeanHolder.get("app"));
        values.put("activeSiteApp", ThreadBeanHolder.get("siteApp"));
        // add enums
        values.put("SortOrder", getEnumForTemplate(SortOrder.class));
        // add request params
        values.put("Parameters", getRequest().getResourceRef().getQueryAsForm().getValuesMap());
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

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return new JSONObject();
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return new JSONObject();
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        return document.createElement("Resource");
    }

    public Element getIdentityElement(Document document) {
        return document.createElement("Resource");
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
        if (MediaTypeUtils.isStandardWebBrowser(getRequest())) {
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
        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void notFound() {
        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void badRequest() {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void badRequest(String msg) {
        log.debug("badRequest() - " + msg);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
    }

    public void error() {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    public Form getForm() {
        if (form == null) {
            if (getRequest().getEntity().isAvailable()) {
                form = getRequest().getEntityAsForm();
            } else {
                form = getRequest().getResourceRef().getQueryAsForm();
            }
        }
        return form;
    }

    public boolean isMultiPartForm() {
        // return getRequest().getEntity().getMediaType().toString().startsWith(MediaType.MULTIPART_FORM_DATA.toString());
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
}