package de.papke.cloud.portal.service;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Service class for evaluating velocity code.
 *
 * @author Christoph Papke (info@papke.it)
 */
@Service
public class VelocityService {

    private static final Logger LOG = LoggerFactory.getLogger(VelocityService.class);

    @Value("${velocity.encoding}")
    private String encoding;

    private VelocityEngine velocityEngine;

    /**
     * Method for initializing the velocity engine.
     */
    @PostConstruct
    public void init() {

        // create velocity engine
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    /**
     * Method for evaluating velocity code.
     *
     * @param templatePath - path to velocity template
     * @param variableMap - variable map for substitution
     * @return output of velocity code
     */
    public String evaluate(String templatePath, Map<String, Object> variableMap) {

        String output = null;

        try {

            // logging
            LOG.info("Evaluating velocity template with path '{}' and variable map '{}'", templatePath, variableMap);

            // create velocity context and string writer
            VelocityContext velocityContext = new VelocityContext(variableMap);
            StringWriter stringWriter = new StringWriter();

            // evaluate velocity code
            velocityEngine.mergeTemplate(templatePath, encoding, velocityContext, stringWriter);

            // get string
            output = stringWriter.toString();

            // close string writer
            stringWriter.close();
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return output;
    }
}