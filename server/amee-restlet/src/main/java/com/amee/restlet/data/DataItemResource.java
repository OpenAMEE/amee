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

import com.amee.calculation.service.CalculationService;
import com.amee.core.*;
import com.amee.domain.StartEndDate;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.restlet.RequestContext;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DataService;
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
public class DataItemResource extends BaseDataResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataService dataService;

    @Autowired
    private CalculationService calculationService;

    private Form query;
    private List<Choice> parameters = new ArrayList<Choice>();

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        query = request.getResourceRef().getQueryAsForm();
        setDataCategory(request.getAttributes().get("categoryUid").toString());
        setDataItemByPathOrUid(request.getAttributes().get("itemPath").toString());
        if (getDataItem() != null) {
            ((RequestContext) ThreadBeanHolder.get("ctx")).setDataItem(getDataItem());
        }
        Set<String> names = query.getNames();

        // The resource may receive a startDate parameter that sets the current date in an historical sequence of
        // ItemValues.
        if (StringUtils.isNotBlank(query.getFirstValue("startDate"))) {
            getDataItem().setEffectiveStartDate(new StartEndDate(query.getFirstValue("startDate")));
        }

        // Pull out any values submitted for Data API calculations.
        for (String key : names) {
            parameters.add(new Choice(key, query.getValues(key)));
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                getDataItem() != null &&
                !getDataItem().isTrash() &&
                getDataCategory() != null &&
                getDataItem().getDataCategory().equals(getDataCategory()) &&
                getDataItem().getEnvironment().equals(environment);
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + DataConstants.VIEW_DATA_ITEM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        DataItem dataItem = getDataItem();
        Choices userValueChoices = dataService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        CO2Amount amount = calculationService.calculate(dataItem, userValueChoices, getAPIVersion());
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new DecimalUnit(SI.KILOGRAM), new DecimalPerUnit(NonSI.MONTH));
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
        DataItem dataItem = getDataItem();
        Choices userValueChoices = dataService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        CO2Amount amount = calculationService.calculate(dataItem, userValueChoices, getAPIVersion());
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new DecimalUnit(SI.KILOGRAM), new DecimalPerUnit(NonSI.MONTH));
        JSONObject obj = new JSONObject();
        obj.put("dataItem", dataItem.getJSONObject(true,false));
        obj.put("path", pathItem.getFullPath());
        obj.put("userValueChoices", userValueChoices.getJSONObject());
        obj.put("amountPerMonth", amount.convert(kgPerMonth).getValue());
        if (getAPIVersion().isNotVersionOne()) {
            String unit = query.getFirstValue("returnUnit");
            String perUnit = query.getFirstValue("returnPerUnit");
            CO2AmountUnit returnUnit = new CO2AmountUnit(unit, perUnit);
            JSONObject amountObj = new JSONObject();
            amountObj.put("value", amount.convert(returnUnit).getValue());
            amountObj.put("unit", returnUnit);
            obj.put("amount", amountObj);
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        DataItem dataItem = getDataItem();
        Choices userValueChoices = dataService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        CO2Amount amount = calculationService.calculate(dataItem, userValueChoices, getAPIVersion());
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new DecimalUnit(SI.KILOGRAM), new DecimalPerUnit(NonSI.MONTH));
        Element element = document.createElement("DataItemResource");
        element.appendChild(dataItem.getElement(document, true, false));
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        element.appendChild(userValueChoices.getElement(document));
        element.appendChild(APIUtils.getElement(document, "AmountPerMonth", amount.convert(kgPerMonth).toString()));
        if (getAPIVersion().isNotVersionOne()) {
            String unit = query.getFirstValue("returnUnit");
            String perUnit = query.getFirstValue("returnPerUnit");
            CO2AmountUnit returnUnit = new CO2AmountUnit(unit, perUnit);
            Element amountElem = document.createElement("Amount");
            amountElem.setTextContent(amount.convert(returnUnit).toString());
            amountElem.setAttribute("unit", returnUnit.toString());
            element.appendChild(amountElem);
        }
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (dataBrowser.getDataItemActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public void acceptRepresentation(Representation entity) {
        log.debug("acceptRepresentation()");

        if (!dataBrowser.getDataItemActions().isAllowCreate()) {
            notAuthorized();
            return;
        }

        StartEndDate startDate = new StartEndDate(getForm().getFirstValue("startDate"));

        // The submitted startDate must be (i) after or equal to the startDate and (ii) before the endDate of the owning DataItem.
        if (!getDataItem().isWithinLifeTime(startDate)) {
            log.error("acceptRepresentation() - badRequest: trying to create a DIV outside the timespan of the owning DI.");
            badRequest();
            return;
        }

        Set<String> names = getForm().getNames();
        names.remove("startDate");
        for (String name : names) {
            ItemValue itemValue = getDataItem().getItemValue(name);
            if (itemValue == null) {
                // The submitted ItemValueDefinition must be in the owning ItemDefinition
                log.error("acceptRepresentation() - badRequest: trying to create a DIV with an IVD not belonging to the DI ID.");
                badRequest();
                return;
            }

            // Cannot create new ItemValues for ItemValueDefinitions which are used in the DrillDown for the owning
            // ItemDefinition
            if (getDataItem().getItemDefinition().isDrillDownValue(itemValue.getItemValueDefinition())) {
                log.error("acceptRepresentation() - badRequest: trying to create a DIV that is a DrillDown value.");
                badRequest();
                return;
            }

            // The new DataItemValue must be unique on itemValueDefinitionUid + startDate.
            if (!getDataItem().isUnique(itemValue.getItemValueDefinition(), startDate)) {
                log.error("acceptRepresentation() - badRequest: trying to create a DIV with the same IVD and StartDate as an existing DIV.");
                badRequest();
                return;
            }

            //Create the new ItemValue entity.
            ItemValue newDataItemValue = new ItemValue(itemValue.getItemValueDefinition(), getDataItem(), getForm().getFirstValue(name));
            newDataItemValue.setStartDate(startDate);

        }

        // Clear caches
        dataService.clearCaches(getDataItem().getDataCategory());

        // Return successful creation of new DataItemValue.
        successfulPost(getFullPath(), getDataItem().getUid());
    }

    @Override
    public void storeRepresentation(Representation entity) {

        log.debug("storeRepresentation()");

        if (!dataBrowser.getDataItemActions().isAllowModify()) {
            notAuthorized();
            return;
        }

        Form form = getForm();
        DataItem dataItem = getDataItem();
        Set<String> names = form.getNames();

        // update 'name' value
        if (names.contains("name")) {
            dataItem.setName(form.getFirstValue("name"));
        }

        // update 'path' value
        if (names.contains("path")) {
            dataItem.setPath(form.getFirstValue("path"));
        }

        // update ItemValues if supplied
        for (String name : form.getNames()) {
            ItemValue itemValue = dataItem.getItemValue(name);
            if (itemValue != null) {
                itemValue.setValue(form.getFirstValue(name));
            }
        }

        // clear caches
        dataService.clearCaches(dataItem.getDataCategory());

        successfulPut(getParentPath() + "/" + dataItem.getResolvedPath());
    }

    @Override
    public void removeRepresentations() {
        log.debug("removeRepresentations()");
        if (dataBrowser.getDataItemActions().isAllowDelete()) {
            DataItem dataItem = getDataItem();
            dataService.clearCaches(dataItem.getDataCategory());
            dataService.remove(dataItem);
            successfulDelete(pathItem.getParent().getFullPath());
        } else {
            notAuthorized();
        }
    }
}