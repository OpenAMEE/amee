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
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.data.builder.v2.ItemValueBuilder;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.Map;

//TODO - Move to builder model
@Component
@Scope("prototype")
public class DataItemValueResource extends BaseDataResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private ItemValue itemValue;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        setDataItemByPathOrUid(request.getAttributes().get("itemPath").toString());
        setDataItemValue(request.getAttributes().get("valuePath").toString());
    }

    private void setDataItemValue(String itemValuePath) {
        if (itemValuePath.isEmpty() || getDataItem() == null) return;
        this.itemValue = getDataItem().matchItemValue(itemValuePath);
    }

    private ItemValue getItemValue() {
        return itemValue;
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (getDataItem() != null) &&
                (getItemValue() != null) &&
                getItemValue().getItem().equals(getDataItem()) &&
                getItemValue().getEnvironment().equals(environment);
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + DataConstants.VIEW_CARBON_VALUE;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataItem", getDataItem());
        values.put("itemValue", getItemValue());
        values.put("node", getItemValue());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        getItemValue().setBuilder(new ItemValueBuilder(getItemValue()));
        obj.put("itemValue", getItemValue().getJSONObject());
        obj.put("dataItem", getDataItem().getIdentityJSONObject());
        obj.put("path", pathItem.getFullPath());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("DataItemValueResource");
        getItemValue().setBuilder(new ItemValueBuilder(getItemValue()));
        element.appendChild(getItemValue().getElement(document));
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
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");
        if (dataBrowser.getDataItemActions().isAllowModify()) {
            Form form = getForm();
            // are we updating this ItemValue?
            if (form.getFirstValue("value") != null) {
                // update ItemValue
                getItemValue().setValue(form.getFirstValue("value"));
                // clear caches
                dataService.clearCaches(getDataItem().getDataCategory());
            }
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
            ItemValue itemValue = getItemValue();
            final ItemValueDefinition itemValueDefinition = itemValue.getItemValueDefinition();
            int valuesInHistory = CollectionUtils.countMatches(getDataItem().getActiveItemValues(), new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    ItemValue iv = (ItemValue) o;
                    return iv.getItemValueDefinition().equals(itemValueDefinition);
                }
            });


            if (valuesInHistory > 1) {
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