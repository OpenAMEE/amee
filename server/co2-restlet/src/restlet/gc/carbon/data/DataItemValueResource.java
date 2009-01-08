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
package gc.carbon.data;

import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemValue;
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

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        dataBrowser.setDataItemUid(request.getAttributes().get("itemUid").toString());
        dataBrowser.setItemValueUid(request.getAttributes().get("valueUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (dataBrowser.getDataItemUid() != null) &&
                (dataBrowser.getItemValueUid() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_CARBON_VALUE;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataItem", dataBrowser.getDataItem());
        values.put("itemValue", dataBrowser.getItemValue());
        values.put("node", dataBrowser.getItemValue());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("itemValue", dataBrowser.getItemValue().getJSONObject());
        obj.put("dataItem", dataBrowser.getDataItem().getIdentityJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("DataItemValueResource");
        element.appendChild(dataBrowser.getItemValue().getElement(document));
        element.appendChild(dataBrowser.getDataItem().getIdentityElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (dataBrowser.getDataItemActions().isAllowView()) {
            DataItem dataItem = dataBrowser.getDataItem();
            ItemValue itemValue = dataBrowser.getItemValue();
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
            DataItem dataItem = dataBrowser.getDataItem();
            ItemValue itemValue = dataBrowser.getItemValue();
            // validation
            if (itemValue.getItem().equals(dataItem)) {
                // are we updating this ItemValue?
                if (form.getFirstValue("value") != null) {
                    // update ItemValue
                    itemValue.setValue(form.getFirstValue("value"));
                    // TODO: recalculate now? make profile dirty?
                }
                // all done
                if (isStandardWebBrowser()) {
                    success(dataBrowser.getFullPath());
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