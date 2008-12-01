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

import com.jellymold.sheet.Choices;
import gc.carbon.domain.data.Algorithm;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.ItemDefinition;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.path.InternalItemValue;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// TODO: 'perMonth' is hard-coding - how should this be made more dynamic?

@Service
public class Calculator implements BeanFactoryAware, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    DataService dataService;

    private BeanFactory beanFactory;

    public BigDecimal calculate(ProfileItem profileItem) {
        log.debug("starting calculator");
        DataFinder dataFinder;
        ProfileFinder profileFinder;
        Map<String, Object> values;
        BigDecimal amount;
        if (!profileItem.isEnd()) {
            ItemDefinition itemDefinition = profileItem.getItemDefinition();
            Algorithm algorithm = getAlgorithm(itemDefinition, "perMonth");
            if (algorithm != null) {
                // get DataFinder and ProfileFinder beans
                dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
                profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
                profileFinder.setDataFinder(dataFinder);
                // setup values list and initialise DataFinder and ProfileFinder
                values = getValues(profileItem);
                profileFinder.setProfileItem(profileItem);
                dataFinder.setStartDate(profileItem.getStartDate());
                dataFinder.setEndDate(profileItem.getEndDate());
                // dataFinder.getDataItemValue("home/energy/quantity", "type=diesel", "kgCO2PerLitre");
                values.put("profileFinder", profileFinder);
                values.put("dataFinder", dataFinder);
                // get the new amount via algorithm and values
                amount = calculate(algorithm, values);
                if (amount != null) {
                    // store carbon to Item
                    profileItem.updateAmount(amount);
                } else {
                    log.warn("carbon not set");
                    amount = ProfileItem.ZERO;
                }
            } else {
                log.warn("Algorithm NOT found");
                amount = ProfileItem.ZERO;
            }
        } else {
            amount = ProfileItem.ZERO;
            profileItem.updateAmount(amount);
        }
        return amount;
    }

    public BigDecimal calculate(DataItem dataItem, Choices userValueChoices) {
        log.debug("starting calculator");
        DataFinder dataFinder;
        ProfileFinder profileFinder;
        Map<String, Object> values;
        BigDecimal amount;
        ItemDefinition itemDefinition = dataItem.getItemDefinition();
        Algorithm algorithm = getAlgorithm(itemDefinition, "perMonth");
        if (algorithm != null) {
            // get DataFinder and ProfileFinder beans
            dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
            profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
            profileFinder.setDataFinder(dataFinder);
            // get the new amount via algorithm and values
            values = getValues(dataItem, userValueChoices);
            values.put("profileFinder", profileFinder);
            values.put("dataFinder", dataFinder);
            amount = calculate(algorithm, values);
        } else {
            log.warn("Algorithm NOT found");
            amount = ProfileItem.ZERO;
        }
        return amount;
    }

    protected BigDecimal calculate(Algorithm algorithm, Map<String, Object> values) {

        log.debug("getting value");

        BigDecimal amount = ProfileItem.ZERO;
        String value = null;

        // get our template
        String algorithmContent = algorithm.getContent();

        try {
            // use a Rhino context to evaluate the carbon algorithm
            Context cx = Context.enter();
            Scriptable scope = cx.initStandardObjects();

            for (String key : values.keySet()) {
                scope.put(key, scope, values.get(key));
            }

            Object result = cx.evaluateString(scope, algorithmContent, "", 0, null);
            value = Context.toString(result);
            log.debug("value: " + value);
        } catch (EvaluatorException e) {
            log.warn("caught EvaluatorException: " + e.getMessage());
        } catch (RhinoException e) {
            log.warn("caught RhinoException: " + e.getMessage());
        } finally {
            Context.exit();
        }

        // process result
        if (value != null) {
            try {
                amount = new BigDecimal(value);
                amount = amount.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                if (amount.precision() > ProfileItem.PRECISION) {
                    log.warn("precision is too big: " + amount);
                    // TODO: do something?
                }
            } catch (Exception e) {
                // swallow
                log.warn("caught Exception: " + e);
                // TODO: do something?
            }
            log.debug("got value: " + amount);
        }
        return amount;
    }

    protected Algorithm getAlgorithm(ItemDefinition itemDefinition, String path) {
        for (Algorithm algorithm : itemDefinition.getAlgorithms()) {
            if (algorithm.getName().equalsIgnoreCase(path)) {
                log.debug("found Algorithm");
                return algorithm;
            }
        }
        return null;
    }

    public Map<String, Object> getValues(ProfileItem profileItem) {
        Map<ItemValueDefinition, InternalItemValue> values = new HashMap<ItemValueDefinition, InternalItemValue>();
        profileItem.getItemDefinition().appendInternalValues(values);
        profileItem.getDataItem().appendInternalValues(values);
        profileItem.appendInternalValues(values);
        Map<String, Object> returnValues = new HashMap<String, Object>();
        for (ItemValueDefinition ivd : values.keySet()) {
            returnValues.put(ivd.getPath(), values.get(ivd).getValue());
        }
        return returnValues;
    }

    public Map<String, Object> getValues(DataItem dataItem, Choices userValueChoices) {
        Map<ItemValueDefinition, InternalItemValue> values = new HashMap<ItemValueDefinition, InternalItemValue>();
        dataItem.getItemDefinition().appendInternalValues(values);
        dataItem.appendInternalValues(values);
        appendUserValueChoices(userValueChoices, values);

        Map<String, Object> returnValues = new HashMap<String, Object>();
        for (ItemValueDefinition ivd : values.keySet()) {
            returnValues.put(ivd.getPath(), values.get(ivd).getValue());
        }

        return returnValues;
    }

    private void appendUserValueChoices(Choices userValueChoices, Map<ItemValueDefinition, InternalItemValue> values) {
        if (userValueChoices != null) {
            Map<ItemValueDefinition, InternalItemValue> userChoices = new HashMap<ItemValueDefinition, InternalItemValue>();
            for (ItemValueDefinition itemValueDefinition : values.keySet()) {
                if (itemValueDefinition.isFromProfile() && userValueChoices.containsKey(itemValueDefinition.getPath())) {
                    userChoices.put(itemValueDefinition,
                            new InternalItemValue(userValueChoices.get(itemValueDefinition.getPath()).getValue()));
                }
            }
            values.putAll(userChoices);
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}