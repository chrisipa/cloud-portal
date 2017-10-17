package de.papke.cloud.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class of application.
 *
 * @author Christoph Papke (info@papke.it)
 */
@SpringBootApplication
@EnableScheduling
public class Main extends SpringBootServletInitializer {

    /**
     * Method for configuring spring boot application
     *
     * @param application
     * @return
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Main.class);
    }

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args); //NOSONAR
    }
}
