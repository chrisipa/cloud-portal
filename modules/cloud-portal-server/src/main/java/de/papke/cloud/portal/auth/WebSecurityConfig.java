package de.papke.cloud.portal.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import de.papke.cloud.portal.ldap.DirectoryAuthenticationProvider;

/**
 * Web security config class for configurating the spring boot webapp.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private DirectoryAuthenticationProvider directoryAuthenticationProvider;

    /**
     * Method for configuring the http security.
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	
    	// set directory authentication provider
    	http
    			.authenticationProvider(directoryAuthenticationProvider);

        // any request should be authenticated
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated();

        // use form based login
        http
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll();

        // disable cross site request forgery
        http
                .csrf()
                .disable();

        // disable strict transport security for frames
        http
                .headers()
                .frameOptions()
                .sameOrigin()
                .httpStrictTransportSecurity()
                .disable();
    }

    /**
     * Method for configuring the web security.
     *
     * @param web
     */
    @Override
    public void configure(WebSecurity web) {

        // ignore static resources from authentication
        web
                .ignoring()
                .antMatchers("/static/**");
    }
}
