package com.amee.restlet.data;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.restlet.AMEEResource;
import com.amee.restlet.RequestContext;
import com.amee.service.data.DataBrowser;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DrillDownService;
import com.amee.service.profile.ProfileService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.*;

// TODO - move to builder resource
@Component
@Scope("prototype")
public class DrillDownResource extends AMEEResource implements Serializable {

    @Autowired
    private DrillDownService drillDownService;

    @Autowired
    private DataBrowser dataBrowser;

    private DataCategory dataCategory;
    @Autowired
    protected ProfileService profileService;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
        dataBrowser.setDataCategory(dataCategory);
        (ThreadBeanHolder.get(RequestContext.class)).setDrillDown(dataCategory);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        DataCategory dc = dataCategory;
        while (dc != null) {
            entities.add(dc);
            dc = dc.getDataCategory();
        }
        Collections.reverse(entities);
        return entities;
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (dataCategory != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_DRILL_DOWN;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        ItemDefinition itemDefinition = dataCategory.getItemDefinition();
        Map<String, Object> values = super.getTemplateValues();
        if (itemDefinition != null) {
            List<Choice> selections = getSelections();
            Choices choices = drillDownService.getChoices(dataCategory, selections);
            values.put("selections", selections);
            values.put("choices", choices);
        }
        values.put("browser", dataBrowser);
        values.put("itemDefinition", itemDefinition);
        values.put("dataCategory", dataCategory);
        values.put("node", dataCategory);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        ItemDefinition itemDefinition = dataCategory.getItemDefinition();
        JSONObject obj = new JSONObject();
        obj.put("dataCategory", dataCategory.getIdentityJSONObject());
        if (itemDefinition != null) {
            obj.put("itemDefinition", itemDefinition.getIdentityJSONObject());
            List<Choice> selections = getSelections();
            Choices choices = drillDownService.getChoices(dataCategory, selections);
            JSONArray selectionsJSONArray = new JSONArray();
            for (Choice selection : selections) {
                selectionsJSONArray.put(selection.getJSONObject());
            }
            obj.put("selections", selectionsJSONArray);
            obj.put("choices", choices.getJSONObject());
        }
        obj.put("dataCategory", dataCategory.getJSONObject(true));
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        ItemDefinition itemDefinition = dataCategory.getItemDefinition();
        Element element = document.createElement("DrillDownResource");
        element.appendChild(dataCategory.getIdentityElement(document));
        if (itemDefinition != null) {
            element.appendChild(itemDefinition.getIdentityElement(document));
            List<Choice> selections = getSelections();
            Choices choices = drillDownService.getChoices(dataCategory, selections);
            Element selectionsElement = document.createElement("Selections");
            for (Choice selection : selections) {
                selectionsElement.appendChild(selection.getElement(document));
            }
            element.appendChild(selectionsElement);
            element.appendChild(choices.getElement(document));
        }
        return element;
    }

    public List<Choice> getSelections() {
        List<Choice> selections = new ArrayList<Choice>();
        Form form = getRequest().getResourceRef().getQueryAsForm();
        Set<String> names = form.getNames();
        for (String name : names) {
            selections.add(new Choice(name, form.getFirstValue(name)));
        }
        return selections;
    }
}