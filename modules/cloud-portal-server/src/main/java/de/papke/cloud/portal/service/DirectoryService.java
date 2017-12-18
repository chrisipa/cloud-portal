package de.papke.cloud.portal.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;

import de.papke.cloud.portal.pojo.User;

@Service
public class DirectoryService {

	private static final Logger LOG = LoggerFactory.getLogger(DirectoryService.class);

	@Value("${ldap.url.string}")
	private String urlString;

	@Value("${ldap.base.dn}")
	private String baseDn;

	@Value("${ldap.principal}")
	private String principal;

	@Value("${ldap.password}")
	private String password;

	@Value("${ldap.user.search.filter}")
	private String userSearchFilter;

	@Value("${ldap.login.attribute}")
	private String loginAttribute;

	@Value("${ldap.givenname.attribute}")
	private String givenNameAttribute;

	@Value("${ldap.surname.attribute}")
	private String surNameAttribute;

	@Value("${ldap.mail.attribute}")
	private String mailAttribute;

	@Value("${ldap.group.attribute}")
	private String groupAttribute;

	@Value("${ldap.timeout}")
	private Integer timeout;

	@Value("${ldap.page.size}")
	private Integer pageSize;

	private String[] urls;

	@PostConstruct
	public void init() {
		if (StringUtils.isNoneEmpty(urlString)) {
			urls = urlString.split(",");
		}
	}

	public boolean authenticate(String username, String password) {

		boolean success = false;
		LDAPConnection ldapConnection = null;
		String loginDn = getLoginDn(username);

		if (loginDn != null) {
			try {
				ldapConnection = getUserConnection(loginDn, password);
				if (ldapConnection != null) {
					success = true;
				}
			}
			catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			finally {
				if (ldapConnection != null) {
					ldapConnection.close();
				}
			}
		}

		return success;
	}

