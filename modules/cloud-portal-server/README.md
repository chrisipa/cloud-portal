# Cloud Portal Server

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=cloud-portal)](https://papke.it/jenkins/job/cloud-portal/)
[![Code Analysis](https://img.shields.io/badge/code%20analysis-available-blue.svg)](https://papke.it/sonar/overview?id=219)
[![Docker Image](https://img.shields.io/badge/docker%20image-available-blue.svg)](https://hub.docker.com/r/chrisipa/cloud-portal-server/)

## Overview

Self service web portal for different Cloud platforms like Azure, AWS and VMWare vSphere.

## Usage

* Create docker compose file `docker-compose.yml` with your configuration data:

  ```yml
  mongodb:
    image: mongo:3.4.10
    volumes:
      - /opt/cloud-portal/data:/data/db

  tomcat:
    image: chrisipa/cloud-portal-server:latest
    links:
      - mongodb
    ports:
      - 80:8080
      - 443:8443
    volumes:
      - /opt/cloud-portal/logs:/opt/tomcat/logs
    environment:
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
      - 'mail.cc=my-cc-account@my-domain.com'
      - 'mail.host=my-mail-server'
      - 'mail.send=true'
      - 'spring.data.mongodb.uri=mongodb://mongodb:27017/cloud-portal'
  ```

* Run docker containers with docker compose:

  ```bash
  docker-compose up -d
  ```