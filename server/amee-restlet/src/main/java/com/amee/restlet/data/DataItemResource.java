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
package com.amee.restlet.data;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.base.utils.XMLUtils;
import com.amee.calculation.service.CalculationService;
import com.amee.domain.AMEEEntity;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.platform.science.*;
import com.amee.restlet.AMEEResource;
import com.amee.restlet.RequestContext;
import com.amee.service.data.DataBrowser;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DataService;
import com.amee.service.data.DataSheetService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
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

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO - move to builder model

@Component
@Scope("prototype")
public class DataItemResource extends AMEEResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataService dataService;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private DataBrowser dataBrowser;

    private DataCategory dataCategory;
    private DataItem dataItem;
    private String unit;
    private String perUnit;
    private List<Choice> parameters;

    @Override
    public void initialise(Context context, Request request, Response response) {

        super.initialise(context, request, response);

        // Obtain DataCategory.
        dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
        dataBrowser.setDataCategory(dataCategory);
        ((RequestContext) ThreadBeanHolder.get("ctx")).setDataCategory(dataCategory);

        // Obtain DataItem.
        dataItem = dataService.getDataItem(request.getAttributes().get("itemPath").toString());
        ((RequestContext) ThreadBeanHolder.get("ctx")).setDataItem(dataItem);

        // Must have a DataItem to do anything here.
        if (dataItem != null) {

            // We'll pre-process query parameters here to keep the Data API calculation parameters sane.
            Form query = request.getResourceRef().getQueryAsForm();
            unit = query.getFirstValue("returnUnit");
            perUnit = query.getFirstValue("returnPerUnit");

            // The resource may receive a startDate parameter that sets the current date in an
            // historical sequence of ItemValues.
            if (StringUtils.isNotBlank(query.getFirstValue("startDate"))) {
                dataItem.setEffectiveStartDate(new StartEndDate(query.getFirstValue("startDate")));
            }

            // Query parameter names, minus startDate, returnUnit and returnPerUnit.
            Set<String> names = query.getNames();
            names.remove("startDate");
            names.remove("returnUnit");
            names.remove("returnPerUnit");

            // Pull out any values submitted for Data API calculations.
            parameters = new ArrayList<Choice>();
            for (String key : names) {
                parameters.add(new Choice(key, query.getValues(key)));
            }
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (dataCategory != null) &&
                (dataItem != null) &&
                dataItem.getDataCategory().equals(dataCategory) &&
                !dataItem.isTrash();
    }

    @Override
    public List<AMEEEntity> getEntities() {
        return dataItem.getHierarchy();
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + DataConstants.VIEW_DATA_ITEM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Choices userValueChoices = dataSheetService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        Amount amount = calculationService.calculate(dataItem, userValueChoices, getAPIVersion()).defaultValueAsAmount();
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new AmountUnit(SI.KILOGRAM), new AmountPerUnit(NonSI.MONTH));
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataItem", dataItem);
        values.put("node", dataItem);
        values.put("userValueChoices", userValueChoices);
        values.put("amountPerMonth", amount.convert(kgPerMonth).getValue());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        Choices userValueChoices = dataSheetService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        ReturnValues returnAmounts = calculationService.calculate(dataItem, userValueChoices, getAPIVersion());
        Amount amount = returnAmounts.defaultValueAsAmount();
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new AmountUnit(SI.KILOGRAM), new AmountPerUnit(NonSI.MONTH));
        JSONObject obj = new JSONObject();
        obj.put("dataItem", dataItem.getJSONObject(true, false));
        obj.put("path", dataItem.getFullPath());
        obj.put("userValueChoices", userValueChoices.getJSONObject());
        obj.put("amountPerMonth", amount.convert(kgPerMonth).getValue());
        if (getAPIVersion().isNotVersionOne()) {
            CO2AmountUnit returnUnit = new CO2AmountUnit(unit, perUnit);
            JSONObject amountObj = new JSONObject();
            amountObj.put("value", amount.convert(returnUnit).getValue());
            amountObj.put("unit", returnUnit);
            obj.put("amount", amountObj);

            // Multiple return values
            JSONObject amounts = new JSONObject();

            // Create an array of amount objects
            JSONArray amountArray = new JSONArray();
            for (Map.Entry<String, ReturnValue> entry : returnAmounts.getReturnValues().entrySet()) {

                // Create an Amount object
                JSONObject multiAmountObj = new JSONObject();
                multiAmountObj.put("value", entry.getValue().getValue());
                multiAmountObj.put("type", entry.getKey());
                multiAmountObj.put("unit", entry.getValue().getUnit());
                multiAmountObj.put("perUnit", entry.getValue().getPerUnit());
                if (entry.getKey().equals(returnAmounts.getDefaultType())) {
                    multiAmountObj.put("default", "true");
                }

                // Add the object to the amounts array
                amountArray.put(multiAmountObj);
            }

            // Add the amount array to the amounts object.
            amounts.put("amount", amountArray);

            // Create an array of note objects
            JSONArray noteArray = new JSONArray();
            for (Note note : returnAmounts.getNotes()) {
                JSONObject noteObj = new JSONObject();
                noteObj.put("type", note.getType());
                noteObj.put("value", note.getValue());
                amounts.put("note", noteObj);

                // Add the note object to the notes array
                noteArray.put(noteObj);
            }

            // Add the notes array to the amounts object.
            if (noteArray.length() > 0) {
                amounts.put("note", noteArray);
            }

            obj.put("amounts", amounts);
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Choices userValueChoices = dataSheetService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        ReturnValues returnAmounts = calculationService.calculate(dataItem, userValueChoices, getAPIVersion());
        Amount amount = returnAmounts.defaultValueAsAmount();
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new AmountUnit(SI.KILOGRAM), new AmountPerUnit(NonSI.MONTH));
        Element element = document.createElement("DataItemResource");
        element.appendChild(dataItem.getElement(document, true, false));
        element.appendChild(XMLUtils.getElement(document, "Path", dataItem.getFullPath()));
        element.appendChild(userValueChoices.getElement(document));
        element.appendChild(XMLUtils.getElement(document, "AmountPerMonth", amount.convert(kgPerMonth).toString()));
        if (getAPIVersion().isNotVersionOne()) {
            CO2AmountUnit returnUnit = new CO2AmountUnit(unit, perUnit);
            Element amountElem = document.createElement("Amount");
            amountElem.setTextContent(amount.convert(returnUnit).toString());
            amountElem.setAttribute("unit", returnUnit.toString());
            element.appendChild(amountElem);

            // Multiple return values
            Element amounts = document.createElement("Amounts");
            for (Map.Entry<String, ReturnValue> entry : returnAmounts.getReturnValues().entrySet()) {
                Element multiAmount = document.createElement("Amount");
                multiAmount.setAttribute("type", entry.getKey());
                multiAmount.setAttribute("unit", entry.getValue().getUnit());
                multiAmount.setAttribute("perUnit", entry.getValue().getPerUnit());
                if (entry.getKey().equals(returnAmounts.getDefaultType())) {
                    multiAmount.setAttribute("default", "true");
                }
                multiAmount.setTextContent(entry.getValue().getValue() + "");
                amounts.appendChild(multiAmount);
            }
            for (Note note : returnAmounts.getNotes()) {
                Element noteElm = document.createElement("Note");
                noteElm.setAttribute("type", note.getType());
                noteElm.setTextContent(note.getValue());
                amounts.appendChild(noteElm);
            }
            element.appendChild(amounts);
        }
        return element;
    }

    /**
     * Create ItemValues based on POSTed parameters.
     *
     * @param entity representation
     */
    @Override
    public void doAccept(Representation entity) {

        log.debug("doAccept()");

        Set<String> names = getForm().getNames();

        // Obtain the startDate.
        StartEndDate startDate = new StartEndDate(getForm().getFirstValue("startDate"));
        names.remove("startDate");

        // The submitted startDate must be on or after the epoch.
        if (!dataItem.isWithinLifeTime(startDate)) {
            log.warn("acceptRepresentation() badRequest - Trying to create a DIV before the epoch.");
            badRequest();
            return;
        }

        // Update named ItemValues.
        for (String name : names) {

            // Fetch the itemValueDefinition.
            ItemValueDefinition itemValueDefinition = dataItem.getItemDefinition().getItemValueDefinition(name);
            if (itemValueDefinition == null) {
                // The submitted ItemValueDefinition must be in the owning ItemDefinition.
                log.warn("acceptRepresentation() badRequest - Trying to create a DIV with an IVD not belonging to the DI ID.");
                badRequest();
                return;
            }

            // Cannot create new ItemValues for ItemValueDefinitions which are used in the DrillDown for
            // the owning ItemDefinition.
            if (dataItem.getItemDefinition().isDrillDownValue(itemValueDefinition)) {
                log.warn("acceptRepresentation() badRequest - Trying to create a DIV that is a DrillDown value.");
                badRequest();
                return;
            }

            // The new ItemValue must be unique on itemValueDefinitionUid + startDate.
            if (!dataItem.isUnique(itemValueDefinition, startDate)) {
                log.warn("acceptRepresentation() badRequest - Trying to create a DIV with the same IVD and startDate as an existing DIV.");
                badRequest();
                return;
            }

            // Create the new ItemValue entity.
            ItemValue newDataItemValue = new ItemValue(itemValueDefinition, dataItem, getForm().getFirstValue(name));
            newDataItemValue.setStartDate(startDate);
        }

        // Clear caches.
        dataService.invalidate(dataItem.getDataCategory());

        // Return successful creation of new DataItemValue.
        successfulPost(getFullPath(), dataItem.getUid());
    }

    /**
     * Update the DataItem and contained ItemValues based on PUT parameters. ItemValues can be identified
     * by their ItemValueDefinition path or their specific UID.
     * <p/>
     * When updating ItemValues using the ItemValueDefinition path the appropriate instance will
     * be selected based on the query string startDate parameter. This is only relevant for non drill-down
     * ItemValues, as only once instance of these is allowed.
     *
     * @param entity representation
     */
    @Override
    public void doStore(Representation entity) {

        log.debug("doStore()");

        Form form = getForm();
        Set<String> names = form.getNames();

        // Update 'name' value.
        if (names.contains("name")) {
            dataItem.setName(form.getFirstValue("name"));
            names.remove("name");
        }

        // Update 'path' value.
        if (names.contains("path")) {
            dataItem.setPath(form.getFirstValue("path"));
            names.remove("path");
        }

        // Update named ItemValues.
        for (String name : form.getNames()) {
            ItemValue itemValue = dataItem.getItemValue(name);
            if (itemValue != null) {
                itemValue.setValue(form.getFirstValue(name));
            }
        }

        // Clear caches.
        dataService.invalidate(dataItem.getDataCategory());

        // Return successful update of DataItem.
        successfulPut("/data/" + dataItem.getFullPath());
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        dataService.invalidate(dataItem.getDataCategory());
        dataService.remove(dataItem);
        successfulDelete("/data/" + dataItem.getDataCategory().getFullPath());
    }

    public String getFullPath() {
        return "/data" + dataItem.getFullPath();
    }
}