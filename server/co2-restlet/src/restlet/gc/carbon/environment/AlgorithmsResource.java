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
package gc.carbon.environment;

import com.jellymold.utils.BaseResource;
import gc.carbon.data.Algorithm;
import gc.carbon.data.DataConstants;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Map;

@Name("algorithmsResource")
@Scope(ScopeType.EVENT)
public class AlgorithmsResource extends BaseResource implements Serializable {

    private final static Logger log = Logger.getLogger(AlgorithmResource.class);

    @In(create = true)
    private DefinitionBrowser definitionBrowser;

    private Algorithm newAlgorithm;

    public AlgorithmsResource() {
        super();
    }

    public AlgorithmsResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getItemDefinition() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ALGORITHMS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", definitionBrowser.getEnvironment());
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("algorithms", definitionBrowser.getItemDefinition().getAlgorithms());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            obj.put("environment", definitionBrowser.getEnvironment().getIdentityJSONObject());
            obj.put("itemDefinition", definitionBrowser.getItemDefinition().getIdentityJSONObject());
            JSONArray algorithms = new JSONArray();
            for (Algorithm algorithm : definitionBrowser.getItemDefinition().getAlgorithms()) {
                algorithms.put(algorithm.getJSONObject(false));
            }
            obj.put("algorithms", algorithms);
        } else if (isPost()) {
            obj.put("algorithm", newAlgorithm.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("AlgorithmsResource");
        if (isGet()) {
            element.appendChild(definitionBrowser.getEnvironment().getIdentityElement(document));
            element.appendChild(definitionBrowser.getItemDefinition().getIdentityElement(document));
            Element algorithmsElement = document.createElement("Algorithms");
            for (Algorithm algorithm : definitionBrowser.getItemDefinition().getAlgorithms()) {
                algorithmsElement.appendChild(algorithm.getElement(document, false));
            }
            element.appendChild(algorithmsElement);
        } else if (isPost()) {
            element.appendChild(newAlgorithm.getElement(document));
        }
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (definitionBrowser.getAlgorithmActions().isAllowList()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void post(Representation entity) {
        log.debug("post");
        if (definitionBrowser.getAlgorithmActions().isAllowCreate()) {
            Form form = getForm();
            if (form.getFirstValue("name") != null) {
                newAlgorithm = new Algorithm(definitionBrowser.getItemDefinition());
                newAlgorithm.setName(form.getFirstValue("name"));
                newAlgorithm.setContent(form.getFirstValue("content"));
            }
            if (newAlgorithm != null) {
                if (isStandardWebBrowser()) {
                    success();
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }
}
