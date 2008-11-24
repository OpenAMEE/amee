package com.jellymold.kiwi.environment.task;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.ScheduledTask;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.EnvironmentService;
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

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class TaskResource extends BaseResource implements Serializable {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    public TaskResource() {
        super();
    }

    public TaskResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setScheduledTaskUid(request.getAttributes().get("scheduledTaskUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getScheduledTask() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_TASK;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("scheduledTask", environmentBrowser.getScheduledTask());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("scheduledTask", environmentBrowser.getScheduledTask().getJSONObject());
        return obj;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getScheduledTaskActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ScheduledTaskResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getScheduledTask().getElement(document));
        return element;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (environmentBrowser.getScheduledTaskActions().isAllowModify()) {
            Form form = getForm();
            ScheduledTask scheduledTask = environmentBrowser.getScheduledTask();
            // update values
            if (form.getNames().contains("name")) {
                scheduledTask.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("component")) {
                scheduledTask.setComponent(form.getFirstValue("component"));
            }
            if (form.getNames().contains("method")) {
                scheduledTask.setMethod(form.getFirstValue("method"));
            }
            if (form.getNames().contains("cron")) {
                scheduledTask.setCron(form.getFirstValue("cron"));
            }
            if (form.getNames().contains("duration")) {
                scheduledTask.setDuration(form.getFirstValue("duration"));
            }
            if (form.getNames().contains("runOnShutdown")) {
                scheduledTask.setRunOnShutdown(form.getFirstValue("runOnShutdown"));
            }
            if (form.getNames().contains("servers")) {
                scheduledTask.setServers(form.getFirstValue("servers"));
            }
            if (form.getNames().contains("enabled")) {
                scheduledTask.setEnabled(form.getFirstValue("enabled"));
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

    @Override
    public void delete() {
        if (environmentBrowser.getScheduledTaskActions().isAllowDelete()) {
            environmentService.remove(environmentBrowser.getScheduledTask());
            success();
        } else {
            notAuthorized();
        }
    }
}