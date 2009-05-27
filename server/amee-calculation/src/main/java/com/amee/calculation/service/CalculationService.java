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
package com.amee.calculation.service;

import com.amee.core.CO2Amount;
import com.amee.core.Decimal;
import com.amee.domain.APIVersion;
import com.amee.domain.InternalValue;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.profile.CO2CalculationService;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.sheet.Choices;
import com.amee.service.transaction.TransactionController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalculationService implements CO2CalculationService, BeanFactoryAware, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private AlgorithmService algorithmService;

    @Autowired
    private TransactionController transactionController;

    // Set by Spring context. The BeanFactory used to retreive ProfileFinder and DataFinder instances.
    private BeanFactory beanFactory;

    /**
     * Calculate and always set the CO2 amount for a ProfileItem.
     *
     * @param profileItem - the ProfileItem for which to calculate CO2 amount
     */
    public void calculate(ProfileItem profileItem) {

        CO2Amount amount = CO2Amount.ZERO;

        // End marker ProfileItems can only have zero amounts.
        if (!profileItem.isEnd()) {
            // Calculate amount for ProfileItem if an Algorithm is available.
            // Some ProfileItems are from ItemDefinitions which do not have Algorithms and
            // hence do not support calculations.
            if (profileItem.supportsCalculation()) {
                Algorithm algorithm = algorithmService.getAlgorithm(profileItem.getItemDefinition());
                if (algorithm != null) {
                    Map<String, Object> values = getValues(profileItem);
                    amount = calculate(algorithm, values);
                }
            }
        }

        // Always set the ProfileItem amount.
        // The ProfileItem will only be re-saved if the amount has changed.
        // If the ProfileItem has changed start a transaction.
        if (profileItem.setAmount(amount)) {
            transactionController.begin(true);
        }
    }

    /**
     * Calculate and return the CO2 amount for a DataItem and a set of user specified values.
     * <p/>
     * Note: I am unsure if this is in active use (SM)
     *
     * @param dataItem         - the DataItem for the calculation
     * @param userValueChoices - user supplied value choices
     * @param version          - the APIVersion. This is used to determine the correct ItemValueDefinitions to load into the calculation
     * @return the calculated CO2 amount
     */
    public CO2Amount calculate(DataItem dataItem, Choices userValueChoices, APIVersion version) {
        CO2Amount amount = CO2Amount.ZERO;
        Algorithm algorithm = algorithmService.getAlgorithm(dataItem.getItemDefinition());
        if (algorithm != null) {
            Map<String, Object> values = getValues(dataItem, userValueChoices, version);
            amount = calculate(algorithm, values);
        }
        return amount;
    }

    /**
     * Calculate and return the CO2 amount for given the provided algorithm and input values.
     * <p/>
     * Intended to be used publically in test harnesses when passing the modified algorithm content and input values
     * for execution is desirable.
     *
     * @param algorithm
     * @param values
     * @return the algorithm result as a BigDecimal
     */
    public CO2Amount calculate(Algorithm algorithm, Map<String, Object> values) {
        CO2Amount amount;
        if (log.isTraceEnabled()) {
            log.trace("calculate()");
            log.trace("calculate() - algorithm uid: " + algorithm.getUid());
            log.trace("calculate() - input values: " + values);
            log.trace("calculate() - starting calculation");
        }
        // TODO: Remove Exception catch once we have resolved algo handling on undef values.
        try {
            amount = new CO2Amount(algorithmService.evaluate(algorithm, values));
        } catch (Exception e) {
            log.warn("calculate() - caught Exception: " + e.getMessage(), e);
            amount = new CO2Amount(Decimal.BIG_DECIMAL_ZERO);
        }
        if (log.isTraceEnabled()) {
            log.trace("calculate() - finished calculation");
            log.trace("calculate() - CO2 Amount: " + amount);
        }
        return amount;
    }

    // Collect all relevant algorithm input values for a ProfileItem calculation.
    private Map<String, Object> getValues(ProfileItem profileItem) {

        Map<ItemValueDefinition, InternalValue> values = new HashMap<ItemValueDefinition, InternalValue>();
        profileItem.getItemDefinition().appendInternalValues(values, profileItem.getProfile().getAPIVersion());
        profileItem.getDataItem().appendInternalValues(values);
        profileItem.appendInternalValues(values);

        Map<String, Object> returnValues = new HashMap<String, Object>();
        for (ItemValueDefinition ivd : values.keySet()) {
            returnValues.put(ivd.getCannonicalPath(), values.get(ivd).getValue());
        }

        initAlgoFinders(profileItem, returnValues);

        return returnValues;
    }

    private void initAlgoFinders(ProfileItem profileItem, Map<String, Object> values) {

        DataFinder dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
        dataFinder.setEnvironment(profileItem.getEnvironment());
        dataFinder.setStartDate(profileItem.getStartDate());
        dataFinder.setEndDate(profileItem.getEndDate());

        ProfileFinder profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
        profileFinder.setProfileItem(profileItem);
        profileFinder.setDataFinder(dataFinder);

        values.put("dataFinder", dataFinder);
        values.put("profileFinder", profileFinder);
    }

    // Collect all relevant algorithm input values for a DataItem + auth Choices calculation.
    private Map<String, Object> getValues(DataItem dataItem, Choices userValueChoices, APIVersion version) {

        Map<ItemValueDefinition, InternalValue> values = new HashMap<ItemValueDefinition, InternalValue>();
        dataItem.getItemDefinition().appendInternalValues(values, version);
        dataItem.appendInternalValues(values);
        appendUserValueChoices(dataItem.getItemDefinition(), userValueChoices, values, version);

        Map<String, Object> returnValues = new HashMap<String, Object>();
        for (ItemValueDefinition ivd : values.keySet()) {
            returnValues.put(ivd.getCannonicalPath(), values.get(ivd).getValue());
        }

        DataFinder dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
        dataFinder.setEnvironment(dataItem.getEnvironment());

        ProfileFinder profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
        profileFinder.setDataFinder(dataFinder);

        returnValues.put("dataFinder", dataFinder);
        returnValues.put("profileFinder", profileFinder);

        return returnValues;
    }

    private void appendUserValueChoices(ItemDefinition itemDefinition, Choices userValueChoices,
                                        Map<ItemValueDefinition, InternalValue> values, APIVersion version) {
        if (userValueChoices != null) {
            Map<ItemValueDefinition, InternalValue> userChoices = new HashMap<ItemValueDefinition, InternalValue>();
            for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                // Add each submitted user Choice that is available in the ItemDefinition and for the user's APIVersion
                if (itemValueDefinition.isFromProfile() &&
                        userValueChoices.containsKey(itemValueDefinition.getPath()) &&
                        itemValueDefinition.isValidInAPIVersion(version)) {
                    // Create transient ItemValue.
                    ItemValue itemValue = new ItemValue();
                    itemValue.setItemValueDefinition(itemValueDefinition);
                    itemValue.setValue(userValueChoices.get(itemValueDefinition.getPath()).getValue());
                    if (version.isNotVersionOne()) {
                        if (itemValue.hasUnit() && userValueChoices.containsKey(itemValueDefinition.getPath() + "Unit")) {
                            itemValue.setUnit(userValueChoices.get(itemValueDefinition.getPath() + "Unit").getValue());
                        }
                        if (itemValue.hasPerUnit() && userValueChoices.containsKey(itemValueDefinition.getPath() + "PerUnit")) {
                            itemValue.setPerUnit(userValueChoices.get(itemValueDefinition.getPath() + "PerUnit").getValue());
                        }
                    }
                    // Only add ItemValue value if it is usable.
                    if (itemValue.isUsableValue()) {
                        userChoices.put(itemValueDefinition, new InternalValue(itemValue));
                    }
                }
            }
            values.putAll(userChoices);
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}