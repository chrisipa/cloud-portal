# Cloud Portal

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=cloud-portal)](https://papke.it/jenkins/job/cloud-portal/)

## Overview

Self service web portal for different Cloud platforms like Azure, AWS and VMWare vSphere.

[![Self Service Cloud Portal - Use Case #1](https://github.com/chrisipa/cloud-portal/raw/master/public/youtube.png)](https://youtu.be/NKZ46OSocp8 "Self Service Cloud Portal - Use Case #1")

## Features

* LDAP authentication & authorization
* VM provisioning to Azure, AWS and VMWare vSphere using Hashicorp's Terraform
* VM provisioning history
* Automated VM deprovisioning with terraform destroy (VM expiration dates)
* Execute bootstrap scripts for automated server configuration
* Send HTML emails with customizable velocity templates and Terraform log data as attachment
* Responsive WebUI with generic Terraform variable rendering
* Optimized RESTful WebService for CD pipeline integrations
* Dashboards showing important provisioning metrics

## Prerequisites

* LDAP server must be available
* SMTP server must be available
* [Docker](https://docs.docker.com/engine/installation/) must be installed
* [Docker-Compose](https://docs.docker.com/compose/install/) must be installed

## Usage

### Client

* See [README.md](modules/cloud-portal-client/README.md#Usage) file of cloud-portal-client module

### Server

* See [README.md](modules/cloud-portal-server/README.md#Usage) file of cloud-portal-server module