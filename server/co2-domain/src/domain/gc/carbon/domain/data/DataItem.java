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
package gc.carbon.domain.data;

import com.jellymold.sheet.Choice;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.EngineUtils;
import gc.carbon.domain.ObjectType;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Map;

@Entity
@DiscriminatorValue("DI")
public class DataItem extends Item {

    @Column(name = "PATH")
    @Index(name = "PATH_IND")
    private String path = "";

    public DataItem() {
        super();
    }

    public DataItem(DataCategory dataCategory, ItemDefinition itemDefinition) {
        super(dataCategory, itemDefinition);
    }

    public String toString() {
        return "DataItem_" + getUid();
    }

    @Transient
    public String getLabel() {
        ItemValue itemValue;
        String label = "";
        ItemDefinition itemDefinition = getItemDefinition();
        Map<String, ItemValue> itemValuesMap = getItemValuesMap();
        for (Choice choice : itemDefinition.getDrillDownChoices()) {
            itemValue = itemValuesMap.get(choice.getName());
            if ((itemValue != null) &&
                    (itemValue.getValue().length() > 0) &&
                    !itemValue.getValue().equals("-")) {
                if (label.length() > 0) {
                    label = label.concat(", ");
                }
                label = label.concat(itemValue.getValue());
            }
        }
        if (label.length() == 0) {
            label = getDisplayPath();
        }
        return label;
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        buildElement(obj, detailed);
        obj.put("path", getPath());
        obj.put("label", getLabel());
        obj.put("startDate", getStartDate().toString());
        obj.put("endDate", (getEndDate() != null) ? getEndDate().toString() : "");
        return obj;
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        Element dataItemElement = document.createElement("DataItem");
        buildElement(document, dataItemElement, detailed);
        dataItemElement.appendChild(APIUtils.getElement(document, "Path", getDisplayPath()));
        dataItemElement.appendChild(APIUtils.getElement(document, "Label", getLabel()));
        dataItemElement.appendChild(APIUtils.getElement(document, "StartDate", getStartDate().toString()));
        dataItemElement.appendChild(APIUtils.getElement(document, "EndDate", (getEndDate() != null) ? getEndDate().toString() : ""));
        return dataItemElement;
    }

    @Transient
    public String getDisplayPath() {
        return EngineUtils.getDisplayPath(this);
    }

    @Transient
    public String getDisplayName() {
        return EngineUtils.getDisplayName(this);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            path = "";
        }
        this.path = path;
    }

    @Transient
    public String getType() {
        return ObjectType.DI.toString();
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.DI;
    }
}