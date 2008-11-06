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

import com.jellymold.sheet.Choice;
import com.jellymold.sheet.Choices;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.path.PathItem;
import gc.carbon.path.PathItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class DataItemResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataService dataService;

    @Autowired
    private Calculator calculator;

    @Autowired
    private DataBrowser dataBrowser;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private PathItemService pathItemService;

    // TODO: Springify
    @Autowired
    private PathItem pathItem;

    private List<Choice> parameters = new ArrayList<Choice>();

    public DataItemResource() {
        super();
    }

    public DataItemResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        dataBrowser.setDataItemUid(request.getAttributes().get("itemUid").toString());
        Form query = request.getResourceRef().getQueryAsForm();
        for (String key : query.getNames()) {
            parameters.add(new Choice(key, query.getValues(key)));
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (dataBrowser.getDataItem() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_DATA_ITEM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        DataItem dataItem = dataBrowser.getDataItem();
        Choices userValueChoices = dataService.getUserValueChoices(dataItem);
        userValueChoices.merge(parameters);
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataItem", dataItem);
        values.put("node", dataItem);
        values.put("userValueChoices", userValueChoices);
        values.put("amountPerMonth", calculator.calculate(dataItem, userValueChoices));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        DataItem dataItem = dataBrowser.getDataItem();
        Choices userValueChoices = dataService.getUserValueChoices(dataItem);
        userValueChoices.merge(parameters);
        JSONObject obj = new JSONObject();
        obj.put("dataItem", dataItem.getJSONObject());
        obj.put("path", pathItem.getFullPath());
        obj.put("userValueChoices", userValueChoices.getJSONObject());
        obj.put("amountPerMonth", calculator.calculate(dataItem, userValueChoices));
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        DataItem dataItem = dataBrowser.getDataItem();
        Choices userValueChoices = dataService.getUserValueChoices(dataItem);
        userValueChoices.merge(parameters);
        Element element = document.createElement("DataItemResource");
        element.appendChild(dataItem.getElement(document));
        element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
        element.appendChild(userValueChoices.getElement(document));
        element.appendChild(APIUtils.getElement(document, "AmountPerMonth",
                calculator.calculate(dataItem, userValueChoices).toString()));
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
    public void put(Representation entity) {
        log.debug("put");
        if (dataBrowser.getDataItemActions().isAllowModify()) {
            Form form = getForm();
            DataItem dataItem = dataBrowser.getDataItem();
            // are we updating this DataItem?
            if (form.getNames().contains("name")) {
                dataItem.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("path")) {
                dataItem.setPath(form.getFirstValue("path"));
            }
            // update ItemValues if supplied
            Map<String, ItemValue> itemValues = dataItem.getItemValuesMap();
            for (String name : form.getNames()) {
                ItemValue itemValue = itemValues.get(name);
                if (itemValue != null) {
                    itemValue.setValue(form.getFirstValue(name));
                }
            }
            // clear caches
            pathItemService.removePathItemGroup(dataItem.getEnvironment());
            dataSheetService.removeSheet(dataItem.getDataCategory());
            success(dataBrowser.getFullPath());
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void delete() {
        log.debug("delete");
        if (dataBrowser.getDataItemActions().isAllowDelete()) {
            DataItem dataItem = dataBrowser.getDataItem();
            pathItemService.removePathItemGroup(dataItem.getEnvironment());
            dataSheetService.removeSheet(dataItem.getDataCategory());
            dataService.remove(dataItem);
            success(pathItem.getParent().getFullPath());
        } else {
            notAuthorized();
        }
    }
}