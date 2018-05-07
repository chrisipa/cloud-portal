# Infrastructure

For the cloud self service portal to work we need to have some existing infrastructure tools.

## LDAP Server

* At the moment the self service portal does not have its own user directory
* That is why the application completely relies on a 3rd party LDAP server for managing user accounts and doing authentication
* The application has been tested with these LDAP servers:
  * Microsoft Active Directory (use LDAPs port 3269 from global catalog)
  * OpenLDAP
* See all configs in [application.properties](../../modules/cloud-portal-server/src/main/resources/application.properties) file starting with the **ldap** prefix

## SMTP Server

* Mail notification be turned on to show the provisioning log and the result variables like hostname etc
* To send this mails you have to configure your desired SMTP server
* See all configs in [application.properties](../../modules/cloud-portal-server/src/main/resources/application.properties) file starting with the **smtp** prefix  