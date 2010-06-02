package com.amee.restlet.environment;

import com.amee.domain.AMEEEntity;
import com.amee.domain.APIVersion;
import com.amee.domain.LocaleConstants;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.data.ReturnValueDefinition;
import com.amee.restlet.AuthorizeResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.locale.LocaleService;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ReturnValueDefinitionResource extends AuthorizeResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LocaleService localeService;

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
        definitionBrowser.setReturnValueDefinitionUid(request.getAttributes().get("returnValueDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getReturnValueDefinition() != null);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getActiveEnvironment());
        entities.add(definitionBrowser.getEnvironment());
        entities.add(definitionBrowser.getItemDefinition());
        entities.add(definitionBrowser.getReturnValueDefinition());
        return entities;
    }

    @Override
    public String getTemplatePath() {

        // Not implemented
        return "";
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        //obj.put("returnValueDefinition", definitionBrowser.getReturnValueDefinition().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ReturnValueDefinitionResource");
        //element.appendChild(definitionBrowser.getReturnValueDefinition().getElement(document));
        return element;
    }

    @Override
    public void doStore(Representation entity) {
        log.debug("doStore()");

        ReturnValueDefinition returnValueDefinition = definitionBrowser.getReturnValueDefinition();
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
                    localeService.clearLocaleName(returnValueDefinition, locale);
                } else {
                    // Update or create.
                    String localeNameStr = form.getFirstValue(name);
                    // Validate - Must have a locale name value.
                    if (StringUtils.isBlank(localeNameStr)) {
                        badRequest(APIFault.INVALID_PARAMETERS);
                        return;
                    }
                    // Do the update or create.
                    localeService.setLocaleName(returnValueDefinition, locale, localeNameStr);
                }
            }
        }

        Set<String> names = form.getNames();
        if (names.contains("unit")) {
            returnValueDefinition.setUnit(form.getFirstValue("unit"));
        }
        if (names.contains("perUnit")) {
            returnValueDefinition.setPerUnit(form.getFirstValue("perUnit"));
        }
        if (names.contains("isDefault")) {
            returnValueDefinition.setDefault(Boolean.getBoolean(form.getFirstValue("isDefault")));
        }

        success();
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        definitionService.remove(definitionBrowser.getReturnValueDefinition());
        success();
    }
}
