package de.papke.cloud.portal.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import de.papke.cloud.portal.pojo.User;
import de.papke.cloud.portal.service.SessionUserService;
import de.papke.cloud.portal.service.UserService;

@Component
public class DirectoryAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserService userService;

	@Autowired
	private SessionUserService sessionUserService;
    
    @Override
    public Authentication authenticate(Authentication authentication) {
        
        Authentication auth = null;

        // get username and password from authentication object
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();        

        // authenticate user
        if (userService.authenticate(username, password)) {
            
            // get user
            User user = userService.getUser(username);

            // create granted authority list
            List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
            for (String group : user.getGroups()) {
            	grantedAuthorityList.add(new SimpleGrantedAuthority(group));
            }
            
            // create authentication token
            auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuthorityList);
            
            // save user in session
            sessionUserService.setUser(user);
        }

        return auth;
    }

    @Override
    public boolean supports(Class<?> arg0) {
        return true;
    }
}
