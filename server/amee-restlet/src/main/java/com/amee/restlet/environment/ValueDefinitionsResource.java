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

import com.amee.core.ValueType;
import com.amee.domain.Pager;
import com.amee.domain.ValueDefinition;
import com.amee.domain.environment.Environment;
import com.amee.restlet.BaseResource;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionServiceDAO;
import com.amee.service.environment.EnvironmentService;
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
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class ValueDefinitionsResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionServiceDAO definitionServiceDAO;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    private Environment environment;
    private ValueDefinition newValueDefinition;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        environment = EnvironmentService.getEnvironment();
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_VALUE_DEFINITIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(environment.getItemsPerPage());
        Environment environment = definitionBrowser.getEnvironment();
        List<ValueDefinition> valueDefinitions = definitionServiceDAO.getValueDefinitions(environment, pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", environment);
        values.put("valueDefinitions", valueDefinitions);
        values.put("valueTypes", ValueType.getChoices());
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            Environment environment = definitionBrowser.getEnvironment();
            List<ValueDefinition> valueDefinitions = definitionServiceDAO.getValueDefinitions(environment, pager);
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
            Pager pager = getPager(environment.getItemsPerPage());
            Environment environment = definitionBrowser.getEnvironment();
            List<ValueDefinition> valueDefinitions = definitionServiceDAO.getValueDefinitions(environment, pager);
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
    public void handleGet() {
        log.debug("handleGet()");
        if (definitionBrowser.getValueDefinitionActions().isAllowList()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");
        if (definitionBrowser.getValueDefinitionActions().isAllowCreate()) {
            Form form = getForm();
            if ((form.getFirstValue("name") != null) && (form.getFirstValue("valueType") != null)) {
                newValueDefinition = new ValueDefinition(
                    definitionBrowser.getEnvironment(),
                    form.getFirstValue("name"),
                    ValueType.valueOf(form.getFirstValue("valueType")));
                definitionServiceDAO.save(newValueDefinition);
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
        } else {
            notAuthorized();
        }
    }
}