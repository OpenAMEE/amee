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
import gc.carbon.ValueDefinition;
import gc.carbon.definition.DefinitionService;
import gc.carbon.data.DataConstants;
import gc.carbon.data.ItemDefinition;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Name("itemDefinitionResource")
@Scope(ScopeType.EVENT)
public class ItemDefinitionResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(ItemDefinitionResource.class);

    @In(create = true)
    private DefinitionService definitionService;

    @In(create = true)
    private DefinitionBrowser definitionBrowser;

    public ItemDefinitionResource() {
        super();
    }

    public ItemDefinitionResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getItemDefinitionUid() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ITEM_DEFINITION;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        List<ValueDefinition> valueDefinitions = definitionService.getValueDefinitions(definitionBrowser.getEnvironment());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", definitionBrowser.getEnvironment());
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("valueDefinitions", valueDefinitions.isEmpty() ? null : valueDefinitions);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("itemDefinition", definitionBrowser.getItemDefinition().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ItemDefinitionResource");
        element.appendChild(definitionBrowser.getItemDefinition().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (definitionBrowser.getItemDefinitionActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (definitionBrowser.getItemDefinitionActions().isAllowModify()) {
            ItemDefinition itemDefinition = definitionBrowser.getItemDefinition();
            Form form = getForm();
            Set<String> names = form.getNames();
            if (names.contains("name")) {
                itemDefinition.setName(form.getFirstValue("name"));
            }
            if (names.contains("drillDown")) {
                itemDefinition.setDrillDown(form.getFirstValue("drillDown"));
            }
            success();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void delete() {
        log.debug("delete");
        if (definitionBrowser.getItemDefinitionActions().isAllowDelete()) {
            ItemDefinition itemDefinition = definitionBrowser.getItemDefinition();
            definitionService.remove(itemDefinition);
            success();
        } else {
            notAuthorized();
        }
    }
}