	private String getLoginDn(String username) {

		String loginDn = null;

		try {

			Filter userFilter = Filter.create(userSearchFilter);
			Filter loginFilter = getLoginFilter(username);
			Filter filter = Filter.createANDFilter(userFilter, loginFilter);

			List<SearchResultEntry> searchResultEntries = search(filter);
			if (!searchResultEntries.isEmpty()) {
				loginDn = searchResultEntries.get(0).getDN();
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return loginDn;
	}        

	private Filter getLoginFilter(String username) {
		return Filter.createEqualityFilter(loginAttribute, username);
	}

	private LDAPConnection getUserConnection(String principal, String password) {
		return getFailoverLdapConnection(principal, password);        
	}    

	private LDAPConnection getAdminConnection() {
		return getFailoverLdapConnection(principal, password);
	}    

	private LDAPConnection getFailoverLdapConnection(String principal, String password) {
		for (String url : urls) {
			LDAPConnection ldapConnection = getLdapConnection(principal, password, url.trim());
			if (ldapConnection != null) {
				return ldapConnection;
			}
		}
		return null;
	}   

	private LDAPConnection getLdapConnection(String principal, String password, String url) {

		LDAPConnection ldapConnection = null;

		try {
			LDAPURL ldapUrl = new LDAPURL(url);
			LDAPConnectionOptions ldapConnectionOptions = new LDAPConnectionOptions();
			ldapConnectionOptions.setConnectTimeoutMillis(timeout);
			ldapConnection = new LDAPConnection(ldapConnectionOptions, ldapUrl.getHost(), ldapUrl.getPort(), principal, password);
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return ldapConnection;
	}    

	public List<SearchResultEntry> search(String filterString) throws LDAPException {
		return search(Filter.create(filterString), new String[]{});
	}

	public List<SearchResultEntry> search(Filter filter) {
		return search(filter, new String[]{});
	}

	public List<SearchResultEntry> search(String filterString, String[] attributes) throws LDAPException {
		return search(SearchScope.SUB, Filter.create(filterString), attributes);
	}

	public List<SearchResultEntry> search(Filter filter, String[] attributes) {
		return search(SearchScope.SUB, filter, attributes);
	}

	public List<SearchResultEntry> search(SearchScope searchScope, String filterString, String[] attributes) throws LDAPException {
		return search(baseDn, searchScope, Filter.create(filterString), attributes);
	}

	public List<SearchResultEntry> search(SearchScope searchScope, Filter filter, String[] attributes) {
		return search(baseDn, searchScope, filter, attributes);
	}

	public List<SearchResultEntry> search(String baseDn, SearchScope searchScope, String filterString, String[] attributes) throws LDAPException {
		return search(baseDn, searchScope, Filter.create(filterString), attributes, false);
	}

	public List<SearchResultEntry> search(String baseDn, SearchScope searchScope, Filter filter, String[] attributes) {
		return search(baseDn, searchScope, filter, attributes, false);
	}

	public List<SearchResultEntry> search(String baseDn, SearchScope searchScope, String filterString, String[] attributes, boolean paging) throws LDAPException {
		return search(baseDn, searchScope, Filter.create(filterString), attributes, paging);    
	}

	/**
	 * Helper method for executing an LDAP search request.
	 * 
	 * @param baseDn
	 * @param searchScope
	 * @param filter
	 * @param attributes
	 * @param pageSize
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	public List<SearchResultEntry> search(String baseDn, SearchScope searchScope, Filter filter, String[] attributes, boolean paging) {

		List<SearchResultEntry> searchResultEntries = new ArrayList<>();

		try {

			LDAPConnection connection = getAdminConnection();

			if (connection != null) { 

				// check if paging should be used
				if (paging) {

					// create LDAP search request
					SearchRequest searchRequest = new SearchRequest(baseDn, searchScope, filter, attributes);

					// instantiate variable for paging cookie
					ASN1OctetString cookie = null;

					do {

						// set controls for LDAP search request
						Control[] controls = new Control[1];
						controls[0] = new SimplePagedResultsControl(pageSize, cookie);
						searchRequest.setControls(controls);

						// execute LDAP search request
						SearchResult searchResult = connection.search(searchRequest);

						// add search entries from page to result list
						searchResultEntries.addAll(searchResult.getSearchEntries());

						// get cookie for next page
						cookie = null;
						for (Control control : searchResult.getResponseControls()) {
							if (control instanceof SimplePagedResultsControl) {
								SimplePagedResultsControl simplePagedResultsControl = (SimplePagedResultsControl) control; 
								cookie = simplePagedResultsControl.getCookie();
							}
						}

					} 
					// do this as long as a cookie is returned
					while ((cookie != null) && (cookie.getValueLength() > 0));
				}
				else {
					// execute LDAP search request
					SearchResult searchResult = connection.search(baseDn, searchScope, filter, attributes);

					// set search entries as result list
					searchResultEntries = searchResult.getSearchEntries();
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return searchResultEntries;
	}    

	public User getUser(String username) {

		User user = new User();

		try {

			List<SearchResultEntry> searchResultEntries = search(getLoginFilter(username));
			if (!searchResultEntries.isEmpty()) {

				SearchResultEntry searchResultEntry = searchResultEntries.get(0);

				user.setUsername(username);
				user.setGivenName(searchResultEntry.getAttributeValue(givenNameAttribute));
				user.setSurName(searchResultEntry.getAttributeValue(surNameAttribute));
				user.setEmail(searchResultEntry.getAttributeValue(mailAttribute));

				List<String> groups = new ArrayList<>();            	
				String[] groupAttributeValues = searchResultEntry.getAttributeValues(groupAttribute);
				for (String groupAttributeValue : groupAttributeValues) {
					DN groupDN = new DN(groupAttributeValue);
					String groupName = groupDN.getRDN().getAttributeValues()[0];
					groups.add(groupName);
				}
				
				user.setGroups(groups);
			}
		}
		catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return user;
	}
}
