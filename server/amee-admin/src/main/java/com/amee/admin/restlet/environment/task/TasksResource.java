package com.amee.admin.restlet.environment.task;

import com.amee.admin.restlet.environment.EnvironmentBrowser;
import com.amee.domain.Pager;
import com.amee.domain.ScheduledTask;
import com.amee.restlet.BaseResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.environment.ScheduledTaskManager;
import org.json.JSONArray;
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

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
        setAvailable(isValid());
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
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<ScheduledTask> scheduledTasks = environmentService.getScheduledTasks(environmentBrowser.getEnvironment(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("scheduledTasks", scheduledTasks);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
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
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
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
    public void acceptRepresentation(Representation entity) {
        log.debug("acceptRepresentation");
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
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation");
        if (environmentBrowser.getScheduledTaskActions().isAllowModify()) {
            Form form = getForm();
            if (form.getNames().contains("restart")) {
                scheduledTaskManager.onShutdown(false);
                scheduledTaskManager.onStart();
            } else if (form.getNames().contains("runTask")) {
                String taskUid = form.getFirstValue("runTask");
                ScheduledTask task = environmentService.getScheduledTaskByUid(environmentBrowser.getEnvironment(), taskUid);
                if (task != null) {
                    scheduledTaskManager.run(task);
                }
            }
            success();
        } else {
            notAuthorized();
        }
    }
}