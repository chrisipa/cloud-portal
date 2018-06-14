# Cloud Portal Client

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=cloud-portal)](https://papke.it/jenkins/job/cloud-portal/)
[![Docker Image](https://img.shields.io/badge/docker%20image-available-blue.svg)](https://hub.docker.com/r/chrisipa/cloud-portal-client/)

## Overview

Docker container image for the Cloud Portal CLI client based on debian and curl.

## Usage

* Show variable information for creating a vsphere virtual machine:

```bash
docker run --rm \
-e 'CLOUD_PORTAL_URL=https://my-cloud-portal-host' \
-e 'CLOUD_PORTAL_USERNAME=my-user@my-domain.com' \
-e 'CLOUD_PORTAL_PASSWORD=my-password' \
-e 'CLOUD_PORTAL_USE_CASE=vm-standalone-vsphere-terraform' \
-e 'CLOUD_PORTAL_ACTION=plan' \
chrisipa/cloud-portal-client
```

* Plan creation of a vsphere virtual machine:

```bash
docker run --rm \
-e 'CLOUD_PORTAL_URL=https://my-cloud-portal-host' \
-e 'CLOUD_PORTAL_USERNAME=my-user@my-domain.com' \
-e 'CLOUD_PORTAL_PASSWORD=my-password' \
-e 'CLOUD_PORTAL_USE_CASE=vm-standalone-vsphere-terraform' \
-e 'CLOUD_PORTAL_ACTION=plan' \
chrisipa/cloud-portal-client \
-F 'title=my-title' \
-F 'description=my-description' \
-F 'password=P@ssword1' \ 
-F 'password_repeat=P@ssword1' 
```

* Execute creation of a vsphere virtual machine:

```bash
docker run --rm \
-e 'CLOUD_PORTAL_URL=https://my-cloud-portal-host' \
-e 'CLOUD_PORTAL_USERNAME=my-user@my-domain.com' \
-e 'CLOUD_PORTAL_PASSWORD=my-password' \
-e 'CLOUD_PORTAL_USE_CASE=vm-standalone-vsphere-terraform' \
-e 'CLOUD_PORTAL_ACTION=apply' \
chrisipa/cloud-portal-client \
-F 'title=my-title' \
-F 'description=my-description' \
-F 'password=P@ssword1' \ 
-F 'password_repeat=P@ssword1' 
```

* Destroy a vsphere virtual machine:

```bash
docker run --rm \
-e 'CLOUD_PORTAL_URL=https://my-cloud-portal-host' \
-e 'CLOUD_PORTAL_USERNAME=my-user@my-domain.com' \
-e 'CLOUD_PORTAL_PASSWORD=my-password' \
-e 'CLOUD_PORTAL_USE_CASE=vm-standalone-vsphere-terraform' \
-e 'CLOUD_PORTAL_ACTION=destroy' \
chrisipa/cloud-portal-client \
my-provisioning-id
```