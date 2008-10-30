package com.jellymold.kiwi.app;

import com.jellymold.kiwi.Target;
import com.jellymold.kiwi.TargetType;
import com.jellymold.utils.BaseResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Component
@Scope("prototype")
public class TargetResource extends BaseResource {

    @Autowired
    private AppService appService;

    @Autowired
    private AppBrowser appBrowser;

    public TargetResource() {
        super();
    }

    public TargetResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        appBrowser.setAppUid(request.getAttributes().get("appUid").toString());
        appBrowser.setTargetUid(request.getAttributes().get("targetUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (appBrowser.getApp() != null) && (appBrowser.getTarget() != null);
    }

    @Override
    public String getTemplatePath() {
        return KiwiAppConstants.VIEW_TARGET;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", appBrowser);
        values.put("app", appBrowser.getApp());
        values.put("target", appBrowser.getTarget());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("app", appBrowser.getApp().getJSONObject());
        obj.put("target", appBrowser.getTarget().getJSONObject());
        return obj;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (appBrowser.getAppActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("TargetResource");
        element.appendChild(appBrowser.getApp().getElement(document));
        element.appendChild(appBrowser.getTarget().getElement(document));
        return element;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicates
    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (appBrowser.getAppActions().isAllowModify()) {
            Form form = getForm();
            Target target = appBrowser.getTarget();
            // update values
            if (form.getNames().contains("type")) {
                try {
                    target.setType(TargetType.valueOf(form.getFirstValue("type")));
                } catch (IllegalArgumentException e) {
                    // swallow
                }
            }
            if (form.getNames().contains("name")) {
                target.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("description")) {
                target.setDescription(form.getFirstValue("description"));
            }
            if (form.getNames().contains("uriPattern")) {
                target.setUriPattern(form.getFirstValue("uriPattern"));
            }
            if (form.getNames().contains("target")) {
                target.setTarget(form.getFirstValue("target"));
            }
            if (form.getNames().contains("defaultTarget")) {
                target.setDefaultTarget(Boolean.valueOf(form.getFirstValue("defaultTarget")));
            }
            if (form.getNames().contains("enabled")) {
                target.setEnabled(Boolean.valueOf(form.getFirstValue("enabled")));
            }
            success();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    // TODO: do not allow delete affecting logged-in user...
    @Override
    public void delete() {
        if (appBrowser.getAppActions().isAllowModify()) {
            appBrowser.getApp().remove(appBrowser.getTarget());
            appService.remove(appBrowser.getTarget());
            success();
        } else {
            notAuthorized();
        }
    }
}