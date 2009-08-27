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
package com.amee.restlet.environment;

import com.amee.domain.AMEEEntity;
import com.amee.domain.algorithm.AlgorithmContext;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class AlgorithmContextResource extends AuthorizeResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setAlgorithmContextUid(request.getAttributes().get("algorithmContentUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (definitionBrowser.getEnvironment() != null) &&
                (definitionBrowser.getAlgorithmContext() != null);
    }

    @Override
    protected List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(definitionBrowser.getEnvironment());
        entities.add(definitionBrowser.getItemDefinition());
        entities.add(definitionBrowser.getAlgorithm());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_CARBON_ALGORITHM_CONTEXT;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", definitionBrowser.getEnvironment());
        values.put("algorithmContext", definitionBrowser.getAlgorithmContext());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("algorithmContextResource", definitionBrowser.getAlgorithmContext().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("algorithmContextResource");
        element.appendChild(definitionBrowser.getAlgorithmContext().getElement(document));
        return element;
    }

    @Override
    protected void doStore(Representation entity) {
        log.debug("doStore");
        AlgorithmContext algorithmContext = definitionBrowser.getAlgorithmContext();
        Form form = getForm();
        Set<String> names = form.getNames();
        if (names.contains("name")) {
            algorithmContext.setName(form.getFirstValue("name"));
        }
        if (names.contains("content")) {
            algorithmContext.setContent(form.getFirstValue("content"));
        }
        success();
    }

    @Override
    protected void doRemove() {
        log.debug("doRemove");
        AlgorithmContext algorithmContext = definitionBrowser.getAlgorithmContext();
        definitionService.remove(algorithmContext);
        success();
    }
}