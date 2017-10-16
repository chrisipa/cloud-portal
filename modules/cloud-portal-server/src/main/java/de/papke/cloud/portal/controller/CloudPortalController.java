package de.papke.cloud.portal.controller;

import de.papke.cloud.portal.cloud.CloudProviderService;
import de.papke.cloud.portal.model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Created by chris on 16.10.17.
 */
@Controller
public class CloudPortalController {

    private static final Logger LOG = LoggerFactory.getLogger(CloudPortalController.class);

    @Value("${application.title}")
    private String applicationTitle;

    @Autowired
    private CloudProviderService cloudProviderService;

    /**
     * Method for returning the model and view for the index page.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/")
    public String index(Map<String, Object> model) throws IOException {

        // put data object into model
        model.put("self", getData());

        // return view name
        return "index";
    }

    /**
     * Method for returning the model and view for the login page.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/login")
    public String login(Map<String, Object> model) {

        // put data object into model
        model.put("self", getData());

        // return view name
        return "login";
    }

    private Data getData() {

        // create data object
        Data data = new Data();

        // set application title
        data.setApplicationTitle(applicationTitle);

        // set cloud providers
        data.setCloudProviderList(cloudProviderService.getProviderList());

        // get ldap user details
        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails != null && userDetails instanceof LdapUserDetails) {
            LdapUserDetails ldapUserDetails = (LdapUserDetails) userDetails;
            data.setUsername(ldapUserDetails.getUsername());
        }

        return data;
    }
}
