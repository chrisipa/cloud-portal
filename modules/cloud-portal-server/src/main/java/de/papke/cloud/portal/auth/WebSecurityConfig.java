package de.papke.cloud.portal.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Web security config class for configurating the spring boot webapp.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${ldap.protocol}")
    private String ldapProtocol;

    @Value("${ldap.host}")
    private String ldapHost;

    @Value("${ldap.port}")
    private String ldapPort;

    @Value("${ldap.base.dn}")
    private String ldapBaseDn;

    @Value("${ldap.user.search.filter}")
    private String ldapUserSearchFilter;

    @Value("${ldap.group.search.filter}")
    private String ldapGroupSearchFilter;

    /**
     * Method for configuring the http security.
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

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

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .ldapAuthentication()
                .userSearchFilter(ldapUserSearchFilter)
                .groupSearchFilter(ldapGroupSearchFilter)
                .contextSource()
                .url(ldapProtocol + "://" + ldapHost + ":" + ldapPort + "/" + ldapBaseDn);
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
