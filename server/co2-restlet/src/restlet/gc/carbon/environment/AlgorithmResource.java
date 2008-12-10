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

import com.jellymold.sheet.Choice;
import com.jellymold.utils.BaseResource;
import gc.carbon.data.Calculator;
import gc.carbon.data.DataConstants;
import gc.carbon.definition.DefinitionServiceDAO;
import gc.carbon.domain.data.Algorithm;
import gc.carbon.domain.data.AlgorithmContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.RhinoException;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class AlgorithmResource extends BaseResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionServiceDAO definitionServiceDAO;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Autowired
    private Calculator calculator;

    private
    StringBuffer testAlgorithmError = null;

    private
    BigDecimal testAlgorithmAmount = null;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
        definitionBrowser.setAlgorithmUid(request.getAttributes().get("algorithmUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getAlgorithm() != null);
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_CARBON_ALGORITHM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", definitionBrowser.getEnvironment());
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("algorithm", definitionBrowser.getAlgorithm());
        values.put("algorithmContexts", definitionBrowser.getAlgorithmContexts());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("algorithmResource", definitionBrowser.getAlgorithm().getJSONObject());
        if (definitionBrowser.getAlgorithm().getAlgorithmContext() != null) {
            obj.put("algorithmContext", definitionBrowser.getAlgorithm().getAlgorithmContext().getJSONObject());
        }
        if (testAlgorithmError != null) {
            obj.put("testAlgorithmError", testAlgorithmError.toString());
        }
        if (testAlgorithmAmount != null) {
            obj.put("testAlgorithmResult", testAlgorithmAmount);
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("AlgorithmResource");
        element.appendChild(definitionBrowser.getAlgorithm().getElement(document));

        Element algorithmContextsElement = document.createElement("AlgorithmContexts");
        element.appendChild(algorithmContextsElement);
        for (AlgorithmContext algorithmContext : definitionBrowser.getAlgorithmContexts()) {
            algorithmContextsElement.appendChild(algorithmContext.getElement(document, false));
        }
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet()");
        if (definitionBrowser.getAlgorithmActions().isAllowView()) {

            Form form = getForm();
            Set<String> names = form.getNames();

            if (names.contains("testAlgorithmContent")) {
                testAlgorithm(form);
            }
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    /**
     * Tests the algorithm in the form
     *
     * @param form the form
     */
    private void testAlgorithm(Form form) {
        Algorithm mockAlgorithm = new Algorithm();
        AlgorithmContext mockAlgorithmContext = new AlgorithmContext();

        // parse values
        List<Choice> choices = Choice.parseChoices(form.getFirstValue("testValues"));
        Map<String, Object> values = new HashMap<String, Object>();
        for (Choice choice : choices) {
            values.put(choice.getName(), choice.getValue());
        }

        // innit algorithm
        mockAlgorithm.setContent(form.getFirstValue("testAlgorithmContent"));
        mockAlgorithmContext.setEnvironment(definitionBrowser.getEnvironment());
        mockAlgorithmContext.setContent(form.getFirstValue("testAlgorithmContextContent"));
        mockAlgorithm.setAlgorithmContext(mockAlgorithmContext);

        try {
            testAlgorithmAmount = calculator.calculateWithRuntime(mockAlgorithm, values, true);
        } catch (RhinoException e) {
            testAlgorithmError = new StringBuffer();
            testAlgorithmError.append("Error on line")
                    .append("'").append(e.lineNumber()).append("'");
            if (!StringUtils.isBlank(e.lineSource())) {
                testAlgorithmError.append("\nLine Source: ").append(e.lineSource());
            }
            testAlgorithmError.append("\nError: ").append(e.getMessage());
            if (!StringUtils.isBlank(e.getScriptStackTrace())) {
                testAlgorithmError.append("\nScript StackTrace: ").append(e.getScriptStackTrace());
            }
        } catch (Exception e) {

            StringWriter writer = null;
            PrintWriter printWriter = null;

            try {
                writer = new StringWriter();
                printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);

                testAlgorithmError = new StringBuffer();
                testAlgorithmError.append("Processing Error ")
                        .append(writer.toString());
            } finally {
                if (printWriter != null) {
                    printWriter.close();
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e1) {
                        // consume
                    }
                }
            }
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation()");
        if (definitionBrowser.getAlgorithmActions().isAllowModify()) {
            Algorithm algorithm = definitionBrowser.getAlgorithm();
            Form form = getForm();
            Set<String> names = form.getNames();
            if (names.contains("name")) {
                algorithm.setName(form.getFirstValue("name"));
            }
            if (names.contains("content")) {
                algorithm.setContent(form.getFirstValue("content"));
            }
            if (names.contains("algorithmContextUid")) {
                String algorithmCxtUid = form.getFirstValue("algorithmContextUid");
                if (StringUtils.isBlank(algorithmCxtUid)) {
                    algorithm.setAlgorithmContext(null);
                } else {
                    definitionBrowser.setAlgorithmContextUid(algorithmCxtUid);
                    algorithm.setAlgorithmContext(definitionBrowser.getAlgorithmContext());
                }
            }
            success();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void removeRepresentations() {
        log.debug("removeRepresentations()");
        if (definitionBrowser.getAlgorithmActions().isAllowDelete()) {
            Algorithm algorithm = definitionBrowser.getAlgorithm();
            definitionBrowser.getItemDefinition().remove(algorithm);
            definitionServiceDAO.remove(algorithm);
            success();
        } else {
            notAuthorized();
        }
    }
}
