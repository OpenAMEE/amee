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

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import gc.carbon.data.DataConstants;
import gc.carbon.definition.DefinitionService;
import gc.carbon.domain.data.ItemDefinition;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Name("itemDefinitionsResource")
@Scope(ScopeType.EVENT)
public class ItemDefinitionsResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(ItemDefinitionsResource.class);

    @In(create = true)
    private DefinitionService definitionService;

    @In(create = true)
    private DefinitionBrowser definitionBrowser;

    @In
    private Environment environment;

    private ItemDefinition newItemDefinition;

    public ItemDefinitionsResource() {
        super();
    }

    public ItemDefinitionsResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ITEM_DEFINITIONS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(environment.getItemsPerPage());
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
            Pager pager = getPager(environment.getItemsPerPage());
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
            Pager pager = getPager(environment.getItemsPerPage());
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
    public void handleGet() {
        log.debug("handleGet");
        if (definitionBrowser.getItemDefinitionActions().isAllowList()) {
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
    public void post(Representation entity) {
        log.debug("post");
        if (definitionBrowser.getItemDefinitionActions().isAllowCreate()) {
            Form form = getForm();
            if (form.getFirstValue("name") != null) {
                newItemDefinition = new ItemDefinition(definitionBrowser.getEnvironment(), form.getFirstValue("name"));
                definitionService.save(newItemDefinition);
            }
            if (newItemDefinition != null) {
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