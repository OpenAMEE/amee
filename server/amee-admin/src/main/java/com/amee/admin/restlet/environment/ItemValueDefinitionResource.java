package com.amee.admin.restlet.environment;

import com.amee.domain.APIVersion;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.LocaleConstants;
import com.amee.domain.LocaleService;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.restlet.environment.DefinitionBrowser;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DataService;
import com.amee.service.definition.DefinitionService;
import org.apache.commons.lang3.StringUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class ItemValueDefinitionResource extends AdminResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LocaleService localeService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
        definitionBrowser.setItemValueDefinitionUid(request.getAttributes().get("itemValueDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getItemValueDefinition() != null);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
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
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("itemValueDefinition", definitionBrowser.getItemValueDefinition());
        values.put("apiVersions", dataService.getAPIVersions());
        values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
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
                // Get locale and locale name to handle.
                String locale = name.substring(name.indexOf("_") + 1);
                // Validate - Must have an available locale.
                if (!LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                    badRequest(APIFault.INVALID_PARAMETERS);
                    return;
                }
                // Remove or Update/Create?
                if (form.getNames().contains("remove_name_" + locale)) {
                    // Remove.
                    localeService.clearLocaleName(itemValueDefinition, locale);
                    itemValueDefinition.onModify();
                } else {
                    // Update or create.
                    String localeNameStr = form.getFirstValue(name);
                    // Validate - Must have a locale name value.
                    if (StringUtils.isBlank(localeNameStr)) {
                        badRequest(APIFault.INVALID_PARAMETERS);
                        return;
                    }
                    // Do the update or create.
                    localeService.setLocaleName(itemValueDefinition, locale, localeNameStr);
                    itemValueDefinition.onModify();
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
        if (names.contains("aliasedTo")) {
            itemValueDefinition.setAliasedTo(definitionService.getItemValueDefinitionByUid(form.getFirstValue("aliasedTo")));
        }

        // Loop over all known APIVersions and check which have been submitted with the new ItemValueDefinition.
        // Remove any versions that have not been sumbitted.
        List<APIVersion> apiVersions = dataService.getAPIVersions();
        for (APIVersion apiVersion : apiVersions) {
            String version = form.getFirstValue("apiversion-" + apiVersion.getVersion());
            if (version != null) {
                itemValueDefinition.addAPIVersion(apiVersion);
            } else {
                itemValueDefinition.removeAPIVersion(apiVersion);
            }
        }
        definitionService.invalidate(itemValueDefinition.getItemDefinition());
        success();
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        definitionService.remove(definitionBrowser.getItemValueDefinition());
        success();
    }
}