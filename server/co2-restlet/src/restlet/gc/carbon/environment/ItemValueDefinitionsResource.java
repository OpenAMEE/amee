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
package gc.carbon.environment;

import com.jellymold.utils.BaseResource;
import com.jellymold.kiwi.environment.EnvironmentService;
import gc.carbon.data.DataConstants;
import gc.carbon.definition.DefinitionServiceDAO;
import gc.carbon.domain.ValueDefinition;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.APIVersion;
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
public class ItemValueDefinitionsResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionServiceDAO definitionServiceDAO;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Autowired
    private EnvironmentService environmentService;

    private ItemValueDefinition newItemValueDefinition;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getItemDefinition() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ITEM_VALUE_DEFINITIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        List<ValueDefinition> valueDefinitions = definitionServiceDAO.getValueDefinitions(definitionBrowser.getEnvironment());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", definitionBrowser.getEnvironment());
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("itemValueDefinitions", definitionBrowser.getItemDefinition().getItemValueDefinitions());
        values.put("valueDefinitions", valueDefinitions.isEmpty() ? null : valueDefinitions);
        values.put("apiVersions", environmentService.getAPIVersions());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            obj.put("environment", definitionBrowser.getEnvironment().getIdentityJSONObject());
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
            element.appendChild(definitionBrowser.getEnvironment().getIdentityElement(document));
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
    public void handleGet() {
        log.debug("handleGet()");
        if (definitionBrowser.getItemValueDefinitionActions().isAllowList()) {
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
    public void acceptRepresentation(Representation entity) {
        log.debug("acceptRepresentation()");
        if (definitionBrowser.getItemValueDefinitionActions().isAllowCreate()) {
            Form form = getForm();
            ValueDefinition valueDefinition =
                    definitionServiceDAO.getValueDefinition(definitionBrowser.getEnvironment(), form.getFirstValue("valueDefinitionUid"));
            if ((form.getFirstValue("name") != null) && (valueDefinition != null)) {
                newItemValueDefinition = new ItemValueDefinition(definitionBrowser.getItemDefinition());
                newItemValueDefinition.setValueDefinition(valueDefinition);
                newItemValueDefinition.setName(form.getFirstValue("name"));
                newItemValueDefinition.setPath(form.getFirstValue("path"));
                newItemValueDefinition.setValue(form.getFirstValue("value"));
                newItemValueDefinition.setFromData(Boolean.valueOf(form.getFirstValue("fromData")));
                newItemValueDefinition.setFromProfile(Boolean.valueOf(form.getFirstValue("fromProfile")));
                newItemValueDefinition.setAllowedRoles(form.getFirstValue("allowedRoles"));
                newItemValueDefinition.setUnit(form.getFirstValue("unit"));
                newItemValueDefinition.setPerUnit(form.getFirstValue("perUnit"));
                if (form.getFirstValue("aliasedTo") != null) {
                    newItemValueDefinition.setAliasedTo(definitionServiceDAO.getItemValueDefinition(form.getFirstValue("aliasedTo")));
                }
                // Loop over all known APIVersions and check which have been submitted with the new ItemValueDefinition.
                List<APIVersion> apiVersions = environmentService.getAPIVersions();
                for (APIVersion apiVersion : apiVersions) {
                    String version = form.getFirstValue("apiversion-" + apiVersion.getVersion());
                    if (version != null) {
                        newItemValueDefinition.getAPIVersions().add(apiVersion);
                    }
                }

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
        } else {
            notAuthorized();
        }
    }
}
