package com.amee.admin.restlet.environment;

import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.Pager;
import com.amee.domain.ValueDefinition;
import com.amee.domain.ValueType;
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
public class ValueDefinitionsResource extends AdminResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    private ValueDefinition newValueDefinition;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_VALUE_DEFINITIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager();
        List<ValueDefinition> valueDefinitions = definitionService.getValueDefinitions(pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("valueDefinitions", valueDefinitions);
        values.put("valueTypes", ValueType.getChoices());
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager();
            List<ValueDefinition> valueDefinitions = definitionService.getValueDefinitions(pager);
            pager.setCurrentPage(getPage());
            JSONArray valueDefinitionsJSONArray = new JSONArray();
            for (ValueDefinition valueDefinition : valueDefinitions) {
                valueDefinitionsJSONArray.put(valueDefinition.getJSONObject(false));
            }
            obj.put("valueDefinitions", valueDefinitionsJSONArray);
            obj.put("valueTypes", ValueType.getJSONObject());
            obj.put("pager", pager.getJSONObject());
        } else if (getRequest().getMethod().equals(Method.POST)) {
            obj.put("valueDefinition", newValueDefinition.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ValueDefinitionsResource");
        if (isGet()) {
            Pager pager = getPager();
            List<ValueDefinition> valueDefinitions = definitionService.getValueDefinitions(pager);
            pager.setCurrentPage(getPage());
            Element valueDefinitionsElement = document.createElement("ValueDefinitions");
            for (ValueDefinition valueDefinition : valueDefinitions) {
                valueDefinitionsElement.appendChild(valueDefinition.getElement(document, false));
            }
            element.appendChild(valueDefinitionsElement);
            element.appendChild(ValueType.getElement(document));
            element.appendChild(pager.getElement(document));
        } else if (getRequest().getMethod().equals(Method.POST)) {
            element.appendChild(newValueDefinition.getElement(document));
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
        if ((form.getFirstValue("name") != null) && (form.getFirstValue("valueType") != null)) {
            String valueType = form.getFirstValue("valueType");
            valueType = valueType.equalsIgnoreCase("DECIMAL") ? "DOUBLE" : valueType;
            newValueDefinition = new ValueDefinition(
                    form.getFirstValue("name"),
                    ValueType.valueOf(valueType));
            definitionService.persist(newValueDefinition);
        }
        if (newValueDefinition != null) {
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