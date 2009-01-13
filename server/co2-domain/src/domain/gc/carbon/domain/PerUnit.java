package gc.carbon.domain;

import org.joda.time.Duration;
import org.joda.time.format.ISOPeriodFormat;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.json.JSONObject;
import org.json.JSONException;

import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
import javax.measure.unit.NonSI;
import javax.measure.DecimalMeasure;

import com.jellymold.utils.domain.APIUtils;

import java.math.BigDecimal;

import gc.carbon.domain.profile.ProfileItem;

/*
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
public class PerUnit extends Unit {

    public static final PerUnit ONE = new PerUnit(javax.measure.unit.Unit.ONE);

    private String string;

    public static PerUnit valueOf(String unit) {
        return new PerUnit(javax.measure.unit.Unit.valueOf(unit));
    }

    public static PerUnit valueOf(Duration duration) {
        return new PerUnit(duration);
    }

    private PerUnit(javax.measure.unit.Unit unit) {
        super(unit);
        this.string = unit.toString();
    }

    private PerUnit(Duration duration) {
        super(MILLI(SECOND).times(duration.getMillis()));
        this.string = ISOPeriodFormat.standard().print(duration.toPeriod());
    }

    public BigDecimal convert(BigDecimal decimal, PerUnit targetUnit) {
        DecimalMeasure dm = DecimalMeasure.valueOf(decimal, toUnit());
        return dm.to(targetUnit.toUnit(), ProfileItem.CONTEXT).getValue();
    }
    public javax.measure.unit.Unit toUnit() {
        return unit;
    }

    public String toString() {
        return string;
    }
}
