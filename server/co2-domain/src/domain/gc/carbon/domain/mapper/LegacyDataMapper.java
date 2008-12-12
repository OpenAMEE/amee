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
package gc.carbon.domain.mapper;

import java.util.HashMap;
import java.util.Map;

public class LegacyDataMapper {
    protected static final Map<String, String[]> LEGACY_2_CURRENT;
    protected static final Map<String, String[]> CURRENT_2_LEGACY;

    static {
         LEGACY_2_CURRENT = new HashMap<String, String[]>();
         LEGACY_2_CURRENT.put("currencyGBPPerMonth", new String[]{"currency","GBP","month"});
         LEGACY_2_CURRENT.put("currencyUSDPerMonth", new String[]{"currency","USD","month"});
         LEGACY_2_CURRENT.put("cyclesPerMonth", new String[]{"cycles","","month"});
         LEGACY_2_CURRENT.put("distanceKmPerMonth", new String[]{"distance","km","month"});
         LEGACY_2_CURRENT.put("distanceKmPerYear", new String[]{"distance","km","year"});
         LEGACY_2_CURRENT.put("hoursPerMonth", new String[]{"hours","","Month"});
         LEGACY_2_CURRENT.put("journeysPerYear", new String[]{"journeys","","year"});
         LEGACY_2_CURRENT.put("kgPerMonth", new String[]{"kg","","month"});
         LEGACY_2_CURRENT.put("kmPerLitre", new String[]{"km","","litre"});
         LEGACY_2_CURRENT.put("kmPerLitreOwn", new String[]{"km","","litre"});
         LEGACY_2_CURRENT.put("kWhPerMonth", new String[]{"kWh","","month"});
         LEGACY_2_CURRENT.put("kWhPerQuarter", new String[]{"kWh","","quarter"});
         LEGACY_2_CURRENT.put("litresPerMonth", new String[]{"litre","","month"});
         LEGACY_2_CURRENT.put("transportKmPerLitre", new String[]{"km","","litre"});
         LEGACY_2_CURRENT.put("usagePerQuarter", new String[]{"usage","","quarter"});

         CURRENT_2_LEGACY = new HashMap<String,String[]>();
         CURRENT_2_LEGACY.put("currencyGBPMonth", new String[]{"currencyGBPPerMonth","Currency GBP Per Month"});
         CURRENT_2_LEGACY.put("currencyUSDmonth", new String[]{"currencyUSDPerMonth","Currency USD Per Month"});
         CURRENT_2_LEGACY.put("cyclesmonth", new String[]{"cyclesPerMonth","Cycles Per Month"});
         CURRENT_2_LEGACY.put("distancekmmonth", new String[]{"distanceKmPerMonth","Distance Km Per Month"});
         CURRENT_2_LEGACY.put("distancekmyear", new String[]{"distanceKmPerYear","Distance Km Per Year"});
         CURRENT_2_LEGACY.put("hoursMonth", new String[]{"hoursPerMonth","Hours Per Month"});
         CURRENT_2_LEGACY.put("journeysyear", new String[]{"journeysPerYear","Journeys Per Year"});
         CURRENT_2_LEGACY.put("kgmonth", new String[]{"kgPerMonth","kg Per Month"});
         CURRENT_2_LEGACY.put("kmlitre", new String[]{"kmPerLitre","km per litre"});
         CURRENT_2_LEGACY.put("kmlitreOwn",new String[]{"kmPerLitreOwn","km per litre own"});
         CURRENT_2_LEGACY.put("kWhmonth", new String[]{"kWhPerMonth","kWh Per Month"});
         CURRENT_2_LEGACY.put("kWhquarter", new String[]{"kWhPerQuarter","kWh Per Quarter"});
         CURRENT_2_LEGACY.put("litremonth", new String[]{"litresPerMonth","Litres Per Month"});
         CURRENT_2_LEGACY.put("kmlitre", new String[]{"transportKmPerLitre","Kilometres per litre"});
         CURRENT_2_LEGACY.put("usagequarter", new String[]{"usagePerQuarter","Usage Per Quarter"});
    }

    public static String getLegacyPath(String path, String unit, String perUnit) {
        if (unit == null && perUnit == null)
            return null;

        unit = (unit == null) ? "" : unit;
        perUnit = (perUnit == null) ? "" : perUnit;
        if (CURRENT_2_LEGACY.containsKey(path+unit+perUnit)) {
            return CURRENT_2_LEGACY.get(path+unit+perUnit)[0];
        } else {
            return path + toTitleCase(unit) + "Per" + toTitleCase(perUnit);
        }
    }

    public static String getLegacyName(String path, String unit, String perUnit) {
        if (unit == null && perUnit == null)
            return null;

        unit = (unit == null) ? "" : unit;
        perUnit = (perUnit == null) ? "" : perUnit;
        if (CURRENT_2_LEGACY.containsKey(path+unit+perUnit)) {
            return CURRENT_2_LEGACY.get(path+unit+perUnit)[1];
        } else {
            return toTitleCase(path) + " " + toTitleCase(unit) + " Per " + toTitleCase(perUnit);
        }
    }

    public static boolean canMap(String legacyPath) {
        return LEGACY_2_CURRENT.containsKey(legacyPath);
    }

    public static String getCurrentPath(String legacyPath) {
        if (!canMap(legacyPath))
            return legacyPath;

        return LEGACY_2_CURRENT.get(legacyPath)[0];
    }

    public static String getUnit(String legacyPath) {
        if (!canMap(legacyPath))
            return null;
        return LEGACY_2_CURRENT.get(legacyPath)[1];
    }

    public static String getPerUnit(String legacyPath) {
        if (!canMap(legacyPath))
            return null;
        return LEGACY_2_CURRENT.get(legacyPath)[2];
    }

    private static String toTitleCase(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
