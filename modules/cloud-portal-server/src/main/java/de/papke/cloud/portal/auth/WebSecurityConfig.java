package de.papke.cloud.portal.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String PATTERN_LOGIN = "/login";
	private static final String PATTERN_STATIC_FILES = "/static/**";
    
	@Autowired
    private DirectoryAuthenticationProvider directoryAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // set directory authentication provider
        http.authenticationProvider(directoryAuthenticationProvider);

        // any request should be authenticated
        http.authorizeRequests().anyRequest().authenticated();

        // use form based login
        http.formLogin().loginPage(PATTERN_LOGIN).permitAll().and().logout().permitAll();

        // disable cross site request forgery
        http.csrf().disable();

        // disable strict transport security for frames
        http.headers().frameOptions().sameOrigin().httpStrictTransportSecurity().disable();
    }

    @Override
    public void configure(WebSecurity web) {

        // ignore static resources from authentication
        web.ignoring().antMatchers(PATTERN_STATIC_FILES);
    }
}