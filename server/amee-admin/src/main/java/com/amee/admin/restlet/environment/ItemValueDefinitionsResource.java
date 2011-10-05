package com.amee.admin.restlet.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ValueDefinition;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.environment.Environment;
import com.amee.restlet.environment.DefinitionBrowser;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DataService;
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
public class ItemValueDefinitionsResource extends AdminResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataService dataService;

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    private ItemValueDefinition newItemValueDefinition;

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
        return DataConstants.VIEW_ITEM_VALUE_DEFINITIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        List<ValueDefinition> valueDefinitions = definitionService.getValueDefinitions();
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("itemValueDefinitions", definitionBrowser.getItemDefinition().getItemValueDefinitions());
        values.put("valueDefinitions", valueDefinitions.isEmpty() ? null : valueDefinitions);
        values.put("apiVersions", dataService.getAPIVersions());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            obj.put("environment", Environment.ENVIRONMENT.getIdentityJSONObject());
            obj.put("itemDefinition", definitionBrowser.getItemDefinition().getIdentityJSONObject());
            JSONArray itemValueDefinitions = new JSONArray();
            for (ItemValueDefinition itemValueDefinition : definitionBrowser.getItemDefinition().getItemValueDefinitions()) {
                itemValueDefinitions.put(itemValueDefinition.getJSONObject(false));
            }
            obj.put("itemValueDefinitions", itemValueDefinitions);
        } else if (getRequest().getMethod().equals(Method.POST)) {
            obj.put("itemValueDefinition", newItemValueDefinition.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ItemValueDefinitionsResource");
        if (isGet()) {
            element.appendChild(Environment.ENVIRONMENT.getIdentityElement(document));
            element.appendChild(definitionBrowser.getItemDefinition().getIdentityElement(document));
            Element itemValueDefinitionsElement = document.createElement("ItemValueDefinitions");
            for (ItemValueDefinition itemValueDefinition : definitionBrowser.getItemDefinition().getItemValueDefinitions()) {
                itemValueDefinitionsElement.appendChild(itemValueDefinition.getElement(document, false));
            }
            element.appendChild(itemValueDefinitionsElement);
        } else if (getRequest().getMethod().equals(Method.POST)) {
            element.appendChild(newItemValueDefinition.getElement(document));
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
        ValueDefinition valueDefinition =
                definitionService.getValueDefinition(form.getFirstValue("valueDefinitionUid"));
        if ((form.getFirstValue("name") != null) && (valueDefinition != null)) {
            newItemValueDefinition = new ItemValueDefinition(definitionBrowser.getItemDefinition());
            newItemValueDefinition.setValueDefinition(valueDefinition);
            newItemValueDefinition.setName(form.getFirstValue("name"));
            newItemValueDefinition.setPath(form.getFirstValue("path"));
            newItemValueDefinition.setValue(form.getFirstValue("value"));
            newItemValueDefinition.setChoices(form.getFirstValue("choices"));
            newItemValueDefinition.setFromData(Boolean.valueOf(form.getFirstValue("fromData")));
            newItemValueDefinition.setFromProfile(Boolean.valueOf(form.getFirstValue("fromProfile")));
            newItemValueDefinition.setUnit(form.getFirstValue("unit"));
            newItemValueDefinition.setPerUnit(form.getFirstValue("perUnit"));
            if (form.getFirstValue("aliasedTo") != null) {
                newItemValueDefinition.setAliasedTo(definitionService.getItemValueDefinitionByUid(form.getFirstValue("aliasedTo")));
            }
            // Loop over all known APIVersions and check which have been submitted with the new ItemValueDefinition.
            List<APIVersion> apiVersions = dataService.getAPIVersions();
            for (APIVersion apiVersion : apiVersions) {
                String version = form.getFirstValue("apiversion-" + apiVersion.getVersion());
                if (version != null) {
                    newItemValueDefinition.addAPIVersion(apiVersion);
                }
            }
            definitionService.persist(newItemValueDefinition);
        }
        if (newItemValueDefinition != null) {
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
