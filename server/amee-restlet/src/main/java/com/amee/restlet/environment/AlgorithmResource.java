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

import com.amee.calculation.service.AlgorithmService;
import com.amee.core.CO2Amount;
import com.amee.domain.AMEEEntity;
import com.amee.domain.StartEndDate;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.algorithm.AlgorithmContext;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.sheet.Choice;
import com.amee.restlet.AuthorizeResource;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
import org.apache.commons.lang.StringUtils;
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

import javax.script.ScriptException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Component
@Scope("prototype")
public class AlgorithmResource extends AuthorizeResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Autowired
    private AlgorithmService algorithmService;

    private AlgorithmTestWrapper algorithmTestWrapper = null;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
        definitionBrowser.setAlgorithmUid(request.getAttributes().get("algorithmUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getAlgorithm() != null);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getActiveEnvironment());
        entities.add(definitionBrowser.getEnvironment());
        entities.add(definitionBrowser.getItemDefinition());
        entities.add(definitionBrowser.getAlgorithm());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ALGORITHM;
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

        if (algorithmTestWrapper != null) {
            obj.put("algorithmTestWrapper", algorithmTestWrapper.getJSONObject());
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
    public void doGet() {
        log.debug("doGet()");
        Form form = getForm();
        Set<String> names = form.getNames();
        if (names.contains("testAlgorithmContent")) {
            testAlgorithm(form);
        }
        super.doGet();
    }

    /**
     * Tests the algorithm in the form
     *
     * @param form the form
     */
    private void testAlgorithm(Form form) {

        algorithmTestWrapper = new AlgorithmTestWrapper();
        algorithmTestWrapper.parseForm(form);

        // apply calculation
        try {
            algorithmTestWrapper.setAmount(
                    new CO2Amount(algorithmService.evaluate(
                            algorithmTestWrapper.getMockAlgorithm(),
                            algorithmTestWrapper.getValuesMap())).getValue());
        } catch (ScriptException sc) {
            algorithmTestWrapper.setError(new StringBuffer(sc.getMessage()));
        }
    }

    @Override
    public void doStore(Representation entity) {
        log.debug("doStore()");
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
            String algorithmContextUid = form.getFirstValue("algorithmContextUid");
            if (StringUtils.isBlank(algorithmContextUid)) {
                algorithm.setAlgorithmContext(null);
            } else {
                algorithm.setAlgorithmContext(
                        definitionService.getAlgorithmContextByUid(
                                definitionBrowser.getEnvironment(), algorithmContextUid));
            }
        }
        success();
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        definitionService.remove(definitionBrowser.getAlgorithm());
        success();
    }

    private class AlgorithmTestWrapper {

        private StringBuffer error = null;

        private BigDecimal amount = null;

        private String values = null;

        private StartEndDate startDate = null;

        private StartEndDate endDate = null;

        private Algorithm mockAlgorithm = null;

        private Map<String, Object> valuesMap = new HashMap<String, Object>();


        public AlgorithmTestWrapper() {
            super();
        }

        public StringBuffer getError() {
            return error;
        }

        public void setError(StringBuffer error) {
            this.error = error;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getValues() {
            return values;
        }

        public void setValues(String values) {
            this.values = values;

            if (values != null) {
                List<Choice> choices = Choice.parseChoices(values);
                for (Choice choice : choices) {
                    valuesMap.put(choice.getName(), choice.getValue());
                }
            }
        }

        public StartEndDate getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = new StartEndDate(startDate);
        }

        public StartEndDate getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            if (endDate != null) {
                this.endDate = new StartEndDate(endDate);
            }
        }

        public Algorithm getMockAlgorithm() {
            return mockAlgorithm;
        }

        public Map<String, Object> getValuesMap() {
            return valuesMap;
        }

        private void setMockAlgorithm(String content, String contextContent) {
            this.mockAlgorithm = new Algorithm();
            this.mockAlgorithm.setContent(content);
            if (!StringUtils.isBlank(contextContent)) {
                AlgorithmContext algorithmContext = new AlgorithmContext();
                algorithmContext.setEnvironment(definitionBrowser.getEnvironment());
                algorithmContext.setContent(contextContent);
                mockAlgorithm.setAlgorithmContext(algorithmContext);
            }
        }

        public ProfileItem getMockProfileItem() {
            ProfileItem mockProfileItem = new ProfileItem();
            mockProfileItem.setStartDate(getStartDate());
            mockProfileItem.setEndDate(getEndDate());
            return mockProfileItem;
        }

        public void parseForm(Form form) {
            setMockAlgorithm(form.getFirstValue("testAlgorithmContent"), form.getFirstValue("testAlgorithmContextContent"));
            setValues(form.getFirstValue("testValues"));
            setStartDate(form.getFirstValue("startDate"));
            setEndDate(form.getFirstValue("endDate"));
        }

        public JSONObject getJSONObject() throws JSONException {
            JSONObject obj = new JSONObject();

            if (algorithmTestWrapper.getError() != null) {
                obj.put("error", algorithmTestWrapper.getError());
            }
            if (algorithmTestWrapper.getAmount() != null) {
                obj.put("result", algorithmTestWrapper.getAmount());
            }
            if (algorithmTestWrapper.getValues() != null) {
                obj.put("values", algorithmTestWrapper.getValues());
            }
            if (algorithmTestWrapper.getStartDate() != null) {
                obj.put("startDate", algorithmTestWrapper.getValues());
            }
            if (algorithmTestWrapper.getEndDate() != null) {
                obj.put("endDate", algorithmTestWrapper.getValues());
            }
            return obj;

        }
    }
}