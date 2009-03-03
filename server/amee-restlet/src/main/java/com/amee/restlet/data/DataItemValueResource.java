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

import com.amee.domain.APIUtils;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.service.data.DataConstants;
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
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setDataItem(request.getAttributes().get("itemUid").toString());
        setItemValueUid(request.getAttributes().get("valueUid").toString());
        setAvailable(isValid());
    }

    private void setItemValueUid(String itemValueUid) {
        if (itemValueUid.isEmpty()) return;
        this.itemValue = dataService.getItemValue(itemValueUid);
    }

    private ItemValue getItemValue() {
        return itemValue;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (getDataItem() != null) && (getItemValue() != null);
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
        obj.put("actions", getActions(dataBrowser.getDataItemActions()));
        obj.put("itemValue", getItemValue().getJSONObject());
        obj.put("dataItem", getDataItem().getIdentityJSONObject());
        obj.put("path", pathItem.getFullPath());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("DataItemValueResource");
        element.appendChild(getItemValue().getElement(document));
        element.appendChild(getDataItem().getIdentityElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (dataBrowser.getDataItemActions().isAllowView()) {
            DataItem dataItem = getDataItem();
            ItemValue itemValue = getItemValue();
            if (itemValue.getItem().equals(dataItem)) {
                super.handleGet();
            } else {
                log.warn("itemValue not found");
                notFound();
            }
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
        if (dataBrowser.getDataItemActions().isAllowModify()) {
            Form form = getForm();
            DataItem dataItem = getDataItem();
            ItemValue itemValue = getItemValue();
            // validation
            if (itemValue.getItem().equals(dataItem)) {
                // are we updating this ItemValue?
                if (form.getFirstValue("value") != null) {
                    // update ItemValue
                    itemValue.setValue(form.getFirstValue("value"));
                }
                // all done
                if (isStandardWebBrowser()) {
                    success(getFullPath());
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                log.warn("itemValue not found");
                notFound();
            }
        } else {
            notAuthorized();
        }
    }
}