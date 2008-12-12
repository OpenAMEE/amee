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

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.ObjectType;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;

@Entity
@DiscriminatorValue("AL")
public class Algorithm extends AbstractAlgorithm {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_DEFINITION_ID", nullable = true)
    /** optional for other types of Algorithm*/
    private ItemDefinition itemDefinition;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ALGORITHM_CONTEXT_ID")
    private AlgorithmContext algorithmContext;

    public Algorithm() {
        super();
    }

    public Algorithm(ItemDefinition itemDefinition, String content) {
        this();
        setEnvironment(itemDefinition.getEnvironment());
        setContent(content);
        setItemDefinition(itemDefinition);
        itemDefinition.add(this);
    }

    public Algorithm(ItemDefinition itemDefinition) {
        this(itemDefinition, "");
    }

    @Transient
    @Override
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = super.getJSONObject(detailed);
        if (detailed) {
            obj.put("itemDefinition", getItemDefinition().getIdentityJSONObject());
        }
        return obj;
    }


    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    @Transient
    public String getElementName() {
        return "Algorithm";
    }

    @Transient
    @Override
    public Element getElement(Document document, boolean detailed) {
        Element element = super.getElement(document, detailed);
        if (detailed) {
            element.appendChild(getItemDefinition().getIdentityElement(document));
            if (getAlgorithmContext() != null) {
                element.appendChild(getAlgorithmContext().getIdentityElement(document));
            }
        }
        return element;
    }

    @Override
    public String toString() {
        return "Algorithm_" + getUid();
    }

    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }

    public void setItemDefinition(ItemDefinition itemDefinition) {
        if (itemDefinition != null) {
            this.itemDefinition = itemDefinition;
        }
    }

    public AlgorithmContext getAlgorithmContext() {
        return algorithmContext;
    }

    public void setAlgorithmContext(AlgorithmContext algorithmContext) {
        this.algorithmContext = algorithmContext;
    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    /**
     * Gets the algorithm content and associated aglorithmContext
     *
     * @return returns the  algorithmContext and algorithm context
     */
    public String getFullContent() {
        StringBuffer outContent = new StringBuffer(super.getContent());
        if (getAlgorithmContext() != null) {
            outContent.insert(0, "\n"); // add spacer line
            outContent.insert(0, getAlgorithmContext().getContent());
        }
        return outContent.toString();
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.AL;
    }
}