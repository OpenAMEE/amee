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
import gc.carbon.data.DataConstants;
import gc.carbon.data.ItemValueDefinition;
import gc.carbon.definition.DefinitionService;
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
import java.util.Map;
import java.util.Set;

// TODO: Add ValueDefinition choice.

@Name("itemValueDefinitionResource")
@Scope(ScopeType.EVENT)
public class ItemValueDefinitionResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(ItemValueDefinitionResource.class);

    @In(create = true)
    private DefinitionService definitionService;

    @In(create = true)
    private DefinitionBrowser definitionBrowser;

    public ItemValueDefinitionResource() {
        super();
    }

    public ItemValueDefinitionResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
        definitionBrowser.setItemValueDefinitionUid(request.getAttributes().get("itemValueDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getItemValueDefinition() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ITEM_VALUE_DEFINITION;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", definitionBrowser.getEnvironment());
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("itemValueDefinition", definitionBrowser.getItemValueDefinition());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("itemValueDefinition", definitionBrowser.getItemValueDefinition().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ItemValueDefinitionResource");
        element.appendChild(definitionBrowser.getItemValueDefinition().getElement(document));
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
            ItemValueDefinition itemValueDefinition = definitionBrowser.getItemValueDefinition();
            Form form = getForm();
            Set<String> names = form.getNames();
            if (names.contains("name")) {
                itemValueDefinition.setName(form.getFirstValue("name"));
            }
            if (names.contains("path")) {
                itemValueDefinition.setPath(form.getFirstValue("path"));
            }
            if (names.contains("value")) {
                // TODO: validation based on ValueType - pass to ValueType?
                itemValueDefinition.setValue(form.getFirstValue("value"));
            }
            if (names.contains("choices")) {
                itemValueDefinition.setChoices(form.getFirstValue("choices"));
            }
            if (names.contains("fromProfile")) {
                itemValueDefinition.setFromProfile(Boolean.valueOf(form.getFirstValue("fromProfile")));
            }
            if (names.contains("fromData")) {
                itemValueDefinition.setFromData(Boolean.valueOf(form.getFirstValue("fromData")));
            }
            if (names.contains("allowedRoles")) {
                itemValueDefinition.setAllowedRoles(form.getFirstValue("allowedRoles"));
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
        if (definitionBrowser.getItemDefinitionActions().isAllowModify()) {
            ItemValueDefinition itemValueDefinition = definitionBrowser.getItemValueDefinition();
            definitionBrowser.getItemDefinition().remove(itemValueDefinition);
            definitionService.remove(itemValueDefinition);
            success();
        } else {
            notAuthorized();
        }
    }
}
