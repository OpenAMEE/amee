package gc.carbon.domain.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gc.carbon.domain.AMEECompoundUnit;
import gc.carbon.domain.AMEEPerUnit;
import gc.carbon.domain.AMEEUnit;

import javax.measure.DecimalMeasure;

/**
 * An AMEE abstraction of a decimal value.
 *
 * Provides for basic string-to-decimal validation, unit conversion and scale and precision definitions.
 *
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
public class Decimal {

    private final Log log = LogFactory.getLog(getClass());

    // Precision, Scale and MathContext properties
    public final static int PRECISION = 21;
    public final static int SCALE = 6;
    public final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final MathContext CONTEXT = new MathContext(PRECISION, ROUNDING_MODE);

    // Representing a decimal amount of BigDecimal.ZERO
    public final static BigDecimal ZERO = BigDecimal.valueOf(0, SCALE);

    private BigDecimal decimal = ZERO;
    private AMEEUnit unit = AMEEUnit.ONE;

    /**
     * A Decimal representing the supplied value and unit.
     *
     * @param decimal
     * @param unit
     */
    public Decimal(String decimal, AMEEUnit unit) {
        this(decimal);
        this.unit = unit;
    }

    /**
     * A Decimal representing the supplied unit-less value.
     *
     * @param decimal
     */
    public Decimal(String decimal) {
        if (decimal == null) throw new IllegalArgumentException("The String decimal must be non-null");

        // Many decimal DataItem values in the DB have values of "-" so we need to handle this here. 
        if (decimal.isEmpty() || decimal.equals("-")) {
            this.decimal = ZERO;
        } else {
            scale(decimal);
        }
    }

    private Decimal(BigDecimal decimal) {
        this.decimal = decimal;
    }

    private Decimal(BigDecimal decimal, AMEEUnit unit) {
        this.decimal = decimal;
        this.unit = unit;
    }

    // Scale the algorithm result according to the AMEE standard precision and scale.
    protected void scale(String decimal) {
        try {
            BigDecimal bd = new BigDecimal(decimal);
            this.decimal = bd.setScale(SCALE, ROUNDING_MODE);
            if (bd.precision() > PRECISION) {
                log.warn("scale() - precision of: " + this.decimal + " exceeds " + PRECISION);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The provided string could not be parsed as a decimal: " + decimal);
        }
    }

    /**
     * Convert and return a new Decimal instance in the target AMEEUnit.
     *
     * @param targetUnit - the target unit
     * @return the decimal in the target unit
     *
     * @see gc.carbon.domain.AMEEUnit
     */
    public Decimal convert(AMEEUnit targetUnit) {
        if (!unit.equals(targetUnit)) {
            DecimalMeasure dm = DecimalMeasure.valueOf(getValue(), unit.toUnit());
            BigDecimal converted = dm.to(targetUnit.toUnit(), Decimal.CONTEXT).getValue();
            BigDecimal scaled = converted.setScale(Decimal.SCALE, Decimal.ROUNDING_MODE);
            return new Decimal(scaled);
        } else {
            return new Decimal(getValue(), unit);
        }
    }

    /**
     * Convert and return a new Decimal instance in the target AMEEPerUnit.
     *
     * @param targetPerUnit - the target perUnit
     * @return the Decimal in the target perUnit
     *
     * @see gc.carbon.domain.AMEEPerUnit
     */
    public Decimal convert(AMEEPerUnit targetPerUnit) {

        if (!(unit instanceof AMEECompoundUnit)) return new Decimal(getValue());

        AMEECompoundUnit cUnit = (AMEECompoundUnit) unit;

        if (cUnit.hasDifferentPerUnit(targetPerUnit)) {
            DecimalMeasure dm = DecimalMeasure.valueOf(getValue(), cUnit.getPerUnit().toUnit().inverse());
            BigDecimal converted = dm.to(targetPerUnit.toUnit().inverse(), Decimal.CONTEXT).getValue();
            BigDecimal scaled = converted.setScale(Decimal.SCALE, Decimal.ROUNDING_MODE);
            return new Decimal(scaled);
        } else {
            return new Decimal(getValue());
        }
    }

    /**
     * @param unit
     * @return returns true is the supplied AMEEUnit is not considered equal to the unit of the current instance.
     *
     */
    public boolean hasDifferentUnits(AMEEUnit unit) {
        return !this.unit.equals(unit);
    }

    public BigDecimal getValue() {
        return decimal;
    }

    public AMEEUnit getUnit() {
        return unit;
    }
    
    public String toString() {
        return decimal.toString();
    }

}
