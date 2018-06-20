# Infrastructure

For the cloud self service portal to work we need to have some existing infrastructure tools.

## LDAP Server

#### Introduction

* At the moment the self service portal does not have its own user directory
* That is why the application completely relies on a 3rd party LDAP server for managing user accounts and doing authentication
* The application has been tested with these LDAP servers:
  * Microsoft Active Directory (use LDAPs port **3269** from global catalog)
  * OpenLDAP
* See all configs in [application.properties](../../modules/cloud-portal-server/src/main/resources/application.properties) file starting with the **ldap** prefix

#### Example

For testing purposes you can use the public LDAP server of the company [Forum Systems](http://www.forumsys.com/homepage/):

[http://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/](http://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/)

For this LDAP server the config in the [application.properties](../../modules/cloud-portal-server/src/main/resources/application.properties) file would look like this:

```bash
APPLICATION_ADMIN_GROUP=scientists
LDAP_URL_STRING=ldap://ldap.forumsys.com:389
LDAP_BASE_DN=dc=example,dc=com
LDAP_PRINCIPAL=cn=read-only-admin,dc=example,dc=com
LDAP_PASSWORD=password
LDAP_USER_SEARCH_FILTER=(objectClass=inetOrgPerson)
LDAP_LOGIN_ATTRIBUTE=uid
LDAP_GIVENNAME_ATTRIBUTE=givenName
LDAP_SURNAME_ATTRIBUTE=sn
LDAP_DISPLAYNAME_ATTRIBUTE=cn
LDAP_MAIL_ATTRIBUTE=mail
LDAP_GROUP_ATTRIBUTE=
LDAP_MEMBER_ATTRIBUTE=uniqueMember
LDAP_TIMEOUT=3000
LDAP_PAGE_SIZE=1000
```

Now you should be able to login to the cloud portal with these credentials:

* Username: **einstein**
* Password: **password**

As the **einstein** user is member of the **scientists** group, you can now start to create credentials for your favorite cloud provider with the [Credentials Admin](../credentials-admin/README.md).

## SMTP Server

* Mail notification can be turned on to show the provisioning log and the result variables like hostname etc
* To send these mails you have to configure your desired SMTP server
* See all configs in [application.properties](../../modules/cloud-portal-server/src/main/resources/application.properties) file starting with the **mail** prefix  