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

import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.kiwi.Environment;
import com.jellymold.sheet.ValueType;
import gc.carbon.ValueDefinition;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.EntityManager;
import java.io.Serializable;

@Name("defraInitialisation")
@Scope(ScopeType.EVENT)
public class DefraInitialisation implements Serializable {

    private final static Logger log = Logger.getLogger(DefraInitialisation.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private SiteService siteService;

    Environment environment;

    String sanitiseStringJSFunction;

    ValueDefinition quantityValueType;
    ValueDefinition textValueType;
    ValueDefinition booleanValueType;
    ValueDefinition kgCO2PerKmValueType;
    ValueDefinition kWhPerYearValueType;
    ValueDefinition kWValueType;
    ValueDefinition litresPerKmValueType;
    ValueDefinition KWhPerCycleValueType;
    ValueDefinition kgCO2PerPassengerJourneyValueType;
    ValueDefinition kgCO2PerKWhValueType;
    ValueDefinition kgCO2PerLitreValueType;
    ValueDefinition kgCO2PerPoundValueType;
    ValueDefinition kgCO2PerKgValueType;
    ValueDefinition kgCO2PerYearValueType;

    ItemDefinition metadataItemDefinition;
    ItemDefinition heatingItemDefinition;
    ItemDefinition cookingItemDefinition;
    ItemDefinition lightingItemDefinition;
    ItemDefinition appliancesItemDefinition;
    ItemDefinition transportItemDefinition;
    ItemDefinition televisionItemDefinition;
    ItemDefinition seasonBillingItemDefinition;
    ItemDefinition fuelItemDefinition;
    ItemDefinition fuelPriceItemDefinition;
    ItemDefinition carItemDefinition;

    DataCategory metadataCat;

    public DefraInitialisation() {
        super();
    }

    public Environment initialise() {

        // Environment essentials
        environment = createEnvironment();
        createValueTypes();
        createJSFunctions();

        // Item Definitions
        metadataItemDefinition = createProfileMetadataID();
        heatingItemDefinition = createHeatingID();
        cookingItemDefinition = createCookieID();
        lightingItemDefinition = createLightingID();
        appliancesItemDefinition = createAppliancesID();
        transportItemDefinition = createTransportID();
        televisionItemDefinition = createTVID();
        seasonBillingItemDefinition = createSeasonalBillingID();
        fuelItemDefinition = createFuelID();
        fuelPriceItemDefinition = createFuelPricesID();
        carItemDefinition = createCarID();

        // Data Categories
        createCats();

        // Data Item for Metadata
        // TODO: createMetadataDI();

        return environment;
    }

    private void createJSFunctions() {
        // Generic JavaScript function to sanitise input strings
        // by removing white spaces and making lowercase.
        // This can be included in any of the Rhino-implementation
        // algorithms.
        sanitiseStringJSFunction = "\n" +
                "function sanitiseString(theString) {\n" +
                "  var noSpacesString = \"\";\n" +
                "  theString = '' + theString;\n" +
                "  splitString = theString.split(\" \");\n" +
                "  for(i = 0; i < splitString.length; i++)\n" +
                "    noSpacesString += splitString[i];\n" +
                "  \n" +
                "  var finalString = noSpacesString.toLowerCase();\n" +
                "  return finalString;\n" +
                "}\n" +
                "\n";
    }

    private Environment createEnvironment() {
        Environment environment = new Environment();
        environment.setName("DEFRA");
        entityManager.persist(environment);
        entityManager.flush();
        return environment;
    }

    private void createValueTypes() {
        entityManager.persist(quantityValueType = new ValueDefinition(environment, "quantity", ValueType.DECIMAL));
        entityManager.persist(textValueType = new ValueDefinition(environment, "text", ValueType.TEXT));
        entityManager.persist(booleanValueType = new ValueDefinition(environment, "boolean", ValueType.TEXT));
        entityManager.persist(kgCO2PerKmValueType = new ValueDefinition(environment, "kgCO2PerKm", ValueType.DECIMAL));
        entityManager.persist(kWhPerYearValueType = new ValueDefinition(environment, "kWhPerYear", ValueType.DECIMAL));
        entityManager.persist(kWValueType = new ValueDefinition(environment, "kW", ValueType.DECIMAL));
        entityManager.persist(litresPerKmValueType = new ValueDefinition(environment, "litresPerKmValueType", ValueType.DECIMAL));
        entityManager.persist(KWhPerCycleValueType = new ValueDefinition(environment, "kWhPerCycle", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerPassengerJourneyValueType = new ValueDefinition(environment, "kgCO2PerPassengerJourney", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerKWhValueType = new ValueDefinition(environment, "kgCO2PerKWh", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerLitreValueType = new ValueDefinition(environment, "kgCO2PerLitre", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerPoundValueType = new ValueDefinition(environment, "kgCO2PerPound", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerKgValueType = new ValueDefinition(environment, "kgCO2PerKg", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerYearValueType = new ValueDefinition(environment, "kgCO2PerYear", ValueType.DECIMAL));
        entityManager.flush();
    }

    private ItemDefinition createProfileMetadataID() {
        // PROFILE METADATA DEFINITION

        ItemDefinition metadataItemDefinition = new ItemDefinition(environment, "Metadata");
        entityManager.persist(metadataItemDefinition);
        metadataItemDefinition.setDrillDown("");

        ItemValueDefinition postcode = new ItemValueDefinition(metadataItemDefinition);
        postcode.setName("Postcode");
        postcode.setPath("postcode");
        postcode.setValueDefinition(textValueType);
        postcode.setChoices("");
        postcode.setValue("");
        postcode.setFromData(false);
        postcode.setFromProfile(true);

        ItemValueDefinition peopleInHousehold = new ItemValueDefinition(metadataItemDefinition);
        peopleInHousehold.setName("People In Household");
        peopleInHousehold.setPath("peopleInHousehold");
        peopleInHousehold.setValueDefinition(quantityValueType);
        peopleInHousehold.setChoices("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20");
        peopleInHousehold.setValue("1");
        peopleInHousehold.setFromData(true);
        peopleInHousehold.setFromProfile(true);

        ItemValueDefinition profileType = new ItemValueDefinition(metadataItemDefinition);
        profileType.setName("Profile Type");
        profileType.setPath("profileType");
        profileType.setValueDefinition(textValueType);
        profileType.setChoices("Individual=Individual,Household=Household");
        profileType.setValue("Individual");
        profileType.setFromData(true);
        profileType.setFromProfile(true);

        Algorithm profileMetadataAlgorithm = new Algorithm(metadataItemDefinition, "0");
        profileMetadataAlgorithm.setName("perMonth");
        return metadataItemDefinition;
    }

    private ItemDefinition createHeatingID() {
        // HEATING DEFINITION

        ItemDefinition heatingItemDefinition = new ItemDefinition(environment, "Heating");
        entityManager.persist(heatingItemDefinition);
        heatingItemDefinition.setDrillDown("heatingHomeDwellingType,heatingHomeNoOfBedrooms,heatingHomeFuel,heatingHomeAge,heatingType");

        ItemValueDefinition heatingHomeDwellingType = new ItemValueDefinition(heatingItemDefinition);
        heatingHomeDwellingType.setName("Home Dwelling Type");
        heatingHomeDwellingType.setPath("heatingHomeDwellingType");
        heatingHomeDwellingType.setValueDefinition(textValueType);
        heatingHomeDwellingType.setChoices("Detached bungalow,Detached house,Enclosed end-terrace,Enclosed mid-terrace,End-terraced house,Flat (2 ext),Flat (3 ext),Masionette,Mid-terraced house,Semi-detached bungalow,Semi-detached house");
        heatingHomeDwellingType.setValue("Semi-detached house");
        heatingHomeDwellingType.setFromData(true);
        heatingHomeDwellingType.setFromProfile(false);

        ItemValueDefinition heatingHomeNoOfBedrooms = new ItemValueDefinition(heatingItemDefinition);
        heatingHomeNoOfBedrooms.setName("Number Of Bedrooms");
        heatingHomeNoOfBedrooms.setPath("heatingHomeNoOfBedrooms");
        heatingHomeNoOfBedrooms.setValueDefinition(quantityValueType);
        heatingHomeNoOfBedrooms.setChoices("1,2,3,4,5,6");
        heatingHomeNoOfBedrooms.setValue("3");
        heatingHomeNoOfBedrooms.setFromData(true);
        heatingHomeNoOfBedrooms.setFromProfile(false);

        ItemValueDefinition heatingHomeFuel = new ItemValueDefinition(heatingItemDefinition);
        heatingHomeFuel.setName("Home Fuel");
        heatingHomeFuel.setPath("heatingHomeFuel");
        heatingHomeFuel.setValueDefinition(textValueType);
        heatingHomeFuel.setChoices("Electricity,Gas,LPG,Oil,Coal");
        heatingHomeFuel.setValue("Gas");
        heatingHomeFuel.setFromData(true);
        heatingHomeFuel.setFromProfile(false);

        ItemValueDefinition heatingHomeAge = new ItemValueDefinition(heatingItemDefinition);
        heatingHomeAge.setName("Home Age");
        heatingHomeAge.setPath("heatingHomeAge");
        heatingHomeAge.setValueDefinition(textValueType);
        heatingHomeAge.setChoices("1930to1995,post1995,pre1930");
        heatingHomeAge.setValue("1930to1995");
        heatingHomeAge.setFromData(true);
        heatingHomeAge.setFromProfile(false);

        ItemValueDefinition heatingType = new ItemValueDefinition(heatingItemDefinition);
        heatingType.setName("Heating Type");
        heatingType.setPath("heatingType");
        heatingType.setValueDefinition(textValueType);
        heatingType.setChoices("Automatic Feed,Boiler ( Old - 12-15yrs old),Boiler (average - 5-10yrs old),Boiler (new - less than 5 yrs old),Closed room heaters,Manual Feed,Modern storage heaters (slim line),Old storage heaters (large volume),Open Fires,Room Heaters,Warm Air,-");
        heatingType.setValue("Boiler (average - 5-10yrs old)");
        heatingType.setFromData(true);
        heatingType.setFromProfile(false);

        ItemValueDefinition heatingKgCO2Year = new ItemValueDefinition(heatingItemDefinition);
        heatingKgCO2Year.setName("kgCO2 Per Year");
        heatingKgCO2Year.setPath("heatingKgCO2Year");
        heatingKgCO2Year.setValueDefinition(kgCO2PerYearValueType);
        heatingKgCO2Year.setValue("0");
        heatingKgCO2Year.setFromData(true);
        heatingKgCO2Year.setFromProfile(false);
        heatingKgCO2Year.setAllowedRoles("dataAdmin");

        ItemValueDefinition heatingSource = new ItemValueDefinition(heatingItemDefinition);
        heatingSource.setName("Source");
        heatingSource.setPath("heatingSource");
        heatingSource.setValueDefinition(textValueType);
        heatingSource.setValue("defra/dgen 2007");
        heatingSource.setFromData(true);
        heatingSource.setFromProfile(false);

        Algorithm heatingKgCO2PerMonthCA = new Algorithm(heatingItemDefinition, "heatingKgCO2Year/12");
        heatingKgCO2PerMonthCA.setName("perMonth");
        return heatingItemDefinition;
    }

    private ItemDefinition createCookieID() {
        // COOKING DEFINITION

        ItemDefinition cookingItemDefinition = new ItemDefinition(environment, "Cooking");
        entityManager.persist(cookingItemDefinition);
        cookingItemDefinition.setDrillDown("cookingNumberOfPeople,cookingFuel");

        ItemValueDefinition cookingNumberOfPeople = new ItemValueDefinition(cookingItemDefinition);
        cookingNumberOfPeople.setName("Number Of People");
        cookingNumberOfPeople.setPath("cookingNumberOfPeople");
        cookingNumberOfPeople.setValueDefinition(textValueType);
        cookingNumberOfPeople.setValue("0");
        cookingNumberOfPeople.setFromData(true);
        cookingNumberOfPeople.setFromProfile(false);

        ItemValueDefinition cookingFuel = new ItemValueDefinition(cookingItemDefinition);
        cookingFuel.setName("Fuel");
        cookingFuel.setPath("cookingFuel");
        cookingFuel.setValueDefinition(textValueType);
        cookingFuel.setChoices("Electric,Electric hob only,Gas,Gas hob only,Mixed");
        cookingFuel.setValue("Gas");
        cookingFuel.setFromData(true);
        cookingFuel.setFromProfile(false);

        ItemValueDefinition cookingKgCO2Year = new ItemValueDefinition(cookingItemDefinition);
        cookingKgCO2Year.setName("kgCO2 Per Year");
        cookingKgCO2Year.setPath("cookingKgCO2Year");
        cookingKgCO2Year.setValueDefinition(kgCO2PerYearValueType);
        cookingKgCO2Year.setValue("0");
        cookingKgCO2Year.setFromData(true);
        cookingKgCO2Year.setFromProfile(false);
        cookingKgCO2Year.setAllowedRoles("dataAdmin");

        ItemValueDefinition cookingSource = new ItemValueDefinition(cookingItemDefinition);
        cookingSource.setName("Source");
        cookingSource.setPath("cookingSource");
        cookingSource.setValueDefinition(textValueType);
        cookingSource.setValue("BRE/MTP/dgen/defra 2007");
        cookingSource.setFromData(true);
        cookingSource.setFromProfile(false);

        Algorithm cookingKgCO2PerMonthCA = new Algorithm(cookingItemDefinition, "cookingKgCO2Year/12");
        cookingKgCO2PerMonthCA.setName("perMonth");
        return cookingItemDefinition;
    }

    private ItemDefinition createLightingID() {
        // LIGHTING DEFINITION

        ItemDefinition lightingItemDefinition = new ItemDefinition(environment, "Lighting");
        entityManager.persist(lightingItemDefinition);
        lightingItemDefinition.setDrillDown("lightingType");

        // TODO: These IVDs (lightingType,lightingkWhYear,lightingSource) are not really needed.
        // TODO: They only exist to facilitate testing via the web interface.
        ItemValueDefinition lightingType = new ItemValueDefinition(lightingItemDefinition);
        lightingType.setName("Type");
        lightingType.setPath("lightingType");
        lightingType.setValueDefinition(textValueType);
        lightingType.setChoices("LEL,normal");
        lightingType.setValue("normal");
        lightingType.setFromData(true);
        lightingType.setFromProfile(false);

        ItemValueDefinition lightingkWhYear = new ItemValueDefinition(lightingItemDefinition);
        lightingkWhYear.setName("kWh Per Year");
        lightingkWhYear.setPath("lightingkWhYear");
        lightingkWhYear.setValueDefinition(kWhPerYearValueType);
        lightingkWhYear.setValue("0");
        lightingkWhYear.setFromData(true);
        lightingkWhYear.setFromProfile(false);

        ItemValueDefinition lightingSource = new ItemValueDefinition(lightingItemDefinition);
        lightingSource.setName("Source");
        lightingSource.setPath("lightingSource");
        lightingSource.setValueDefinition(textValueType);
        lightingSource.setValue("MTP/dgen/defra 2007");
        lightingSource.setFromData(true);
        lightingSource.setFromProfile(false);

        ItemValueDefinition noOfLightBulbs = new ItemValueDefinition(lightingItemDefinition);
        noOfLightBulbs.setName("Number Of Light Bulbs");
        noOfLightBulbs.setPath("noOfLightBulbs");
        noOfLightBulbs.setValueDefinition(quantityValueType);
        noOfLightBulbs.setValue("0");
        noOfLightBulbs.setFromData(false);
        noOfLightBulbs.setFromProfile(true);

        ItemValueDefinition noOfLowEnergyLightBulbs = new ItemValueDefinition(lightingItemDefinition);
        noOfLowEnergyLightBulbs.setName("Number Of Low Energy Light Bulbs");
        noOfLowEnergyLightBulbs.setPath("noOfLowEnergyLightBulbs");
        noOfLowEnergyLightBulbs.setValueDefinition(quantityValueType);
        noOfLowEnergyLightBulbs.setValue("0");
        noOfLowEnergyLightBulbs.setFromData(false);
        noOfLowEnergyLightBulbs.setFromProfile(true);

        String lightingAlgorithm = "\n" +
                sanitiseStringJSFunction +
                "if (sanitiseString(lightingType) == \"normal\") {\n" +
                "  (noOfLightBulbs*lightingkWhYear*0.527) / 12;\n" +
                "} else {\n" +
                "  (noOfLowEnergyLightBulbs*lightingkWhYear*0.527) / 12;\n" +
                "}\n";

        Algorithm lightingKgCO2PerMonthCA = new Algorithm(lightingItemDefinition, lightingAlgorithm);
        lightingKgCO2PerMonthCA.setName("perMonth");
        return lightingItemDefinition;
    }

    private ItemDefinition createAppliancesID() {
        // APPLIANCES DEFINITION

        ItemDefinition appliancesItemDefinition = new ItemDefinition(environment, "Appliances");
        entityManager.persist(appliancesItemDefinition);
        appliancesItemDefinition.setDrillDown("appliancesDevice,appliancesRating,appliancesAge");

        ItemValueDefinition appliancesDevice = new ItemValueDefinition(appliancesItemDefinition);
        appliancesDevice.setName("Device");
        appliancesDevice.setPath("appliancesDevice");
        appliancesDevice.setValueDefinition(textValueType);
        appliancesDevice.setValue("Fridge");
        appliancesDevice.setFromData(true);
        appliancesDevice.setFromProfile(false);

        ItemValueDefinition appliancesRating = new ItemValueDefinition(appliancesItemDefinition);
        appliancesRating.setName("Rating");
        appliancesRating.setPath("appliancesRating");
        appliancesRating.setValueDefinition(textValueType);
        appliancesRating.setValue("Other");
        appliancesRating.setFromData(true);
        appliancesRating.setFromProfile(false);

        ItemValueDefinition appliancesAge = new ItemValueDefinition(appliancesItemDefinition);
        appliancesAge.setName("Age");
        appliancesAge.setPath("appliancesAge");
        appliancesAge.setValueDefinition(textValueType);
        appliancesAge.setValue("-");
        appliancesAge.setFromData(true);
        appliancesAge.setFromProfile(false);

        ItemValueDefinition appliancesKWhPerYear = new ItemValueDefinition(appliancesItemDefinition);
        appliancesKWhPerYear.setName("kWh Per Year");
        appliancesKWhPerYear.setPath("appliancesKWhPerYear");
        appliancesKWhPerYear.setValueDefinition(kWhPerYearValueType);
        appliancesKWhPerYear.setValue("0");
        appliancesKWhPerYear.setFromData(true);
        appliancesKWhPerYear.setFromProfile(false);

        ItemValueDefinition appliancesKWhPerCycle = new ItemValueDefinition(appliancesItemDefinition);
        appliancesKWhPerCycle.setName("kWh Per Cycle");
        appliancesKWhPerCycle.setPath("appliancesKWhPerCycle");
        appliancesKWhPerCycle.setValueDefinition(KWhPerCycleValueType);
        appliancesKWhPerCycle.setValue("0");
        appliancesKWhPerCycle.setFromData(true);
        appliancesKWhPerCycle.setFromProfile(false);

        ItemValueDefinition appliancesSource = new ItemValueDefinition(appliancesItemDefinition);
        appliancesSource.setName("Source");
        appliancesSource.setPath("appliancesSource");
        appliancesSource.setValueDefinition(textValueType);
        appliancesSource.setValue("dgen / defra 2007");
        appliancesSource.setFromData(true);
        appliancesSource.setFromProfile(false);

        ItemValueDefinition appliancesCycle = new ItemValueDefinition(appliancesItemDefinition);
        appliancesCycle.setName("Number Of Cycles");
        appliancesCycle.setPath("appliancesCycle");
        appliancesCycle.setValueDefinition(quantityValueType);
        appliancesCycle.setValue("0");
        appliancesCycle.setFromData(false);
        appliancesCycle.setFromProfile(true);

        ItemValueDefinition appliancesQuantity = new ItemValueDefinition(appliancesItemDefinition);
        appliancesQuantity.setName("Number Owned");
        appliancesQuantity.setPath("appliancesQuantity");
        appliancesQuantity.setValueDefinition(quantityValueType);
        appliancesQuantity.setValue("0");
        appliancesQuantity.setFromData(false);
        appliancesQuantity.setFromProfile(true);

        String applicancesAlgorithm = "\n" +
                sanitiseStringJSFunction +
                "if (appliancesKWhPerYear != 0) {\n" +
                "  if (sanitiseString(appliancesDevice) == \"personalcomputers\" || " +
                "      sanitiseString(appliancesDevice) == \"set-topboxes\" || " +
                "      sanitiseString(appliancesDevice) == \"video\") {\n" +
                "    (appliancesKWhPerYear * 0.527 * appliancesQuantity) / 12;\n" +
                "  } else {\n" +
                "    appliancesKWhPerYear * 0.527 / 12;\n" +
                "  }\n" +
                "} else {\n" +
                "  appliancesKWhPerCycle * appliancesCycle * 0.527;\n" +
                "}";
        Algorithm appliancesKgCO2PerMonthCA = new Algorithm(appliancesItemDefinition, applicancesAlgorithm);
        appliancesKgCO2PerMonthCA.setName("perMonth");
        return appliancesItemDefinition;
    }

    private ItemDefinition createTransportID() {
        // TRANSPORT DEFINITION

        ItemDefinition transportItemDefinition = new ItemDefinition(environment, "Transport");
        entityManager.persist(transportItemDefinition);
        transportItemDefinition.setDrillDown("transportType,transportStyle,transportSize,transportFuel");

        ItemValueDefinition transportType = new ItemValueDefinition(transportItemDefinition);
        transportType.setName("Type");
        transportType.setPath("transportType");
        transportType.setValueDefinition(textValueType);
        transportType.setValue("Car");
        transportType.setFromData(true);
        transportType.setFromProfile(false);

        ItemValueDefinition transportStyle = new ItemValueDefinition(transportItemDefinition);
        transportStyle.setName("Style");
        transportStyle.setPath("transportStyle");
        transportStyle.setValueDefinition(textValueType);
        transportStyle.setValue("European");
        transportStyle.setFromData(true);
        transportStyle.setFromProfile(false);

        ItemValueDefinition transportFuel = new ItemValueDefinition(transportItemDefinition);
        transportFuel.setName("Fuel");
        transportFuel.setPath("transportFuel");
        transportFuel.setValueDefinition(textValueType);
        transportFuel.setValue("Petrol");
        transportFuel.setFromData(true);
        transportFuel.setFromProfile(false);

        ItemValueDefinition transportSize = new ItemValueDefinition(transportItemDefinition);
        transportSize.setName("Size");
        transportSize.setPath("transportSize");
        transportSize.setValueDefinition(textValueType);
        transportSize.setValue("Medium");
        transportSize.setFromData(true);
        transportSize.setFromProfile(false);

        ItemValueDefinition transportKgCO2PerKm = new ItemValueDefinition(transportItemDefinition);
        transportKgCO2PerKm.setName("KgCO2 Per Km");
        transportKgCO2PerKm.setPath("transportKgCO2PerKm");
        transportKgCO2PerKm.setValueDefinition(kgCO2PerKmValueType);
        transportKgCO2PerKm.setValue("0");
        transportKgCO2PerKm.setFromData(true);
        transportKgCO2PerKm.setFromProfile(false);
        transportKgCO2PerKm.setAllowedRoles("dataAdmin");

        ItemValueDefinition transportKgCO2PerPassengerJourney = new ItemValueDefinition(transportItemDefinition);
        transportKgCO2PerPassengerJourney.setName("KgCO2 Per Passenger Journey");
        transportKgCO2PerPassengerJourney.setPath("transportKgCO2PerPassengerJourney");
        transportKgCO2PerPassengerJourney.setValueDefinition(kgCO2PerPassengerJourneyValueType);
        transportKgCO2PerPassengerJourney.setValue("0");
        transportKgCO2PerPassengerJourney.setFromData(true);
        transportKgCO2PerPassengerJourney.setFromProfile(false);
        transportKgCO2PerPassengerJourney.setAllowedRoles("dataAdmin");

        ItemValueDefinition transportSource = new ItemValueDefinition(transportItemDefinition);
        transportSource.setName("Source");
        transportSource.setPath("transportSource");
        transportSource.setValueDefinition(textValueType);
        transportSource.setValue("NAEI / Company Reporting Guidelines, Data emailed from defra to AJC jan08");
        transportSource.setFromData(true);
        transportSource.setFromProfile(false);

        ItemValueDefinition transportDistance = new ItemValueDefinition(transportItemDefinition);
        transportDistance.setName("Distance Travelled");
        transportDistance.setPath("transportDistance");
        transportDistance.setValueDefinition(quantityValueType);
        transportDistance.setValue("0");
        transportDistance.setFromData(true);
        transportDistance.setFromProfile(true);

        ItemValueDefinition transportNumberOfJourneys = new ItemValueDefinition(transportItemDefinition);
        transportNumberOfJourneys.setName("Number of journeys");
        transportNumberOfJourneys.setPath("transportNumberOfJourneys");
        transportNumberOfJourneys.setValueDefinition(quantityValueType);
        transportNumberOfJourneys.setValue("0");
        transportNumberOfJourneys.setFromData(false);
        transportNumberOfJourneys.setFromProfile(true);

        ItemValueDefinition transportKmPerLitre = new ItemValueDefinition(transportItemDefinition);
        transportKmPerLitre.setName("Kilometres per litre");
        transportKmPerLitre.setPath("transportKmPerLitre");
        transportKmPerLitre.setValueDefinition(quantityValueType);
        transportKmPerLitre.setValue("1");
        transportKmPerLitre.setFromData(false);
        transportKmPerLitre.setFromProfile(true);

        ItemValueDefinition transportTyresUnderInflated = new ItemValueDefinition(transportItemDefinition);
        transportTyresUnderInflated.setName("Tyres Under Inflated");
        transportTyresUnderInflated.setPath("transportTyresUnderInflated");
        transportTyresUnderInflated.setValueDefinition(booleanValueType);
        transportTyresUnderInflated.setChoices("false,true");
        transportTyresUnderInflated.setValue("false");
        transportTyresUnderInflated.setFromData(false);
        transportTyresUnderInflated.setFromProfile(true);

        ItemValueDefinition transportAirConAverage = new ItemValueDefinition(transportItemDefinition);
        transportAirConAverage.setName("Air Conditioning Average Use");
        transportAirConAverage.setPath("transportAirConAverage");
        transportAirConAverage.setValueDefinition(booleanValueType);
        transportAirConAverage.setChoices("false,true");
        transportAirConAverage.setValue("false");
        transportAirConAverage.setFromData(false);
        transportAirConAverage.setFromProfile(true);

        ItemValueDefinition transportAirConFull = new ItemValueDefinition(transportItemDefinition);
        transportAirConFull.setName("Air Conditioning Full Power");
        transportAirConFull.setPath("transportAirConFull");
        transportAirConFull.setValueDefinition(booleanValueType);
        transportAirConFull.setChoices("false,true");
        transportAirConFull.setValue("false");
        transportAirConFull.setFromData(false);
        transportAirConFull.setFromProfile(true);

        ItemValueDefinition transportEcoDriving = new ItemValueDefinition(transportItemDefinition);
        transportEcoDriving.setName("Eco Driving");
        transportEcoDriving.setPath("transportEcoDriving");
        transportEcoDriving.setValueDefinition(booleanValueType);
        transportEcoDriving.setChoices("false,true");
        transportEcoDriving.setValue("false");
        transportEcoDriving.setFromData(false);
        transportEcoDriving.setFromProfile(true);

        ItemValueDefinition transportAdhereToSpeedLimit = new ItemValueDefinition(transportItemDefinition);
        transportAdhereToSpeedLimit.setName("Adhere To Speed Limit");
        transportAdhereToSpeedLimit.setPath("transportAdhereToSpeedLimit");
        transportAdhereToSpeedLimit.setValueDefinition(booleanValueType);
        transportAdhereToSpeedLimit.setChoices("false,true");
        transportAdhereToSpeedLimit.setValue("false");
        transportAdhereToSpeedLimit.setFromData(false);
        transportAdhereToSpeedLimit.setFromProfile(true);

        String transportAlgorithm = "\n" +
                sanitiseStringJSFunction +
                "if (transportKgCO2PerKm == 0 && " +
                "    transportKgCO2PerPassengerJourney == 0) {\n" +
                "  if (sanitiseString(transportSize) == \"fuelconsumption\") {\n" +
                "    if (sanitiseString(transportFuel) == \"diesel\") {\n" +
                "      (2.63 * transportDistance) / transportKmPerLitre;\n" +
                "    } else {\n" +
                "      (2.3 * transportDistance) / transportKmPerLitre;\n" +
                "    }\n" +
                "  } else {\n" +
                "    if (sanitiseString(transportFuel) == \"diesel\") {\n" +
                "      1.15 * (2.63 * transportDistance) / transportKmPerLitre;\n" +
                "    } else {\n" +
                "      1.15 * (2.3 * transportDistance) / transportKmPerLitre;\n" +
                "    }\n" +
                "  }\n" +
                "} else if (transportKgCO2PerKm != 0) {\n" +
                "  var multiplier = 0;\n" +
                "  if (transportTyresUnderInflated == \"true\") {\n" +
                "    multiplier = multiplier + 1;\n" +
                "  }\n" +
                "  if (transportAirConAverage == \"true\") {\n" +
                "    multiplier = multiplier + 5;\n" +
                "  }\n" +
                "  if (transportAirConFull == \"true\") {\n" +
                "    multiplier = multiplier + 25;\n" +
                "  }\n" +
                "  if (transportEcoDriving == \"true\") {\n" +
                "    multiplier = multiplier - 10;\n" +
                "  }\n" +
                "  if (transportAdhereToSpeedLimit == \"true\") {\n" +
                "    multiplier = multiplier - 25;\n" +
                "  }\n" +
                "\n" +
                "  if (multiplier == 0) {\n" +
                "    multiplier = 1;\n" +
                "  } else if (multiplier > 0) {\n" +
                "    multiplier = 1 + (multiplier * 0.01);\n" +
                "  } else if (multiplier < 0) {\n" +
                "    multiplier = 1 - (multiplier * -0.01);\n" +
                "  }" +
                "\n" +
                "  transportKgCO2PerKm * transportDistance * multiplier;\n" +
                "} else if (transportKgCO2PerPassengerJourney != 0) {\n" +
                "  transportKgCO2PerPassengerJourney * transportNumberOfJourneys;\n" +
                "}";
        Algorithm transportKgCO2PerMonthCA = new Algorithm(transportItemDefinition, transportAlgorithm);
        transportKgCO2PerMonthCA.setName("perMonth");
        return transportItemDefinition;
    }

    private ItemDefinition createTVID() {
        // TV DEFINITION

        ItemDefinition televisionItemDefinition = new ItemDefinition(environment, "Television");
        entityManager.persist(televisionItemDefinition);
        televisionItemDefinition.setDrillDown("tvsTV,tvsType,tvsSize");

        ItemValueDefinition tvsTV = new ItemValueDefinition(televisionItemDefinition);
        tvsTV.setName("Television");
        tvsTV.setPath("tvsTV");
        tvsTV.setValueDefinition(textValueType);
        tvsTV.setChoices("tv1,tv2,tv3,tv4");
        tvsTV.setValue("tv1");
        tvsTV.setFromData(true);
        tvsTV.setFromProfile(false);

        ItemValueDefinition tvsType = new ItemValueDefinition(televisionItemDefinition);
        tvsType.setName("Type");
        tvsType.setPath("tvsType");
        tvsType.setValueDefinition(textValueType);
        tvsType.setChoices("Flat screen,LCD,Plasma,Rear Projection,Standard");
        tvsType.setValue("Standard");
        tvsType.setFromData(true);
        tvsType.setFromProfile(false);

        ItemValueDefinition tvsSize = new ItemValueDefinition(televisionItemDefinition);
        tvsSize.setName("Size");
        tvsSize.setPath("tvsSize");
        tvsSize.setValueDefinition(textValueType);
        tvsSize.setValue("28");
        tvsSize.setFromData(true);
        tvsSize.setFromProfile(false);

        ItemValueDefinition tvsValue = new ItemValueDefinition(televisionItemDefinition);
        tvsValue.setName("kW");
        tvsValue.setPath("tvsValue");
        tvsValue.setValueDefinition(kWValueType);
        tvsValue.setValue("0.29");
        tvsValue.setFromData(true);
        tvsValue.setFromProfile(false);

        ItemValueDefinition tvsSource = new ItemValueDefinition(televisionItemDefinition);
        tvsSource.setName("Source");
        tvsSource.setPath("tvsSource");
        tvsSource.setValueDefinition(textValueType);
        tvsSource.setValue("defra/dgen 200");
        tvsSource.setFromData(true);
        tvsSource.setFromProfile(false);

        ItemValueDefinition tvsHours = new ItemValueDefinition(televisionItemDefinition);
        tvsHours.setName("Hours watched");
        tvsHours.setPath("tvsHours");
        tvsHours.setValueDefinition(quantityValueType);
        tvsHours.setValue("0");
        tvsHours.setFromData(false);
        tvsHours.setFromProfile(true);

        Algorithm televisionKwPerMonthCA = new Algorithm(televisionItemDefinition, "tvsValue*tvsHours*0.527");
        televisionKwPerMonthCA.setName("perMonth");
        return televisionItemDefinition;
    }

    private ItemDefinition createSeasonalBillingID() {
        // SEASONAL BILLING DEFINITION

        ItemDefinition seasonBillingItemDefinition = new ItemDefinition(environment, "Seasonal Billing");
        entityManager.persist(seasonBillingItemDefinition);
        seasonBillingItemDefinition.setDrillDown("seasonName,seasonEnergyType");

        ItemValueDefinition seasonName = new ItemValueDefinition(seasonBillingItemDefinition);
        seasonName.setName("Season");
        seasonName.setPath("seasonName");
        seasonName.setValueDefinition(textValueType);
        seasonName.setChoices("spring,summer,autumn,winter");
        seasonName.setValue("summer");
        seasonName.setFromData(true);
        seasonName.setFromProfile(false);

        ItemValueDefinition seasonEnergyType = new ItemValueDefinition(seasonBillingItemDefinition);
        seasonEnergyType.setName("Energy Type");
        seasonEnergyType.setPath("seasonEnergyType");
        seasonEnergyType.setValueDefinition(textValueType);
        seasonEnergyType.setChoices("electricity,electricityWithHeating,gas,oil,solidFuel,");
        seasonEnergyType.setValue("gas");
        seasonEnergyType.setFromData(true);
        seasonEnergyType.setFromProfile(false);

        ItemValueDefinition seasonPercentage = new ItemValueDefinition(seasonBillingItemDefinition);
        seasonPercentage.setName("Percentage");
        seasonPercentage.setPath("seasonPercentage");
        seasonPercentage.setValueDefinition(quantityValueType);
        seasonPercentage.setValue("0.25");
        seasonPercentage.setFromData(true);
        seasonPercentage.setFromProfile(false);

        ItemValueDefinition seasonSource = new ItemValueDefinition(seasonBillingItemDefinition);
        seasonSource.setName("Source");
        seasonSource.setPath("seasonSource");
        seasonSource.setValueDefinition(textValueType);
        seasonSource.setValue("EST/defra2007");
        seasonSource.setFromData(true);
        seasonSource.setFromProfile(false);

        ItemValueDefinition kWhPerQuarter = new ItemValueDefinition(seasonBillingItemDefinition);
        kWhPerQuarter.setName("kWh Per Quarter");
        kWhPerQuarter.setPath("kWhPerQuarter");
        kWhPerQuarter.setValueDefinition(kWValueType);
        kWhPerQuarter.setValue("0");
        kWhPerQuarter.setFromData(false);
        kWhPerQuarter.setFromProfile(true);

        Algorithm seasonBillingKwPerMonthCA = new Algorithm(seasonBillingItemDefinition, "(kWhPerQuarter/seasonPercentage)/12");
        seasonBillingKwPerMonthCA.setName("perMonth");
        return seasonBillingItemDefinition;
    }

    private ItemDefinition createFuelID() {
        // FUEL DEFINITION

        ItemDefinition fuelItemDefinition = new ItemDefinition(environment, "Fuel");
        entityManager.persist(fuelItemDefinition);
        fuelItemDefinition.setDrillDown("fuelType");

        ItemValueDefinition fuelType = new ItemValueDefinition(fuelItemDefinition);
        fuelType.setName("Type");
        fuelType.setPath("fuelType");
        fuelType.setValueDefinition(textValueType);
        fuelType.setChoices("Biodiesel,Biomass,Coal,Diesel,Electricity,Gas,Kerosene,LPG,Oil,Petrol");
        fuelType.setValue("Gas");
        fuelType.setFromData(true);
        fuelType.setFromProfile(false);

        ItemValueDefinition fuelKgCO2PerKWh = new ItemValueDefinition(fuelItemDefinition);
        fuelKgCO2PerKWh.setName("kgCO2 Per KWh");
        fuelKgCO2PerKWh.setPath("fuelKgCO2PerKWh");
        fuelKgCO2PerKWh.setValueDefinition(kgCO2PerKWhValueType);
        fuelKgCO2PerKWh.setValue("0.30");
        fuelKgCO2PerKWh.setFromData(true);
        fuelKgCO2PerKWh.setFromProfile(false);
        fuelKgCO2PerKWh.setAllowedRoles("dataAdmin");

        ItemValueDefinition fuelKgCO2PerLitre = new ItemValueDefinition(fuelItemDefinition);
        fuelKgCO2PerLitre.setName("kgCO2 Per Litre");
        fuelKgCO2PerLitre.setPath("fuelKgCO2PerLitre");
        fuelKgCO2PerLitre.setValueDefinition(kgCO2PerLitreValueType);
        fuelKgCO2PerLitre.setValue("2.29");
        fuelKgCO2PerLitre.setFromData(true);
        fuelKgCO2PerLitre.setFromProfile(false);
        fuelKgCO2PerLitre.setAllowedRoles("dataAdmin");

        ItemValueDefinition fuelKgCO2perKg = new ItemValueDefinition(fuelItemDefinition);
        fuelKgCO2perKg.setName("kgCO2 Per Kg");
        fuelKgCO2perKg.setPath("fuelKgCO2perKg");
        fuelKgCO2perKg.setValueDefinition(kgCO2PerKgValueType);
        fuelKgCO2perKg.setValue("0");
        fuelKgCO2perKg.setFromData(true);
        fuelKgCO2perKg.setFromProfile(false);
        fuelKgCO2perKg.setAllowedRoles("dataAdmin");

        ItemValueDefinition fuelSource = new ItemValueDefinition(fuelItemDefinition);
        fuelSource.setName("Source");
        fuelSource.setPath("fuelSource");
        fuelSource.setValueDefinition(textValueType);
        fuelSource.setValue("DEFRA 2005");
        fuelSource.setFromData(true);
        fuelSource.setFromProfile(false);

        ItemValueDefinition fuelUsage = new ItemValueDefinition(fuelItemDefinition);
        fuelUsage.setName("Usage");
        fuelUsage.setPath("fuelUsage");
        fuelUsage.setValueDefinition(quantityValueType);
        fuelUsage.setValue("0");
        fuelUsage.setFromData(false);
        fuelUsage.setFromProfile(true);

        String fuelAlgorithm = "\n" +
                "if (fuelKgCO2PerLitre != 0) {\n" +
                "  fuelUsage * fuelKgCO2PerLitre;\n" +
                "} else if (fuelKgCO2PerKWh != 0) {\n" +
                "  fuelUsage * fuelKgCO2PerKWh;\n" +
                "} else if (fuelKgCO2perKg != 0) {\n" +
                "  fuelUsage * fuelKgCO2perKg;\n" +
                "} else {\n" +
                "  0;\n" +
                "}";

        Algorithm fuelKgCO2PerMonthCA = new Algorithm(fuelItemDefinition, fuelAlgorithm);
        fuelKgCO2PerMonthCA.setName("perMonth");
        return fuelItemDefinition;
    }

    private ItemDefinition createFuelPricesID() {
        // FUEL PRICES DEFINITION

        ItemDefinition fuelPriceItemDefinition = new ItemDefinition(environment, "Fuel Price");
        entityManager.persist(fuelPriceItemDefinition);
        fuelPriceItemDefinition.setDrillDown("fuelPriceType,fuelPricePayment");

        ItemValueDefinition fuelPriceType = new ItemValueDefinition(fuelPriceItemDefinition);
        fuelPriceType.setName("Type");
        fuelPriceType.setPath("fuelPriceType");
        fuelPriceType.setValueDefinition(textValueType);
        fuelPriceType.setChoices("Biomass,Coal,Gas,Electricity,LPG,Oil");
        fuelPriceType.setValue("Gas");
        fuelPriceType.setFromData(true);
        fuelPriceType.setFromProfile(false);

        ItemValueDefinition fuelPricePayment = new ItemValueDefinition(fuelPriceItemDefinition);
        fuelPricePayment.setName("Payment");
        fuelPricePayment.setPath("fuelPricePayment");
        fuelPricePayment.setValueDefinition(textValueType);
        fuelPricePayment.setChoices("normal,prepayment");
        fuelPricePayment.setValue("normal");
        fuelPricePayment.setFromData(true);
        fuelPricePayment.setFromProfile(false);

        ItemValueDefinition fuelKgCO2PerPound = new ItemValueDefinition(fuelPriceItemDefinition);
        fuelKgCO2PerPound.setName("kgCO2 per £");
        fuelKgCO2PerPound.setPath("fuelKgCO2PerPound");
        fuelKgCO2PerPound.setValueDefinition(kgCO2PerPoundValueType);
        fuelKgCO2PerPound.setValue("0");
        fuelKgCO2PerPound.setFromData(true);
        fuelKgCO2PerPound.setFromProfile(false);
        fuelKgCO2PerPound.setAllowedRoles("dataAdmin");

        ItemValueDefinition fuelPriceSource = new ItemValueDefinition(fuelPriceItemDefinition);
        fuelPriceSource.setName("Source");
        fuelPriceSource.setPath("fuelPriceSource");
        fuelPriceSource.setValueDefinition(textValueType);
        fuelPriceSource.setValue("http://www.dti.gov.uk/energy/statistics/publications/prices/tables/page18125.html");
        fuelPriceSource.setFromData(true);
        fuelPriceSource.setFromProfile(false);

        ItemValueDefinition fuelPrice = new ItemValueDefinition(fuelPriceItemDefinition);
        fuelPrice.setName("Price");
        fuelPrice.setPath("fuelPrice");
        fuelPrice.setValueDefinition(quantityValueType);
        fuelPrice.setValue("0");
        fuelPrice.setFromData(false);
        fuelPrice.setFromProfile(true);

        Algorithm fuelPriceCostPerMonthCA = new Algorithm(fuelPriceItemDefinition, "fuelPrice*fuelKgCO2PerPound");
        fuelPriceCostPerMonthCA.setName("perMonth");
        return fuelPriceItemDefinition;
    }

    private ItemDefinition createCarID() {

        ItemDefinition carItemDefinition = new ItemDefinition(environment, "Car");
        entityManager.persist(carItemDefinition);

        ItemValueDefinition carManufacturer = new ItemValueDefinition(carItemDefinition);
        carManufacturer.setName("Manufacturer");
        carManufacturer.setPath("carManufacturer");
        carManufacturer.setValueDefinition(textValueType);
        carManufacturer.setValue("FORD");
        carManufacturer.setFromData(true);
        carManufacturer.setFromProfile(false);

        ItemValueDefinition carModel = new ItemValueDefinition(carItemDefinition);
        carModel.setName("Model");
        carModel.setPath("carModel");
        carModel.setValueDefinition(textValueType);
        carModel.setValue("Mondeo");
        carModel.setFromData(true);
        carModel.setFromProfile(false);

        ItemValueDefinition carDescription = new ItemValueDefinition(carItemDefinition);
        carDescription.setName("Description");
        carDescription.setPath("carDescription");
        carDescription.setValueDefinition(textValueType);
        carDescription.setValue("");
        carDescription.setFromData(true);
        carDescription.setFromProfile(false);

        ItemValueDefinition carTransmission = new ItemValueDefinition(carItemDefinition);
        carTransmission.setName("Transmission");
        carTransmission.setPath("carTransmission");
        carTransmission.setValueDefinition(textValueType);
        carTransmission.setValue("M5");
        carTransmission.setFromData(true);
        carTransmission.setFromProfile(false);

        ItemValueDefinition carEngineCapacity = new ItemValueDefinition(carItemDefinition);
        carEngineCapacity.setName("Engine Capacity");
        carEngineCapacity.setPath("carEngineCapacity");
        carEngineCapacity.setValueDefinition(textValueType);
        carEngineCapacity.setValue("1595");
        carEngineCapacity.setFromData(true);
        carEngineCapacity.setFromProfile(false);

        ItemValueDefinition carFuelType = new ItemValueDefinition(carItemDefinition);
        carFuelType.setName("Fuel Type");
        carFuelType.setPath("carFuelType");
        carFuelType.setValueDefinition(textValueType);
        carFuelType.setValue("Petrol");
        carFuelType.setFromData(true);
        carFuelType.setFromProfile(false);

        ItemValueDefinition carUrbanColdLitresPerKm = new ItemValueDefinition(carItemDefinition);
        carUrbanColdLitresPerKm.setName("Urban Cold Litres Per Km");
        carUrbanColdLitresPerKm.setPath("carUrbanColdLitresPerKm");
        carUrbanColdLitresPerKm.setValueDefinition(litresPerKmValueType);
        carUrbanColdLitresPerKm.setValue("0.11");
        carUrbanColdLitresPerKm.setFromData(true);
        carUrbanColdLitresPerKm.setFromProfile(false);

        ItemValueDefinition carExtraUrbanLitresPerKm = new ItemValueDefinition(carItemDefinition);
        carExtraUrbanLitresPerKm.setName("Extra Urban Litres Per Km");
        carExtraUrbanLitresPerKm.setPath("carExtraUrbanLitresPerKm");
        carExtraUrbanLitresPerKm.setValueDefinition(litresPerKmValueType);
        carExtraUrbanLitresPerKm.setValue("0.06");
        carExtraUrbanLitresPerKm.setFromData(true);
        carExtraUrbanLitresPerKm.setFromProfile(false);

        ItemValueDefinition carCombineLitresPerKm = new ItemValueDefinition(carItemDefinition);
        carCombineLitresPerKm.setName("Combine Litres Per Km");
        carCombineLitresPerKm.setPath("carCombineLitresPerKm");
        carCombineLitresPerKm.setValueDefinition(litresPerKmValueType);
        carCombineLitresPerKm.setValue("0.08");
        carCombineLitresPerKm.setFromData(true);
        carCombineLitresPerKm.setFromProfile(false);

        ItemValueDefinition carKgCO2PerKm = new ItemValueDefinition(carItemDefinition);
        carKgCO2PerKm.setName("kgCO2 Per Km");
        carKgCO2PerKm.setPath("carKgCO2PerKm");
        carKgCO2PerKm.setValueDefinition(kgCO2PerKmValueType);
        carKgCO2PerKm.setValue("0.20");
        carKgCO2PerKm.setFromData(true);
        carKgCO2PerKm.setFromProfile(false);
        carKgCO2PerKm.setAllowedRoles("dataAdmin");

        Algorithm carKgCO2PerMonthCA = new Algorithm(carItemDefinition, "carKgCO2PerKm*user_distance*user_multipliers");
        carKgCO2PerMonthCA.setName("perMonth");

        return carItemDefinition;
    }

    private DataCategory createCats() {
        DataCategory rootCat = new DataCategory(environment, "Root", "");
        entityManager.persist(rootCat);
        metadataCat = new DataCategory(rootCat, "Metadata", "metadata", metadataItemDefinition);
        entityManager.persist(metadataCat);
        DataCategory homeCat = new DataCategory(rootCat, "Home", "home");
        entityManager.persist(homeCat);
        DataCategory appliancesCat = new DataCategory(homeCat, "Appliances", "appliances", appliancesItemDefinition);
        entityManager.persist(appliancesCat);
        DataCategory televisionCat = new DataCategory(homeCat, "Television", "television", televisionItemDefinition);
        entityManager.persist(televisionCat);
        DataCategory heatingCat = new DataCategory(homeCat, "Heating", "heating", heatingItemDefinition);
        entityManager.persist(heatingCat);
        DataCategory cookingCat = new DataCategory(homeCat, "Cooking", "cooking", cookingItemDefinition);
        entityManager.persist(cookingCat);
        DataCategory lightingCat = new DataCategory(homeCat, "Lighting", "lighting", lightingItemDefinition);
        entityManager.persist(lightingCat);
        DataCategory seasonalBillingCat = new DataCategory(homeCat, "Seasonal Billing", "seasonal_billing", seasonBillingItemDefinition);
        entityManager.persist(seasonalBillingCat);
        DataCategory fuelCat = new DataCategory(homeCat, "Fuel", "fuel", fuelItemDefinition);
        entityManager.persist(fuelCat);
        DataCategory fuelPriceCat = new DataCategory(homeCat, "Fuel Price", "fuel_price", fuelPriceItemDefinition);
        entityManager.persist(fuelPriceCat);
        DataCategory transportCat = new DataCategory(rootCat, "Transport", "transport");
        entityManager.persist(transportCat);
        DataCategory subTransportCat = new DataCategory(transportCat, "Transport", "transport", transportItemDefinition);
        entityManager.persist(subTransportCat);
        entityManager.persist(rootCat);
        entityManager.flush();
        return metadataCat;
    }

    private void createMetadataDI() {
        DataItem metadataDI = new DataItem(metadataCat, metadataItemDefinition);
        metadataDI.setName("Metadata");
        metadataDI.setPath("metadata");
        entityManager.persist(metadataDI);
        entityManager.flush();
    }
}