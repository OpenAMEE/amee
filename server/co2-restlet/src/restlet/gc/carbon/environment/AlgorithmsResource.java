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
import gc.carbon.data.DataConstants;
import gc.carbon.domain.data.Algorithm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class AlgorithmsResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
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
        } else if (getRequest().getMethod().equals(Method.POST)) {
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
        } else if (getRequest().getMethod().equals(Method.POST)) {
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
