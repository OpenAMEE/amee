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
import com.amee.domain.AMEEStatus;
import com.amee.domain.APIVersion;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.data.ItemValueDefinitionLocaleName;
import com.amee.domain.data.LocaleName;
import com.amee.restlet.AuthorizeResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
import com.amee.service.environment.EnvironmentService;
import org.apache.commons.lang.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class ItemValueDefinitionResource extends AuthorizeResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

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
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getActiveEnvironment());
        entities.add(definitionBrowser.getEnvironment());
        entities.add(definitionBrowser.getItemDefinition());
        entities.add(definitionBrowser.getItemValueDefinition());
        return entities;
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
        values.put("availableLocales", LocaleName.AVAILABLE_LOCALES.keySet());
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
    public void doStore(Representation entity) {
        log.debug("doStore()");

        ItemValueDefinition itemValueDefinition = definitionBrowser.getItemValueDefinition();
        Form form = getForm();

        // Parse any submitted locale names
        for (String name : form.getNames()) {
            if (name.startsWith("name_")) {

                String locale = name.substring(name.indexOf("_") + 1);
                String localeNameStr = form.getFirstValue(name);

                if (StringUtils.isBlank(localeNameStr) || !LocaleName.AVAILABLE_LOCALES.containsKey(locale)) {
                    badRequest(APIFault.INVALID_PARAMETERS);
                    return;
                }

                if (itemValueDefinition.getLocaleNames().containsKey(locale)) {
                    LocaleName localeName = itemValueDefinition.getLocaleNames().get(locale);
                    localeName.setName(localeNameStr);
                    if (form.getNames().contains("remove_name_" + locale)) {
                        localeName.setStatus(AMEEStatus.TRASH);
                    }
                } else {
                    LocaleName localeName =
                            new ItemValueDefinitionLocaleName(itemValueDefinition, LocaleName.AVAILABLE_LOCALES.get(locale), localeNameStr);
                    itemValueDefinition.addLocaleName(localeName);
                }
            }
        }

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
            itemValueDefinition.setAliasedTo(definitionService.getItemValueDefinitionByUid(form.getFirstValue("aliasedTo")));
        }
        if (names.contains("forceTimeSeries")) {
            itemValueDefinition.setForceTimeSeries(Boolean.valueOf(form.getFirstValue("forceTimeSeries")));
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
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        definitionService.remove(definitionBrowser.getItemValueDefinition());
        success();
    }
}