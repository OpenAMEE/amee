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
import gc.carbon.domain.path.InternalValue;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.profile.ProfileFinder;
import gc.carbon.data.DataServiceDAO;
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

@Service
public class Calculator implements BeanFactoryAware, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    DataServiceDAO dataServiceDAO;

    private BeanFactory beanFactory;

    public BigDecimal calculate(ProfileItem profileItem) {
        log.debug("calculate() - starting calculator");
        BigDecimal amount;
        if (!profileItem.isEnd()) {
            ItemDefinition itemDefinition = profileItem.getItemDefinition();
            Algorithm algorithm = getAlgorithm(itemDefinition, "perMonth");

            if (algorithm != null) {

                DataFinder dataFinder = initDataFinder(profileItem);
                ProfileFinder profileFinder = initProfileFinder(profileItem, dataFinder);
                Map<String, Object> values = getValues(profileItem);
                values.put("profileFinder", profileFinder);
                values.put("dataFinder", dataFinder);

                // get the new amount via algorithm and values
                amount = calculate(algorithm, values);

                if (amount != null) {
                    profileItem.updateAmount(amount);
                } else {
                    log.warn("calculate() - Calculated amount was NULL. Setting amount to ZERO");
                    amount = ProfileItem.ZERO;
                }
            } else {
                log.warn("calculate() - Algorithm NOT found. Setting amount to ZERO");
                amount = ProfileItem.ZERO;
            }
        } else {
            amount = ProfileItem.ZERO;
            profileItem.updateAmount(amount);
        }
        return amount;
    }

    private ProfileFinder initProfileFinder(ProfileItem profileItem, DataFinder dataFinder) {
        ProfileFinder profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
        profileFinder.setDataFinder(dataFinder);
        profileFinder.setProfileItem(profileItem);
        return profileFinder;
    }

    private DataFinder initDataFinder(ProfileItem profileItem) {
        DataFinder dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
        dataFinder.setStartDate(profileItem.getStartDate());
        dataFinder.setEndDate(profileItem.getEndDate());
        return dataFinder;
    }

    public BigDecimal calculate(DataItem dataItem, Choices userValueChoices) {
        log.debug("calculate() - Starting calculator");
        DataFinder dataFinder;
        ProfileFinder profileFinder;
        Map<String, Object> values;
        BigDecimal amount;
        ItemDefinition itemDefinition = dataItem.getItemDefinition();
        Algorithm algorithm = getAlgorithm(itemDefinition, "perMonth");
        if (algorithm != null) {
            dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
            profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
            profileFinder.setDataFinder(dataFinder);
            values = getValues(dataItem, userValueChoices);
            values.put("profileFinder", profileFinder);
            values.put("dataFinder", dataFinder);
            amount = calculate(algorithm, values);
        } else {
            log.warn("calculate() - Algorithm NOT found");
            amount = ProfileItem.ZERO;
        }
        return amount;
    }

    protected BigDecimal calculate(Algorithm algorithm, Map<String, Object> values) {

        BigDecimal amount = ProfileItem.ZERO;
        String value = null;

        String algorithmContent = algorithm.getContent();

        try {
            Context cx = Context.enter();
            Scriptable scope = cx.initStandardObjects();

            for (String key : values.keySet()) {
                scope.put(key, scope, values.get(key));
            }

            Object result = cx.evaluateString(scope, algorithmContent, "", 0, null);
            value = Context.toString(result);
            log.debug("calculate() - Unscaled amount: " + value);
        } catch (EvaluatorException e) {
            log.warn("calculate() - " + e);
        } catch (RhinoException e) {
            log.warn("calculate() - " + e);
        } finally {
            Context.exit();
        }

        // Scale the result
        if (value != null) {
            try {
                amount = new BigDecimal(value);
                amount = amount.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                if (amount.precision() > ProfileItem.PRECISION) {
                    log.warn("calculate() - Precision is too big: " + amount);
                }
            } catch (Exception e) {
                log.warn("calculate() - " + e);
            }
            log.debug("calculate() - Scaled amount: " + amount);
        }
        return amount;
    }

    protected Algorithm getAlgorithm(ItemDefinition itemDefinition, String path) {
        for (Algorithm algorithm : itemDefinition.getAlgorithms()) {
            if (algorithm.getName().equalsIgnoreCase(path)) {
                log.debug("getAlgorithm() - Found Algorithm: " + algorithm.getId());
                return algorithm;
            }
        }
        return null;
    }

    public Map<String, Object> getValues(ProfileItem profileItem) {
        Map<ItemValueDefinition, InternalValue> values = new HashMap<ItemValueDefinition, InternalValue>();
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
        Map<ItemValueDefinition, InternalValue> values = new HashMap<ItemValueDefinition, InternalValue>();
        dataItem.getItemDefinition().appendInternalValues(values);
        dataItem.appendInternalValues(values);
        appendUserValueChoices(userValueChoices, values);

        Map<String, Object> returnValues = new HashMap<String, Object>();
        for (ItemValueDefinition ivd : values.keySet()) {
            returnValues.put(ivd.getPath(), values.get(ivd).getValue());
        }

        return returnValues;
    }

    private void appendUserValueChoices(Choices userValueChoices, Map<ItemValueDefinition, InternalValue> values) {
        if (userValueChoices != null) {
            Map<ItemValueDefinition, InternalValue> userChoices = new HashMap<ItemValueDefinition, InternalValue>();
            for (ItemValueDefinition itemValueDefinition : values.keySet()) {
                if (itemValueDefinition.isFromProfile() && userValueChoices.containsKey(itemValueDefinition.getPath())) {
                    userChoices.put(itemValueDefinition,
                            new InternalValue(userValueChoices.get(itemValueDefinition.getPath()).getValue()));
                }
            }
            values.putAll(userChoices);
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}