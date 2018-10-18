# Cloud Portal Server

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=cloud-portal)](https://papke.it/jenkins/job/cloud-portal/)
[![Docker Image](https://img.shields.io/badge/docker%20image-available-blue.svg)](https://hub.docker.com/r/chrisipa/cloud-portal-server/)

## Overview

Self service web portal for different Cloud platforms like Azure, AWS and VMware vSphere.

## Usage

* Create a underprivileged user on the host system:

  ```bash
  sudo groupadd -g 1001 chrisipa
  sudo useradd -u 1001 -g 1001 -m -s /usr/sbin/nologin chrisipa
  ```

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
      - 'APPLICATION_TITLE=My Cloud Portal'
      - 'APPLICATION_ADMIN_GROUP=my-admin-group'
      - 'ENCRYPTOR_SECRET=my-encryptor-secret'
      - 'LDAP_BASE_DN=dc=my,dc=domain'
      - 'LDAP_GROUP_ATTRIBUTE=memberOf'
      - 'LDAP_LOGIN_ATTRIBUTE=userPrincipalName'
      - 'LDAP_PASSWORD=my-ldap-password'
      - 'LDAP_PRINCIPAL=cn=my-admin-user,ou=users,dc=my,dc=domain'
      - 'LDAP_URL_STRING=ldap://my-ldap-server-1:389,ldap://my-ldap-server-2:389'
      - 'LDAP_USER_SEARCH_FILTER=(objectClass=inetOrgPerson)'
      - 'MAIL_FROM=no-reply@my-domain.com'
      - 'MAIL_CC=my-cc-account@my-domain.com'
      - 'MAIL_HOST=my-mail-server'
      - 'MAIL_SEND=true'
      - 'MONGO_DB_URI=mongodb://mongodb:27017/cloud-portal'
  ```

* Run docker containers with docker compose:

  ```bash
  docker-compose up -d
  ```