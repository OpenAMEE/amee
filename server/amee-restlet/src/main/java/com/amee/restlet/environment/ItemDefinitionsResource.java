/**
 * This file is part of AMEE.
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.restlet.environment;

import com.amee.domain.AMEEEntity;
import com.amee.domain.Pager;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.environment.Environment;
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
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getEnvironment() != null);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(definitionBrowser.getEnvironment());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ITEM_DEFINITIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(getItemsPerPage());
        Environment environment = definitionBrowser.getEnvironment();
        List<ItemDefinition> itemDefinitions = definitionService.getItemDefinitions(environment, pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", environment);
        values.put("itemDefinitions", itemDefinitions);
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(getItemsPerPage());
            Environment environment = definitionBrowser.getEnvironment();
            List<ItemDefinition> itemDefinitions = definitionService.getItemDefinitions(environment, pager);
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
            Pager pager = getPager(getItemsPerPage());
            Environment environment = definitionBrowser.getEnvironment();
            List<ItemDefinition> itemDefinitions = definitionService.getItemDefinitions(environment, pager);
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
            newItemDefinition = new ItemDefinition(definitionBrowser.getEnvironment(), form.getFirstValue("name"));
            definitionService.save(newItemDefinition);
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