package de.papke.cloud.portal.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@EnableMongoHttpSession(maxInactiveIntervalInSeconds = HttpSessionConfig.SESSION_TIMEOUT)
public class HttpSessionConfig {
	
	public static final int SESSION_TIMEOUT = 28800;
	
	@Bean
	public JdkMongoSessionConverter jdkMongoSessionConverter() {
		return new JdkMongoSessionConverter(); 
	}
}
