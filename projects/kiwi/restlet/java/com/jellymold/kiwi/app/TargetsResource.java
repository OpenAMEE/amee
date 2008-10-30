package com.jellymold.kiwi.app;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.Target;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import org.json.JSONArray;
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class TargetsResource extends BaseResource implements Serializable {

    @Autowired
    private AppService appService;

    @Autowired
    private AppBrowser appBrowser;

    @Autowired
    private Environment environment;

    private Target newTarget;

    public TargetsResource() {
        super();
    }

    public TargetsResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        appBrowser.setAppUid(request.getAttributes().get("appUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (appBrowser.getApp() != null);
    }

    @Override
    public String getTemplatePath() {
        return KiwiAppConstants.VIEW_TARGETS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(environment.getItemsPerPage());
        List<Target> targets = appService.getTargets(appBrowser.getApp(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", appBrowser);
        values.put("app", appBrowser.getApp());
        values.put("targets", targets);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<Target> targets = appService.getTargets(appBrowser.getApp(), pager);
            pager.setCurrentPage(getPage());
            obj.put("app", appBrowser.getApp().getJSONObject());
            JSONArray targetsArr = new JSONArray();
            for (Target target : targets) {
                targetsArr.put(target.getJSONObject());
            }
            obj.put("targets", targetsArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("target", newTarget.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("TargetsResource");
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<Target> targets = appService.getTargets(appBrowser.getApp(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(appBrowser.getApp().getElement(document));
            Element targetsElement = document.createElement("Targets");
            for (Target target : targets) {
                targetsElement.appendChild(target.getElement(document));
            }
            element.appendChild(targetsElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newTarget.getElement(document));
        }
        return element;

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
    public boolean allowPost() {
        return true;
    }

    // TODO: prevent duplicate instances
    @Override
    public void post(Representation entity) {
        log.debug("post");
        if (appBrowser.getAppActions().isAllowModify()) {
            Form form = getForm();
            // create new instance if submitted
            if (form.getFirstValue("name") != null) {
                // create new instance
                newTarget = new Target();
                newTarget.setName(form.getFirstValue("name"));
                newTarget.setDescription(form.getFirstValue("description"));
                newTarget.setUriPattern(form.getFirstValue("uriPattern"));
                newTarget.setTarget(form.getFirstValue("target"));
                appBrowser.getApp().add(newTarget);
                appService.save(newTarget);
            }
            if (newTarget != null) {
                if (isStandardWebBrowser()) {
                    success();
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }
}
