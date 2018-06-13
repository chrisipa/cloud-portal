package de.papke.cloud.portal.service;

import java.io.PrintWriter;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.papke.cloud.portal.helper.ScriptHelper;

/**
 * Groovy service class for executing groovy scripts
 */
@Component
public class GroovyService {

    private static final Logger LOG = LoggerFactory.getLogger(GroovyService.class);
    private static final ScriptEngine SCRIPT_ENGINE = new GroovyScriptEngineImpl();
    private static final String SCRIPT_VAR_SCRIPT = "script";

    /**
     * Method for executing a groovy script.
     *
     * @param script
     * @param variableMap
     * @return
     */
    public void execute(String script, Map<String, Object> variableMap, PrintWriter out) {

        try {

            // create script helper
            ScriptHelper scriptHelper = new ScriptHelper(out);

            // create bindings
            Bindings bindings = SCRIPT_ENGINE.createBindings();
            bindings.putAll(variableMap);
            bindings.put(SCRIPT_VAR_SCRIPT, scriptHelper);

            // create script context
            SimpleScriptContext context = new SimpleScriptContext();
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            context.setWriter(out);
            context.setErrorWriter(out);

            // execute groovy script
            Object result = SCRIPT_ENGINE.eval(script, context);

            // print result if not null
            if (result != null) {
                scriptHelper.print(result);
            }
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
            e.printStackTrace(out);
        }
    }
}
