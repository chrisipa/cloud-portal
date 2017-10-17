package de.papke.cloud.portal.ldap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class DirectoryAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private DirectoryService directoryService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        Authentication auth = null;

        // get username and password from authentication object
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();        

        // create granted authority list
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        
        // authenticate against directory server
        if (directoryService.authenticate(username, password)) {
            
            List<String> groupList = directoryService.getGroupList(username);
            for (String group : groupList) {
                grantedAuthorityList.add(new SimpleGrantedAuthority(group));
            }
            
            auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuthorityList);
        }

        return auth;
    }

    @Override
    public boolean supports(Class<?> arg0) {
        return true;
    }
}
