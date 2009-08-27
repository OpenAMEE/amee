package com.amee.admin.restlet.environment;

import com.amee.domain.AMEEEntity;
import com.amee.domain.Pager;
import com.amee.domain.environment.Environment;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.environment.EnvironmentConstants;
import com.amee.service.environment.EnvironmentService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class EnvironmentsResource extends AuthorizeResource implements Serializable {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    private Environment newEnvironment;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        setPage(request);
    }

    @Override
    protected List<AMEEEntity> getEntities() {
        return new ArrayList<AMEEEntity>();
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_ENVIRONMENTS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
        List<Environment> environments = environmentService.getEnvironments(pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("environments", environments);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Environment> environments = environmentService.getEnvironments(pager);
            pager.setCurrentPage(getPage());
            JSONArray environmentsArr = new JSONArray();
            for (Environment e : environments) {
                environmentsArr.put(e.getJSONObject());
            }
            obj.put("environments", environmentsArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("environment", newEnvironment.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("EnvironmentsResource");
        if (isGet()) {
            Pager pager = getPager(EnvironmentService.getEnvironment().getItemsPerPage());
            List<Environment> environments = environmentService.getEnvironments(pager);
            pager.setCurrentPage(getPage());
            Element environmentsElement = document.createElement("Environments");
            for (Environment e : environments) {
                environmentsElement.appendChild(e.getElement(document));
            }
            element.appendChild(environmentsElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newEnvironment.getElement(document));
        }
        return element;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void doAccept(Representation entity) {
        log.debug("doAccept");
        Form form = getForm();
        // create new instance if submitted
        if (form.getFirstValue("name") != null) {
            // create new instance
            newEnvironment = new Environment();
            newEnvironment.setName(form.getFirstValue("name"));
            newEnvironment.setPath(form.getFirstValue("path"));
            newEnvironment.setDescription(form.getFirstValue("description"));
            try {
                newEnvironment.setItemsPerPage(new Integer(form.getFirstValue("itemsPerPage")));
            } catch (NumberFormatException e) {
                // swallow
            }
            environmentService.save(newEnvironment);
        }
        if (newEnvironment != null) {
            if (isStandardWebBrowser()) {
                success();
            } else {
                // return a response for API calls
                super.handleGet();
            }
        } else {
            badRequest();
        }
    }
}