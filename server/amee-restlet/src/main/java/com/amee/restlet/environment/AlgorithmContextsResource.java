package com.amee.restlet.environment;

import com.amee.domain.AMEEEntity;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.algorithm.AlgorithmContext;
import com.amee.domain.environment.Environment;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Method;
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
public class AlgorithmContextsResource extends AuthorizeResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    private AlgorithmContext newAlgorithmContext;

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ALGORITHM_CONTEXTS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("algorithmContexts", definitionBrowser.getAlgorithmContexts());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            obj.put("environment", Environment.ENVIRONMENT.getIdentityJSONObject());
            JSONArray algorithmContexts = new JSONArray();
            for (AlgorithmContext algorithmContext : definitionBrowser.getAlgorithmContexts()) {
                algorithmContexts.put(algorithmContext.getJSONObject(false));
            }
            obj.put("algorithms", algorithmContexts);
        } else if (getRequest().getMethod().equals(Method.POST)) {
            obj.put("algorithm", newAlgorithmContext.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("AlgorithmContextsResource");
        if (isGet()) {
            element.appendChild(Environment.ENVIRONMENT.getIdentityElement(document));
            Element algorithmContextsElement = document.createElement("AlgorithmContexts");
            for (AlgorithmContext algorithmContext : definitionBrowser.getAlgorithmContexts()) {
                algorithmContextsElement.appendChild(algorithmContext.getElement(document, false));
            }
            element.appendChild(algorithmContextsElement);
        } else if (getRequest().getMethod().equals(Method.POST)) {
            element.appendChild(newAlgorithmContext.getElement(document));
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
        if (form.getFirstValue("name") != null) {
            newAlgorithmContext = new AlgorithmContext();
            newAlgorithmContext.setName(form.getFirstValue("name"));
            newAlgorithmContext.setContent(form.getFirstValue("content"));
        }
        if (newAlgorithmContext != null) {
            if (isStandardWebBrowser()) {
                definitionService.persist(newAlgorithmContext);
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
