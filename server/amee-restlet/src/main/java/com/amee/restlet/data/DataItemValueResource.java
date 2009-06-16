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

import com.amee.core.APIUtils;
import com.amee.domain.StartEndDate;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.data.builder.v2.ItemValueBuilder;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataConstants;
import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

//TODO - Move to builder model
@Component
@Scope("prototype")
public class DataItemValueResource extends BaseDataResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    // Will be null is a sequence of ItemValues is being requested.
    private ItemValue itemValue;

    // Will be null is a single ItemValue is being requested.
    private List<ItemValue> itemValues;

    // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
    private String select;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        setDataItemByPathOrUid(request.getAttributes().get("itemPath").toString());
        setDataItemValue(request);
    }

    private void setDataItemValue(Request request) {

        String itemValuePath = request.getAttributes().get("valuePath").toString();

        if (itemValuePath.isEmpty() || getDataItem() == null) {
            return;
        }

        Form query = request.getResourceRef().getQueryAsForm();

        // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
        if (StringUtils.isNotBlank(query.getFirstValue("get"))) {
            this.select = query.getFirstValue("get");
        }

        // The resource may receive a startDate parameter that sets the current date in an historical sequence of
        // ItemValues.
        Date startDate = new Date();
        if (StringUtils.isNotBlank(query.getFirstValue("startDate"))) {
            startDate = new StartEndDate(query.getFirstValue("startDate"));
        }

        // Retrieve all itemValues in a historical sequence if mandated in the request (get=all), otherwise retrieve
        // the closest match
        if (StringUtils.equals(select,"all")) {
            this.itemValues = getDataItem().getItemValues(itemValuePath);
        } else {
            this.itemValue = getDataItem().matchItemValue(itemValuePath, startDate);
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && getDataItem() != null &&
                (isItemValueValid() || isItemValuesValid());
    }

    private boolean isItemValueValid() {
        return this.itemValue != null &&
                this.itemValue.getItem().equals(getDataItem()) &&
                this.itemValue.getEnvironment().equals(environment);
    }


    private boolean isItemValuesValid() {
        if (itemValues == null)
            return false;
        ItemValue test = (ItemValue) CollectionUtils.get(itemValues,0);
        return test.getItem().equals(getDataItem()) && test.getEnvironment().equals(environment);
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + DataConstants.VIEW_CARBON_VALUE;
    }

    @Override
    // Note, itemValues (historical sequences) are not supported in V1 API and templates are only used in v1 API.
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataItem", getDataItem());
        values.put("itemValue", this.itemValue);
        values.put("node", this.itemValue);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (itemValue != null) {
            this.itemValue.setBuilder(new ItemValueBuilder(this.itemValue));
            obj.put("itemValue", this.itemValue.getJSONObject());
        } else {
            JSONArray values = new JSONArray();
            for(ItemValue iv : itemValues) {
                iv.setBuilder(new ItemValueBuilder(iv));
                values.put(iv.getJSONObject(false));
            }
            obj.put("itemValues",values);
        }
        obj.put("dataItem", getDataItem().getIdentityJSONObject());
        obj.put("path", pathItem.getFullPath());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("DataItemValueResource");
        this.itemValue.setBuilder(new ItemValueBuilder(this.itemValue));
        element.appendChild(this.itemValue.getElement(document));
        element.appendChild(getDataItem().getIdentityElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
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
    public boolean allowPost() {
        // POSTs to Data ItemValues are never allowed.
        return false;
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");

        if (dataBrowser.getDataItemActions().isAllowModify()) {
            
            Form form = getForm();
            if (StringUtils.isNotBlank(form.getFirstValue("value"))) {
                this.itemValue.setValue(form.getFirstValue("value"));
            }

            if (StringUtils.isNotBlank(form.getFirstValue("startDate"))) {
                Date startDate = new StartEndDate(form.getFirstValue("startDate"));

                // The submitted startDate must be (i) after or equal to the startDate and (ii) before the endDate of the owning DataItem.
                if (!getDataItem().isWithinLifeTime(startDate)) {
                    log.error("acceptRepresentation() - badRequest: trying to create a DIV starting after the endDate of the owning DI.");
                    badRequest();
                    return;
                }

                this.itemValue.setStartDate(startDate);
            }

            dataService.clearCaches(getDataItem().getDataCategory());
            successfulPut(getFullPath());
        } else {
            notAuthorized();
        }
    }

    @Override
    public void removeRepresentations() {
        log.debug("removeRepresentations()");

        if (dataBrowser.getDataItemActions().isAllowDelete()) {

            // Only allow delete if there would be at least one DataItemValue for this ItemValueDefinition remaining.
            ItemValue itemValue = this.itemValue;
            final ItemValueDefinition itemValueDefinition = itemValue.getItemValueDefinition();

            int remaining = getDataItem().getItemValuesMap().size(itemValueDefinition.getPath());

            if (remaining > 1) {
                dataService.clearCaches(getDataItem().getDataCategory());
                dataService.remove(itemValue);
                successfulDelete(pathItem.getParent().getFullPath());
            } else {
                badRequest(APIFault.DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE);
            }
        } else {
            notAuthorized();
        }
    }
}