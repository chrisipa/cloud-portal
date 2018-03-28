Cloud Portal Client
===================

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=cloud-portal)](https://papke.it/jenkins/job/cloud-portal/)

Overview
--------

Docker container image for the Cloud Portal CLI client based on debian and curl.

Usage examples
--------------

* Show variable information for creating a vsphere virtual:

```bash
docker run --rm \
-e 'CLOUD_PORTAL_URL=https://my-cloud-portal-host/cloud-portal' \
-e 'CLOUD_PORTAL_USERNAME=my-user@my-domain.com' \
-e 'CLOUD_PORTAL_PASSWORD=my-password' \
-e 'CLOUD_PORTAL_PROVIDER=vsphere' \
-e 'CLOUD_PORTAL_ACTION=vm-create' \ 
chrisipa/cloud-portal-client
```

* Create a vsphere virtual machine:

```bash
docker run --rm \
-e 'CLOUD_PORTAL_URL=https://my-cloud-portal-host/cloud-portal' \
-e 'CLOUD_PORTAL_USERNAME=my-user@my-domain.com' \
-e 'CLOUD_PORTAL_PASSWORD=my-password' \
-e 'CLOUD_PORTAL_PROVIDER=vsphere' \
-e 'CLOUD_PORTAL_ACTION=vm-create' \ 
chrisipa/cloud-portal-client \
-F 'title=my-title' \
-F 'description=my-description' \
-F 'password=P@ssword1'
```

* Destroy a virtual machine:

```bash
docker run --rm \
-e 'CLOUD_PORTAL_URL=https://my-cloud-portal-host/cloud-portal' \
-e 'CLOUD_PORTAL_USERNAME=my-user@my-domain.com' \
-e 'CLOUD_PORTAL_PASSWORD=my-password' \
-e 'CLOUD_PORTAL_PROVIDER=vsphere' \
-e 'CLOUD_PORTAL_ACTION=vm-destroy' \ 
chrisipa/cloud-portal-client \
my-provisioning-id 
```