package gc.carbon.domain;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.json.JSONObject;
import org.json.JSONException;
import com.jellymold.utils.domain.APIUtils;

import javax.measure.DecimalMeasure;
import java.math.BigDecimal;

import gc.carbon.domain.profile.ProfileItem;

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
public class CompoundUnit extends Unit {

    private PerUnit perUnit;

    public static Unit valueOf(Unit unit, PerUnit perUnit) {
        return new CompoundUnit(unit, perUnit);
    }

    private CompoundUnit(Unit unit, PerUnit perUnit) {
        super(unit.toUnit());
        this.perUnit = perUnit;
    }

    public BigDecimal convert(BigDecimal decimal, CompoundUnit targetUnit) {
        DecimalMeasure dm = DecimalMeasure.valueOf(decimal, toUnit());
        return dm.to(targetUnit.toUnit(), ProfileItem.CONTEXT).getValue();
    }

    public javax.measure.unit.Unit toUnit() {
        return unit.divide(perUnit.toUnit());
    }

    public void getElement(Element parent, Document document) {
        parent.appendChild(APIUtils.getElement(document, "Unit", unit.toString()));
        parent.appendChild(APIUtils.getElement(document, "PerUnit", perUnit.toString()));
    }

    public void getJSONObject(JSONObject parent) throws JSONException {
        parent.put("unit", unit.toString());
        parent.put("perUnit", perUnit.toString());
    }
}
