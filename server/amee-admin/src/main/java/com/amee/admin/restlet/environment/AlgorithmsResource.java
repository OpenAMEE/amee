package com.amee.admin.restlet.environment;

import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.environment.Environment;
import com.amee.restlet.environment.DefinitionBrowser;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class AlgorithmsResource extends AdminResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    private Algorithm newAlgorithm;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getItemDefinition() != null);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        entities.add(definitionBrowser.getItemDefinition());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ALGORITHMS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("algorithms", definitionBrowser.getItemDefinition().getAlgorithms());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            obj.put("environment", Environment.ENVIRONMENT.getIdentityJSONObject());
            obj.put("itemDefinition", definitionBrowser.getItemDefinition().getIdentityJSONObject());
            JSONArray algorithms = new JSONArray();
            for (Algorithm algorithm : definitionBrowser.getItemDefinition().getAlgorithms()) {
                algorithms.put(algorithm.getJSONObject(false));
            }
            obj.put("algorithms", algorithms);
        } else if (getRequest().getMethod().equals(Method.POST)) {
            obj.put("algorithm", newAlgorithm.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("AlgorithmsResource");
        if (isGet()) {
            element.appendChild(Environment.ENVIRONMENT.getIdentityElement(document));
            element.appendChild(definitionBrowser.getItemDefinition().getIdentityElement(document));
            Element algorithmsElement = document.createElement("Algorithms");
            for (Algorithm algorithm : definitionBrowser.getItemDefinition().getAlgorithms()) {
                algorithmsElement.appendChild(algorithm.getElement(document, false));
            }
            element.appendChild(algorithmsElement);
        } else if (getRequest().getMethod().equals(Method.POST)) {
            element.appendChild(newAlgorithm.getElement(document));
        }
        return element;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void doAccept(Representation entity) {
        log.debug("doAccept()");
        Form form = getForm();
        if (form.getFirstValue("name") != null) {
            ItemDefinition itemDefinition = definitionBrowser.getItemDefinition();
            String content = form.getFirstValue("content");
            newAlgorithm = new Algorithm(itemDefinition, content);
            newAlgorithm.setName(form.getFirstValue("name"));
            definitionService.persist(newAlgorithm);
        }
        if (newAlgorithm != null) {
            if (isStandardWebBrowser()) {
                success();
            } else {
                // Return a response for API calls
                super.handleGet();
            }
        } else {
            badRequest();
        }
    }
}
