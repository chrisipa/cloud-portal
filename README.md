# Cloud Portal

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=cloud-portal)](https://papke.it/jenkins/job/cloud-portal/)
[![Code Analysis](https://img.shields.io/badge/code%20analysis-available-blue.svg)](https://papke.it/sonar/overview?id=219)

## Overview

Self service web portal supporting multiple Cloud providers and use cases.

[![Self Service Cloud Portal - Use Case #1](https://github.com/chrisipa/cloud-portal/raw/master/docs/screencast/images/youtube.png)](https://youtu.be/NKZ46OSocp8 "Self Service Cloud Portal - Use Case #1")

## Providers

* [Amazon Web Services](https://aws.amazon.com)
* [Microsoft Azure](https://azure.microsoft.com)
* [VMware ESXi](https://www.vmware.com/products/vsphere-hypervisor.html)
* [VMware vSphere](https://www.vmware.com/products/vsphere.html)

## Provisioners

* [Ansible](https://www.ansible.com)
* [Terraform](https://www.terraform.io)

## Features

* LDAP authentication & authorization
* Use case provisioning with Hashicorp's Terraform 
* Provisioning history
* Automated deprovisioning with terraform destroy (by expiration dates)
* Execute bootstrap scripts for automated server configuration
* Send HTML emails with customizable velocity templates and Terraform log data as attachment
* Responsive WebUI with generic variable rendering
* Optimized RESTful WebServices and CLI client for CD pipeline integrations
* Dashboards showing important provisioning metrics
* Scripting console exposing business logic services for Groovy scripting

## Prerequisites

* [Check infrastructure requirements](docs/infrastructure/README.md)
* [Setup accounts for Cloud providers](docs/cloud-providers/README.md)
* [Install the self service portal](docs/installation/README.md)
* [Add Cloud credentials with an admin user](docs/credentials-admin/README.md)

## Usage

### Client

* See [README.md](modules/cloud-portal-client/README.md#Usage) file of cloud-portal-client module

### Server

* See [README.md](modules/cloud-portal-server/README.md#Usage) file of cloud-portal-server module