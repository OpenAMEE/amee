 package gc.carbon.profile;
    
import org.restlet.data.Form;

import java.util.Map;
import java.util.HashMap;

 /**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
public class ProfileForm extends Form {

    private static final Map<String, String[]> LEGACY_ITEM_VALUES;
    static {
      LEGACY_ITEM_VALUES = new HashMap<String, String[]>();
      LEGACY_ITEM_VALUES.put("currencyGBPPerMonth", new String[]{"currency","GBP","month"});
      LEGACY_ITEM_VALUES.put("currencyUSDPerMonth", new String[]{"currency","USD","month"});
      LEGACY_ITEM_VALUES.put("cyclesPerMonth", new String[]{"cycles","","month"});
      LEGACY_ITEM_VALUES.put("distanceKmPerMonth", new String[]{"distance","km","month"});
      LEGACY_ITEM_VALUES.put("distanceKmPerYear", new String[]{"distance","km","year"});
      LEGACY_ITEM_VALUES.put("hoursPerMonth", new String[]{"hours","","Month"});
      LEGACY_ITEM_VALUES.put("journeysPerYear", new String[]{"journeys","","year"});
      LEGACY_ITEM_VALUES.put("kgPerMonth", new String[]{"kg","","month"});
      LEGACY_ITEM_VALUES.put("kmPerLitre", new String[]{"km","","litre"});
      LEGACY_ITEM_VALUES.put("kmPerLitreOwn", new String[]{"km","","litre"});
      LEGACY_ITEM_VALUES.put("kWhPerMonth", new String[]{"km","","month"});
      LEGACY_ITEM_VALUES.put("kWhPerQuarter", new String[]{"kWh","","quarter"});
      LEGACY_ITEM_VALUES.put("litresPerMonth", new String[]{"litre","","month"});
      LEGACY_ITEM_VALUES.put("transportKmPerLitre", new String[]{"km","","litre"});
      LEGACY_ITEM_VALUES.put("usagePerQuarter", new String[]{"usage","","quarter"});
    }

    public ProfileForm(Form form) {
        super(form.getQueryString());
        // Read API version as a parameter - may move to a header
        if (form.getFirstValue("v","1.0").equals("1.0")) {
            mapLegacyParameters();
        }
    }

    private void mapLegacyParameters() {
        if (getNames().contains("validFrom")) {
            add("startDate",getFirstValue("validFrom"));
            removeFirst("validFrom");   
        }
        for (String name : getNames()) {
            if (LEGACY_ITEM_VALUES.containsKey(name)) {
                String[] legacyItemValueMapping = LEGACY_ITEM_VALUES.get(name);
                add(legacyItemValueMapping[0],getFirstValue(name));
                add(legacyItemValueMapping[0]+"Unit",legacyItemValueMapping[1]);
                add(legacyItemValueMapping[0]+"PerUnit",legacyItemValueMapping[2]);
                removeFirst(name);
            }
        }
    }
}
