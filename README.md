Cloud Portal
============

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=cloud-portal)](https://papke.it/jenkins/job/cloud-portal/)
[![Code Analysis](https://img.shields.io/badge/code%20analysis-available-blue.svg)](https://papke.it/sonar/overview?id=219)
[![Docker Image](https://img.shields.io/badge/docker%20image-available-blue.svg)](https://hub.docker.com/r/chrisipa/cloud-portal/)

Overview
--------
Self service web portal for different Cloud platforms like Azure, AWS and VMWare vSphere.

[![Self Service Cloud Portal - Use Case #1](https://github.com/chrisipa/cloud-portal/raw/master/public/youtube.png)](https://youtu.be/NKZ46OSocp8 "Self Service Cloud Portal - Use Case #1")

Features (working)
------------------
* LDAP authentication & authorization
* VM provisioning to Azure, AWS and VMWare vSphere using Hashicorp's Terraform
* VM provisioning history
* Execute bootstrap scripts for automated server configuration
* Send HTML emails with customizable velocity templates and Terraform log data as attachment
* Responsive WebUI with generic Terraform variable rendering

Features (planned)
------------------
* Automated VM decommisioning with terraform destroy (VM expiration dates)
* Optimized RESTful WebService for CD pipeline integrations
* Additional workflow actions after VM provisioning
* Support of additional use cases

Prerequisites
-------------
* LDAP server must be available
* SMTP server must be available
* [Docker](https://docs.docker.com/engine/installation/) must be installed
* [Docker-Compose](https://docs.docker.com/compose/install/) must be installed

Usage
-----
1. Create docker compose file `docker-compose.yml` with your configuration data:
  ```yml
mongodb:
  image: mongo:3.4.10
  volumes:
    - /opt/cloud-portal/data:/data/db

tomcat:
  image: chrisipa/cloud-portal:latest
  links: 
    - mongodb
  ports:
    - 80:8080
    - 443:8443
  volumes:
    - /opt/cloud-portal/logs:/opt/tomcat/logs
  environment:    
    - 'TOMCAT_SESSION_TIMEOUT=720'
    - 'application.title=My Cloud Portal'
    - 'application.admin.group=my-admin-group'
    - 'encryptor.secret=my-encryptor-secret'
    - 'ldap.base.dn=dc=my,dc=domain'
    - 'ldap.group.attribute=memberOf'
    - 'ldap.login.attribute=userPrincipalName'
    - 'ldap.password=my-ldap-password'
    - 'ldap.principal=cn=my-admin-user,ou=users,dc=my,dc=domain'
    - 'ldap.url.string=ldap://my-ldap-server-1:389,ldap://my-ldap-server-2:389'
    - 'ldap.user.search.filter=(objectClass=inetOrgPerson)'
    - 'mail.from=no-reply@my-domain.com'
    - 'mail.host=my-mail-server'
    - 'mail.send=true'
    - 'spring.data.mongodb.uri=mongodb://mongodb:27017/cloud-portal'
  ```

2. Run docker containers with docker compose:
  ```
  docker-compose up -d
  ```
