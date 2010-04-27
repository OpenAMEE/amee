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
package com.amee.domain.algorithm;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.ObjectType;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.environment.Environment;
import com.amee.platform.science.AlgorithmException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

@Entity
@DiscriminatorValue("AL")
public class Algorithm extends AbstractAlgorithm implements com.amee.platform.science.Algorithm {

    // Default Algorithm name to use in calculations
    public final static String DEFAULT = "default";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_DEFINITION_ID", nullable = true)
    private ItemDefinition itemDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ALGORITHM_CONTEXT_ID")
    private AlgorithmContext algorithmContext;

    public Algorithm() {
        super();
    }

    public Algorithm(Environment environment) {
        super(environment);
    }

    public Algorithm(ItemDefinition itemDefinition, String content) {
        super(itemDefinition.getEnvironment(), content);
        setItemDefinition(itemDefinition);
        itemDefinition.add(this);
    }

    @Override
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = super.getJSONObject(detailed);
        if (detailed) {
            obj.put("itemDefinition", getItemDefinition().getIdentityJSONObject());
        }
        return obj;
    }

    public Element getIdentityElement(Document document) {
        return XMLUtils.getIdentityElement(document, this);
    }

    @Override
    public String getElementName() {
        return "Algorithm";
    }

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

    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }

    public void setItemDefinition(ItemDefinition itemDefinition) {
        if (itemDefinition == null) return;
        this.itemDefinition = itemDefinition;
    }

    public AlgorithmContext getAlgorithmContext() {
        return algorithmContext;
    }

    public void setAlgorithmContext(AlgorithmContext algorithmContext) {
        this.algorithmContext = algorithmContext;
    }

    /**
     * Get the Algorithm content with any associated AlgorithmContext content.
     *
     * @return a string constructed from the Algorithm content and context
     */
    public String getFullContent() {
        StringBuffer outContent = new StringBuffer(super.getContent());
        if (getAlgorithmContext() != null) {
            outContent.insert(0, "\n"); // add spacer line
            outContent.insert(0, getAlgorithmContext().getContent());
        }
        return outContent.toString();
    }

    public ObjectType getObjectType() {
        return ObjectType.AL;
    }

    @Override
    public String toString() {
        return "Algorithm_" + getUid();
    }

    public CompiledScript getCompiledScript(ScriptEngine engine) throws ScriptException {
        if (StringUtils.isBlank(getContent())) {
            throw new AlgorithmException(
                    "Algorithm content is null (" + getLabel() + ").");
        }
        CompiledScript compiledScript = ((Compilable) engine).compile(getContent());
        if (compiledScript == null) {
            throw new AlgorithmException(
                    "CompiledScript is null (" + getLabel() + ").");
        }
        return compiledScript;
    }

    public String getLabel() {
        return getItemDefinition() + "/" + getName();
    }
}