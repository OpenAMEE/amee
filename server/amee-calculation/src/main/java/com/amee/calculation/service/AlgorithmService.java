package com.amee.calculation.service;

import com.amee.core.ThreadBeanHolder;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.data.ItemDefinition;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import sun.org.mozilla.javascript.internal.JavaScriptException;
import sun.org.mozilla.javascript.internal.NativeJavaObject;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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

    private final Log scienceLog = LogFactory.getLog("science");

    // The ScriptEngine for the JavaScript context.
    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");

    // Default Algorithm name to use in calculations
    private final static String DEFAULT = "default";

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
     * @param values    - map of key/value input pairs
     * @return the value returned by the Algorithm as a String
     * @throws ScriptException - re-throws exception generated by script execution
     */
    public String evaluate(Algorithm algorithm, Map<String, Object> values) throws ScriptException {
        Bindings bindings = createBindings();
        bindings.putAll(values);
        bindings.put("logger", scienceLog);
        Object result = getCompiledScript(algorithm).eval(bindings);
        if (result != null) {
            return result.toString();
        } else {
            throw new CalculationException(
                    "Algorithm result is null (" +
                            algorithm.getItemDefinition().getName() +
                            ", " +
                            algorithm.getName() +
                            ").");
        }
    }

    private Bindings createBindings() {
        return engine.createBindings();
    }

    /**
     * Return a CompiledScript based on the content of the Algorithm. Will throw a RuntimeException if
     * the Algorithm content is blank or if the CompiledScript is null. Will cache the CompiledScript
     * in the thread.
     *
     * @param algorithm to create CompiledScript from
     * @return the CompiledScript
     * @throws ScriptException
     */
    private CompiledScript getCompiledScript(Algorithm algorithm) throws ScriptException {
        CompiledScript compiledScript = (CompiledScript) ThreadBeanHolder.get("algorithm-" + algorithm.getUid());
        if (compiledScript == null) {
            if (StringUtils.isBlank(algorithm.getContent())) {
                throw new CalculationException(
                        "Algorithm content is null (" +
                                algorithm.getItemDefinition().getName() +
                                ", " +
                                algorithm.getName() +
                                ").");
            }
            compiledScript = ((Compilable) engine).compile(algorithm.getContent());
            if (compiledScript == null) {
                throw new CalculationException(
                        "CompiledScript is null (" +
                                algorithm.getItemDefinition().getName() +
                                ", " +
                                algorithm.getName() +
                                ").");
            }
            ThreadBeanHolder.set("algorithm-" + algorithm.getUid(), compiledScript);
        }
        return compiledScript;
    }

    /**
     * Returns an IllegalArgumentException that is wrapped in a ScriptException.
     *
     * @param e ScriptException to look within
     * @return an IllegalArgumentException instance or null
     */
    public static IllegalArgumentException getIllegalArgumentException(ScriptException e) {

        // Must have a cause.
        if (e.getCause() == null) {
            return null;
        }

        // If cause is an IllegalArgumentException return that.
        if (e.getCause() instanceof IllegalArgumentException) {
            return (IllegalArgumentException) e.getCause();
        }

        // Is there a wrapped JavaScriptException?
        if (!(e.getCause() instanceof JavaScriptException)) {
            return null;
        }

        // Now switch to working with wrapped JavaScriptException.
        JavaScriptException jse = (JavaScriptException) e.getCause();

        // JavaScriptException must have a value.
        if (jse.getValue() == null) {
            return null;
        }

        // Get value in which the IllegalArgumentException may be wrapped.
        Object value = jse.getValue();

        // Unwrap NativeError to NativeJavaObject first, if possible.
        if (value instanceof Scriptable) {
            Object njo = ScriptableObject.getProperty(((Scriptable) value), "rhinoException");
            if (njo instanceof NativeJavaObject) {
                value = njo;
            } else {
                njo = ScriptableObject.getProperty(((Scriptable) value), "javaException");
                if (njo instanceof NativeJavaObject) {
                    value = njo;
                }
            }
        }

        // If value is a NativeJavaObject, unwrap to the Java object.
        if (value instanceof NativeJavaObject) {
            value = ((NativeJavaObject) value).unwrap();
        }

        // Did we find an IllegalArgumentException?
        if (value instanceof IllegalArgumentException) {
            return (IllegalArgumentException) value;
        } else {
            return null;
        }
    }
}