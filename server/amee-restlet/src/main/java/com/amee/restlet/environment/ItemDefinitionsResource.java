package com.amee.restlet.environment;

import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.Pager;
import com.amee.domain.data.ItemDefinition;
import com.amee.restlet.AuthorizeResource;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class ItemDefinitionsResource extends AuthorizeResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    private ItemDefinition newItemDefinition;

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
        return DataConstants.VIEW_ITEM_DEFINITIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager();
        List<ItemDefinition> itemDefinitions = definitionService.getItemDefinitions(pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("itemDefinitions", itemDefinitions);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager();
            List<ItemDefinition> itemDefinitions = definitionService.getItemDefinitions(pager);
            pager.setCurrentPage(getPage());
            JSONArray itemDefinitionsJSONArray = new JSONArray();
            for (ItemDefinition itemDefinition : itemDefinitions) {
                itemDefinitionsJSONArray.put(itemDefinition.getJSONObject(false));
            }
            obj.put("itemDefinitions", itemDefinitionsJSONArray);
            obj.put("pager", pager.getJSONObject());
        } else if (getRequest().getMethod().equals(Method.POST)) {
            obj.put("itemDefinition", newItemDefinition.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ItemDefinitionsResource");
        if (isGet()) {
            Pager pager = getPager();
            List<ItemDefinition> itemDefinitions = definitionService.getItemDefinitions(pager);
            pager.setCurrentPage(getPage());
            Element itemDefinitionsElement = document.createElement("ItemDefinitions");
            for (ItemDefinition itemDefinition : itemDefinitions) {
                itemDefinitionsElement.appendChild(itemDefinition.getElement(document, false));
            }
            element.appendChild(itemDefinitionsElement);
            element.appendChild(pager.getElement(document));
        } else if (getRequest().getMethod().equals(Method.POST)) {
            element.appendChild(newItemDefinition.getElement(document));
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
            newItemDefinition = new ItemDefinition(form.getFirstValue("name"));
            definitionService.persist(newItemDefinition);
        }
        if (newItemDefinition != null) {
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