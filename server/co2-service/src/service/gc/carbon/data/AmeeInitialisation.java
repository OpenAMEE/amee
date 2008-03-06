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

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.sheet.ValueType;
import gc.carbon.ValueDefinition;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.EntityManager;
import java.io.Serializable;

@Name("ameeInitialisation")
@Scope(ScopeType.EVENT)
public class AmeeInitialisation implements Serializable {

    private final static Logger log = Logger.getLogger(AmeeInitialisation.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private SiteService siteService;

    Environment environment;

    String sanitiseStringJSFunction;

    ValueDefinition count;
    ValueDefinition amount;
    ValueDefinition text;
    ValueDefinition kgCO2PerKm;
    ValueDefinition kWhPerYear;
    ValueDefinition kW;
    ValueDefinition kM;
    ValueDefinition kWh;
    ValueDefinition litre;
    ValueDefinition kg;
    ValueDefinition kWhPerCycle;
    ValueDefinition kgCO2PerJourney;
    ValueDefinition kgCO2PerKWh;
    ValueDefinition kgCO2PerLitre;
    ValueDefinition kgCO2PerPound;
    ValueDefinition currencyGBP;
    ValueDefinition kgCO2PerKg;
    ValueDefinition kgCO2PerYear;
    ValueDefinition percentage;

    public AmeeInitialisation() {
        super();
    }

    public Environment initialise() {

        // Environment essentials
        environment = createEnvironment();
        createValueTypes();
        createJSFunctions();

        // Data Categories
        createCats();

        return environment;
    }

    private void createCats() {

        DataCategory root;
        DataCategory lev1;
        DataCategory lev2;
        DataCategory lev3;
        DataCategory lev4;

        // Root
        root = new DataCategory(environment, "Root", "");
        entityManager.persist(root);
        // Home
        lev1 = new DataCategory(root, "Home", "home");
        entityManager.persist(lev1);
        {
            // Appliances
            lev2 = new DataCategory(lev1, "Appliances", "appliances");
            entityManager.persist(lev2);
            {
                // Computers
                lev3 = new DataCategory(lev2, "Computers", "computers");
                entityManager.persist(lev3);
                {
                    // Generic
                    lev4 = new DataCategory(lev3, "Generic", "generic", createComputersGeneric());
                    entityManager.persist(lev4);
                }
                // Cooking
                lev3 = new DataCategory(lev2, "Cooking", "cooking", createCooking());
                entityManager.persist(lev3);
                // Entertainment
                lev3 = new DataCategory(lev2, "Entertainment", "entertainment");
                entityManager.persist(lev3);
                {
                    // Generic
                    lev4 = new DataCategory(lev3, "Generic", "generic", createEntertainmentGeneric());
                    entityManager.persist(lev4);
                }
                // Kitchen
                lev3 = new DataCategory(lev2, "Kitchen", "kitchen");
                entityManager.persist(lev3);
                {
                    // Generic
                    lev4 = new DataCategory(lev3, "Generic", "generic", createKitchenGeneric());
                    entityManager.persist(lev4);
                }
                // Televisions
                lev3 = new DataCategory(lev2, "Televisions", "televisions");
                entityManager.persist(lev3);
                {
                    // Generic
                    lev4 = new DataCategory(lev3, "Generic", "generic", createTelevisionsGeneric());
                    entityManager.persist(lev4);
                }
            }
            // Energy
            lev2 = new DataCategory(lev1, "Energy", "energy");
            entityManager.persist(lev2);
            {
                // Electricity
                lev3 = new DataCategory(lev2, "Electricity", "electricity", createElectricity());
                entityManager.persist(lev3);
                // Quantity
                lev3 = new DataCategory(lev2, "Quantity", "quantity", createEnergyQuantity());
                entityManager.persist(lev3);
                // UK
                lev3 = new DataCategory(lev2, "UK", "uk");
                entityManager.persist(lev3);
                {
                    // Price
                    lev4 = new DataCategory(lev3, "Price", "price", createEnergyUkPrice());
                    entityManager.persist(lev4);
                    // Seasonal
                    lev4 = new DataCategory(lev3, "Seasonal", "seasonal", createEnergyUkSeasonal());
                    entityManager.persist(lev4);
                    // suppliers
                    lev4 = new DataCategory(lev3, "Suppliers", "suppliers", createEnergyUkSuppliers());
                    entityManager.persist(lev4);
                }
            }
            // Heating
            lev2 = new DataCategory(lev1, "Heating", "heating", createHeating());
            entityManager.persist(lev2);
            // Lighting
            lev2 = new DataCategory(lev1, "Lighting", "lighting", createLighting());
            entityManager.persist(lev2);
        }
        // Personal
        lev1 = new DataCategory(root, "Personal", "personal", createPersonal());
        entityManager.persist(lev1);
        // Transport
        lev1 = new DataCategory(root, "Transport", "transport");
        entityManager.persist(lev1);
        {
            // Bus
            lev2 = new DataCategory(lev1, "Bus", "bus");
            entityManager.persist(lev2);
            {
                // Generic
                lev3 = new DataCategory(lev2, "Generic", "generic", createBusGeneric());
                entityManager.persist(lev3);
            }
            // Car
            lev2 = new DataCategory(lev1, "Car", "car");
            entityManager.persist(lev2);
            {
                // Generic
                lev3 = new DataCategory(lev2, "Generic", "generic", createCarGeneric());
                entityManager.persist(lev3);
                // Specific
                lev3 = new DataCategory(lev2, "Specific", "specific", createCarSpecific());
                entityManager.persist(lev3);
            }
            // Motorcycle
            lev2 = new DataCategory(lev1, "Motorcycle", "motorcycle");
            entityManager.persist(lev2);
            {
                // Generic
                lev3 = new DataCategory(lev2, "Generic", "generic", createMotorcycleGeneric());
                entityManager.persist(lev3);
            }
            // Other
            lev2 = new DataCategory(lev1, "Other", "Other", createTransportOther());
            entityManager.persist(lev2);
            // Plane
            lev2 = new DataCategory(lev1, "Plane", "plane");
            entityManager.persist(lev2);
            {
                // Generic
                lev3 = new DataCategory(lev2, "Generic", "generic", createPlaneGeneric());
                entityManager.persist(lev3);
            }
            // Taxi
            lev2 = new DataCategory(lev1, "Taxi", "taxi");
            entityManager.persist(lev2);
            {
                // Generic
                lev3 = new DataCategory(lev2, "Generic", "generic", createTaxiGeneric());
                entityManager.persist(lev3);
            }
            // Train
            lev2 = new DataCategory(lev1, "Train", "train");
            entityManager.persist(lev2);
            {
                // Generic
                lev3 = new DataCategory(lev2, "Generic", "generic", createTrainGeneric());
                entityManager.persist(lev3);
            }
        }
        // Metadata
        ItemDefinition id = createProfileMetadata();
        lev1 = new DataCategory(root, "Metadata", "metadata", id);
        entityManager.persist(lev1);

        // save everything
        entityManager.flush();

        // Metadata DI
        DataItem metadataDI = new DataItem(lev1, id);
        metadataDI.setName("Metadata");
        metadataDI.setPath("metadata");
        entityManager.persist(metadataDI);

        // save everything
        entityManager.flush();
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
        environment.setName("AMEE");
        entityManager.persist(environment);
        entityManager.flush();
        return environment;
    }

    private void createValueTypes() {
        entityManager.persist(count = new ValueDefinition(environment, "count", ValueType.INTEGER));
        entityManager.persist(amount = new ValueDefinition(environment, "amount", ValueType.DECIMAL));
        entityManager.persist(text = new ValueDefinition(environment, "text", ValueType.TEXT));
        entityManager.persist(kgCO2PerKm = new ValueDefinition(environment, "kgCO2PerKm", ValueType.DECIMAL));
        entityManager.persist(kWhPerYear = new ValueDefinition(environment, "kWhPerYear", ValueType.DECIMAL));
        entityManager.persist(kW = new ValueDefinition(environment, "kW", ValueType.DECIMAL));
        entityManager.persist(kWh = new ValueDefinition(environment, "kWh", ValueType.DECIMAL));
        entityManager.persist(kM = new ValueDefinition(environment, "kM", ValueType.DECIMAL));
        entityManager.persist(litre = new ValueDefinition(environment, "litre", ValueType.DECIMAL));
        entityManager.persist(kg = new ValueDefinition(environment, "kg", ValueType.DECIMAL));
        entityManager.persist(kWhPerCycle = new ValueDefinition(environment, "kWhPerCycle", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerJourney = new ValueDefinition(environment, "kgCO2PerJourney", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerKWh = new ValueDefinition(environment, "kgCO2PerKWh", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerLitre = new ValueDefinition(environment, "kgCO2PerLitre", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerPound = new ValueDefinition(environment, "kgCO2PerPound", ValueType.DECIMAL));
        entityManager.persist(currencyGBP = new ValueDefinition(environment, "currencyGBP", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerKg = new ValueDefinition(environment, "kgCO2PerKg", ValueType.DECIMAL));
        entityManager.persist(kgCO2PerYear = new ValueDefinition(environment, "kgCO2PerYear", ValueType.DECIMAL));
        entityManager.persist(percentage = new ValueDefinition(environment, "percentage", ValueType.DECIMAL));
        entityManager.flush();
    }

    private ItemDefinition createComputersGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Computers Generic");
        id.setDrillDown("device,rating");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Device");
        ivd.setPath("device");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Rating");
        ivd.setPath("rating");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("KWh Per Year");
        ivd.setPath("kWhPerYear");
        ivd.setValueDefinition(kWhPerYear);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Number Owned");
        ivd.setPath("numberOwned");
        ivd.setValueDefinition(count);
        ivd.setValue("1");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "(kWhPerYear * numberOwned * 0.527) / 12");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createCooking() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Cooking");
        entityManager.persist(id);
        id.setDrillDown("numberOfPeople,fuel");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Number Of People");
        ivd.setPath("numberOfPeople");
        ivd.setValueDefinition(count);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Fuel");
        ivd.setPath("fuel");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Year");
        ivd.setPath("kgCO2PerYear");
        ivd.setValueDefinition(kgCO2PerYear);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        al = new Algorithm(id, "kgCO2PerYear / 12");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createEntertainmentGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Entertainment Generic");
        id.setDrillDown("device,rating");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Device");
        ivd.setPath("device");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Rating");
        ivd.setPath("rating");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("KWh Per Year");
        ivd.setPath("kWhPerYear");
        ivd.setValueDefinition(kWhPerYear);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Number Owned");
        ivd.setPath("numberOwned");
        ivd.setValueDefinition(count);
        ivd.setValue("1");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "numberOwned * kWhPerYear * 0.527");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createKitchenGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Kitchen Generic");
        id.setDrillDown("device,rating,age,temperature");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Device");
        ivd.setPath("device");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Rating");
        ivd.setPath("rating");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Age");
        ivd.setPath("age");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Temperature");
        ivd.setPath("temperature");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("KWh Per Year");
        ivd.setPath("kWhPerYear");
        ivd.setValueDefinition(kWhPerYear);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("KWh Per Cycle");
        ivd.setPath("kWhPerCycle");
        ivd.setValueDefinition(kWhPerCycle);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Cycles Per Month");
        ivd.setPath("cyclesPerMonth");
        ivd.setValueDefinition(amount);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "if (kWhPerYear != 0) {\n" +
                "  (kWhPerYear * 0.527) / 12;\n" +
                "} else if (kWhPerCycle != 0) {\n" +
                "  cyclesPerMonth * kWhPerCycle * 0.527;\n" +
                "} else {\n" +
                "  0;\n" +
                "}");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createTelevisionsGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Televisions Generic");
        id.setDrillDown("type,size");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Size");
        ivd.setPath("size");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kW");
        ivd.setPath("kW");
        ivd.setValueDefinition(kW);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Hours Per Month");
        ivd.setPath("hoursPerMonth");
        ivd.setValueDefinition(amount);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kW * hoursPerMonth * 0.527");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createElectricity() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Electricity");
        id.setDrillDown("country");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Country");
        ivd.setPath("country");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per KWh");
        ivd.setPath("kgCO2PerKWh");
        ivd.setValueDefinition(kgCO2PerKWh);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kWh Per Month");
        ivd.setPath("kWhPerMonth");
        ivd.setValueDefinition(kWh);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kWhPerMonth * kgCO2PerKWh");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createEnergyQuantity() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Energy Quantity");
        id.setDrillDown("type");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per KWh");
        ivd.setPath("kgCO2PerKWh");
        ivd.setValueDefinition(kgCO2PerKWh);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Litre");
        ivd.setPath("kgCO2PerLitre");
        ivd.setValueDefinition(kgCO2PerLitre);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Kg");
        ivd.setPath("kgCO2PerKg");
        ivd.setValueDefinition(kgCO2PerKg);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kWh Per Month");
        ivd.setPath("kWhPerMonth");
        ivd.setValueDefinition(kWh);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Litres Per Month");
        ivd.setPath("litresPerMonth");
        ivd.setValueDefinition(litre);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kg Per Month");
        ivd.setPath("kgPerMonth");
        ivd.setValueDefinition(kg);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "if (kgCO2PerKWh != 0) {\n" +
                "  kWhPerMonth * kgCO2PerKWh;\n" +
                "} else if (kgCO2PerLitre != 0) {\n" +
                "  litresPerMonth * kgCO2PerLitre;\n" +
                "} else if (kgCO2PerKg != 0) {\n" +
                "  kgPerMonth * kgCO2PerKg;\n" +
                "} else {\n" +
                "  0;\n" +
                "}");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createEnergyUkPrice() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Energy UK Price");
        id.setDrillDown("type,payment");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Payment");
        ivd.setPath("payment");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Pound");
        ivd.setPath("kgCO2PerPound");
        ivd.setValueDefinition(kgCO2PerPound);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Currency GBP Per Month");
        ivd.setPath("currencyGBPPerMonth");
        ivd.setValueDefinition(currencyGBP);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "currencyGBPPerMonth * kgCO2PerPound");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createEnergyUkSeasonal() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Energy UK Seasonal");
        id.setDrillDown("name,energy");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Name");
        ivd.setPath("name");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Energy");
        ivd.setPath("energy");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Percentage");
        ivd.setPath("percentage");
        ivd.setValueDefinition(percentage);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Usage Per Quarter");
        ivd.setPath("usagePerQuarter");
        ivd.setValueDefinition(amount);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "(usagePerQuarter / percentage) / 12");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createEnergyUkSuppliers() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Energy UK Suppliers");
        id.setDrillDown("supplier");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Supplier");
        ivd.setPath("supplier");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per KWh");
        ivd.setPath("kgCO2PerKWh");
        ivd.setValueDefinition(kgCO2PerKWh);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kWh Per Month");
        ivd.setPath("kWhPerMonth");
        ivd.setValueDefinition(kWh);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kgCO2PerKWh * kWhPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createHeating() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Heating");
        entityManager.persist(id);
        id.setDrillDown("homeDwellingType,homeNoOfBedrooms,homeFuel,homeAge,type");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Home Dwelling Type");
        ivd.setPath("homeDwellingType");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Number Of Bedrooms");
        ivd.setPath("homeNoOfBedrooms");
        ivd.setValueDefinition(count);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Home Fuel");
        ivd.setPath("homeFuel");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Home Age");
        ivd.setPath("homeAge");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Year");
        ivd.setPath("kgCO2PerYear");
        ivd.setValueDefinition(kgCO2PerYear);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        al = new Algorithm(id, "kgCO2PerYear / 12");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createLighting() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Lighting");
        entityManager.persist(id);
        id.setDrillDown("type");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setValue("normal");
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kWh Per Year");
        ivd.setPath("kWhPerYear");
        ivd.setValueDefinition(kWhPerYear);
        ivd.setValue("0");
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setValue("");
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Number Of Light Bulbs");
        ivd.setPath("noOfLightBulbs");
        ivd.setValueDefinition(count);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Number Of Low Energy Light Bulbs");
        ivd.setPath("noOfLowEnergyLightBulbs");
        ivd.setValueDefinition(count);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id,
                sanitiseStringJSFunction +
                        "if (sanitiseString(type) == \"normal\") {\n" +
                        "  (noOfLightBulbs * kWhPerYear * 0.527) / 12;\n" +
                        "} else {\n" +
                        "  (noOfLowEnergyLightBulbs * kWhPerYear * 0.527) / 12;\n" +
                        "}\n");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createPersonal() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Personal");
        entityManager.persist(id);
        id.setDrillDown("type");

        ivd = new ItemValueDefinition(id);
        ivd.setName("type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setValue("");
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Pound");
        ivd.setPath("kgCO2PerPound");
        ivd.setValueDefinition(this.kgCO2PerPound);
        ivd.setValue("0");
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Currency GBP Per Month");
        ivd.setPath("currencyGBPPerMonth");
        ivd.setValueDefinition(currencyGBP);
        ivd.setValue("0");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setValue("");
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        al = new Algorithm(id, "currencyGBPPerMonth*kgCO2PerPound");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createBusGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Bus Generic");
        entityManager.persist(id);
        id.setDrillDown("type");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Passenger Km");
        ivd.setPath("kgCO2PerKmPassenger");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Month");
        ivd.setPath("distanceKmPerMonth");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kgCO2PerKmPassenger * distanceKmPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createCarGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Car Generic");
        entityManager.persist(id);
        id.setDrillDown("fuel,size");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Fuel");
        ivd.setPath("fuel");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Size");
        ivd.setPath("size");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Km");
        ivd.setPath("kgCO2PerKm");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Month");
        ivd.setPath("distanceKmPerMonth");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "1.15 * kgCO2PerKm * distanceKmPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createCarSpecific() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Car Specific");
        id.setDrillDown("manufacturer,model,description,transmission,engineCapacity,fuelType");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Manufacturer");
        ivd.setPath("manufacturer");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Model");
        ivd.setPath("model");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Description");
        ivd.setPath("description");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Transmission");
        ivd.setPath("transmission");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Engine Capacity");
        ivd.setPath("engineCapacity");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Fuel Type");
        ivd.setPath("fuelType");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Km");
        ivd.setPath("kgCO2PerKm");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Month");
        ivd.setPath("distanceKmPerMonth");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "1.15 * kgCO2PerKm * distanceKmPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createMotorcycleGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Motorcycle Generic");
        entityManager.persist(id);
        id.setDrillDown("fuel,size");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Fuel");
        ivd.setPath("fuel");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Size");
        ivd.setPath("size");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Km");
        ivd.setPath("kgCO2PerKm");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Month");
        ivd.setPath("distanceKmPerMonth");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kgCO2PerKm * distanceKmPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createTransportOther() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Transport Other");
        entityManager.persist(id);
        id.setDrillDown("type");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Km");
        ivd.setPath("kgCO2PerKm");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Month");
        ivd.setPath("distanceKmPerMonth");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kgCO2PerKm * distanceKmPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createPlaneGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Plane Generic");
        entityManager.persist(id);
        id.setDrillDown("type,size");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Size");
        ivd.setPath("size");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Passenger Km");
        ivd.setPath("kgCO2PerPassengerKm");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Passenger Journey");
        ivd.setPath("kgCO2PerPassengerJourney");
        ivd.setValueDefinition(kgCO2PerJourney);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Year");
        ivd.setPath("distanceKmPerYear");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Journeys Per Year");
        ivd.setPath("journeysPerYear");
        ivd.setValueDefinition(count);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "if (kgCO2PerPassengerKm != 0) {\n" +
                "  (distanceKmPerYear * kgCO2PerPassengerKm) / 12;\n" +
                "} else if (kgCO2PerPassengerJourney != 0) {\n" +
                "  (journeysPerYear * kgCO2PerPassengerJourney) / 12;\n" +
                "} else {\n" +
                "  0;\n" +
                "}");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createTaxiGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Taxi Generic");
        entityManager.persist(id);
        id.setDrillDown("type");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Km");
        ivd.setPath("kgCO2PerKm");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Month");
        ivd.setPath("distanceKmPerMonth");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kgCO2PerKm * distanceKmPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createTrainGeneric() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Train Generic");
        entityManager.persist(id);
        id.setDrillDown("type");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Type");
        ivd.setPath("type");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("kgCO2 Per Passenger Km");
        ivd.setPath("kgCO2PerKmPassenger");
        ivd.setValueDefinition(kgCO2PerKm);
        ivd.setFromData(true);
        ivd.setFromProfile(false);
        ivd.setAllowedRoles("dataAdmin");

        ivd = new ItemValueDefinition(id);
        ivd.setName("Source");
        ivd.setPath("source");
        ivd.setValueDefinition(text);
        ivd.setFromData(true);
        ivd.setFromProfile(false);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Distance Km Per Month");
        ivd.setPath("distanceKmPerMonth");
        ivd.setValueDefinition(kM);
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "kgCO2PerKmPassenger * distanceKmPerMonth");
        al.setName("perMonth");

        return id;
    }

    private ItemDefinition createProfileMetadata() {

        ItemDefinition id;
        ItemValueDefinition ivd;
        Algorithm al;

        id = new ItemDefinition(environment, "Metadata");
        id.setDrillDown("");
        entityManager.persist(id);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Postcode");
        ivd.setPath("postcode");
        ivd.setValueDefinition(text);
        ivd.setChoices("");
        ivd.setValue("");
        ivd.setFromData(false);
        ivd.setFromProfile(true);

        ivd = new ItemValueDefinition(id);
        ivd.setName("People In Household");
        ivd.setPath("peopleInHousehold");
        ivd.setValueDefinition(count);
        ivd.setChoices("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20");
        ivd.setValue("1");
        ivd.setFromData(true);
        ivd.setFromProfile(true);

        ivd = new ItemValueDefinition(id);
        ivd.setName("Profile Type");
        ivd.setPath("profileType");
        ivd.setValueDefinition(text);
        ivd.setChoices("Individual=Individual,Household=Household");
        ivd.setValue("Individual");
        ivd.setFromData(true);
        ivd.setFromProfile(true);

        al = new Algorithm(id, "0");
        al.setName("perMonth");

        return id;
    }
}