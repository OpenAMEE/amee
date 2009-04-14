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

import com.amee.domain.APIVersion;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.restlet.BaseResource;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionServiceDAO;
import com.amee.service.environment.EnvironmentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
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
import java.util.Set;

@Component
@Scope("prototype")
public class ItemValueDefinitionResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionServiceDAO definitionServiceDAO;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
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
        values.put("apiVersions", environmentService.getAPIVersions());
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
        log.debug("handleGet()");
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
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");
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
                itemValueDefinition.setValue(form.getFirstValue("value"));
            }
            if (names.contains("choices")) {
                itemValueDefinition.setChoices(form.getFirstValue("choices"));
            }
            if (names.contains("unit")) {
                itemValueDefinition.setUnit(form.getFirstValue("unit"));
            }
            if (names.contains("perUnit")) {
                itemValueDefinition.setPerUnit(form.getFirstValue("perUnit"));
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
            if (names.contains("aliasedTo")) {
                itemValueDefinition.setAliasedTo(definitionServiceDAO.getItemValueDefinition(form.getFirstValue("aliasedTo")));
            }

            // Loop over all known APIVersions and check which have been submitted with the new ItemValueDefinition.
            // Remove any versions that have not been sumbitted.
            List<APIVersion> apiVersions = environmentService.getAPIVersions();
            for (APIVersion apiVersion : apiVersions) {
                String version = form.getFirstValue("apiversion-" + apiVersion.getVersion());
                if (version != null) {
                    itemValueDefinition.addAPIVersion(apiVersion);
                } else {
                    itemValueDefinition.removeAPIVersion(apiVersion);
                }
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
    public void removeRepresentations() {
        log.debug("removeRepresentations()");
        if (definitionBrowser.getItemDefinitionActions().isAllowModify()) {
            ItemValueDefinition itemValueDefinition = definitionBrowser.getItemValueDefinition();
            definitionBrowser.getItemDefinition().remove(itemValueDefinition);
            definitionServiceDAO.remove(itemValueDefinition);
            success();
        } else {
            notAuthorized();
        }
    }
}
