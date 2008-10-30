package com.jellymold.kiwi.environment.task;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.ScheduledTask;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.kiwi.environment.ScheduledTaskManager;
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
public class TasksResource extends BaseResource implements Serializable {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private ScheduledTaskManager scheduledTaskManager;

    private ScheduledTask newScheduledTask;

    @Autowired
    private Environment environment;

    public TasksResource() {
        super();
    }

    public TasksResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_TASKS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(environment.getItemsPerPage());
        List<ScheduledTask> ScheduledTasks = environmentService.getScheduledTasks(environmentBrowser.getEnvironment(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("scheduledTasks", ScheduledTasks);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<ScheduledTask> ScheduledTasks = environmentService.getScheduledTasks(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
            JSONArray ScheduledTasksArr = new JSONArray();
            for (ScheduledTask ScheduledTask : ScheduledTasks) {
                ScheduledTasksArr.put(ScheduledTask.getJSONObject());
            }
            obj.put("scheduledTasks", ScheduledTasksArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("scheduledTask", newScheduledTask.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ScheduledTasksResource");
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<ScheduledTask> ScheduledTasks = environmentService.getScheduledTasks(environmentBrowser.getEnvironment(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            Element ScheduledTasksElement = document.createElement("ScheduledTasks");
            for (ScheduledTask ScheduledTask : ScheduledTasks) {
                ScheduledTasksElement.appendChild(ScheduledTask.getElement(document));
            }
            element.appendChild(ScheduledTasksElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newScheduledTask.getElement(document));
        }
        return element;

    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getScheduledTaskActions().isAllowList()) {
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
        if (environmentBrowser.getScheduledTaskActions().isAllowCreate()) {
            Form form = getForm();
            // create new instance if submitted
            if (form.getFirstValue("name") != null) {
                // create new instance
                newScheduledTask = new ScheduledTask(environmentBrowser.getEnvironment());
                newScheduledTask.setName(form.getFirstValue("name"));
                environmentService.save(newScheduledTask);
            }
            if (newScheduledTask != null) {
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

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (environmentBrowser.getScheduledTaskActions().isAllowModify()) {
            Form form = getForm();
            if (form.getNames().contains("restart")) {
                scheduledTaskManager.onShutdown(false);
                scheduledTaskManager.onStart();
            }
            success();
        } else {
            notAuthorized();
        }
    }
}