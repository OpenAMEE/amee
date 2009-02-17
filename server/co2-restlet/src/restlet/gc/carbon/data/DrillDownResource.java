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
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.ItemDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO - need to define actions for this resource
// TODO - move to builder resource
@Component
@Scope("prototype")
public class DrillDownResource extends BaseDataResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DrillDownService drillDownService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        dataBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        setAvailable(isValid());
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (dataBrowser.getDataCategoryUid() != null);
    }

    @Override
    public String getTemplatePath() {
        return getApiVersion() + "/" + DataConstants.VIEW_DRILL_DOWN;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        DataCategory dataCategory = dataBrowser.getDataCategory();
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
        DataCategory dataCategory = dataBrowser.getDataCategory();
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
        DataCategory dataCategory = dataBrowser.getDataCategory();
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

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (dataBrowser.getDataCategoryActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
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