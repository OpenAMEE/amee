package com.amee.calculation.service;

import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.data.ItemDefinition;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.util.Map;

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

@Service
public class AlgorithmService {

    // The ScriptEngine for the Javscript context.
    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");

    // Default Algortithm name to use in calculations
    private static final String DEFAULT = "default";

    /**
     * Get the default algorithm for an ItemDefinition.
     *
     * @param itemDefinition
     * @return the default Algorithm for the supplied ItemDefinition
     */
    public Algorithm getAlgorithm(ItemDefinition itemDefinition) {
        return itemDefinition.getAlgorithm(DEFAULT);
    }

    /**
     * Evaluate an Algorithm.
     *
     * @param algorithm - the Algorithm to evaluate
     * @param values - map of key/value input pairs
     * @return the value returned by the Algorithm as a String
     */
    public String evaluate(Algorithm algorithm, Map<String, Object> values) {
        try {
            Bindings bindings = createBindings();
            bindings.putAll(values);
            return compile(algorithm.getContent()).eval(bindings).toString();
        } catch (ScriptException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Return the compiled Algorithm content.
    private CompiledScript compile(String content) {
        try {
            return ((Compilable) AlgorithmService.engine).compile(content);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    private static Bindings createBindings() {
        return engine.createBindings();
    }
}
