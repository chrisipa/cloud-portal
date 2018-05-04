# Installation

## Ansible Installer

### Prerequisites

#### Install Ansible

* Add package repository:

```bash
sudo apt-add-repository ppa:ansible/ansible
sudo apt-get update
```

* Install ansible binary:

```bash
sudo apt-get install ansible
```

### Execute installer

* Change properties in [vars.json](../../installer/vars.json) file to fit your environment

* Call bash script:

```bash
cd installer
./install.sh
```

## Manual Setup

* Install [Docker](https://docs.docker.com/engine/installation/) and [Docker-Compose](https://docs.docker.com/compose/install/)
* See [README.md](../../modules/cloud-portal-server/README.md#Usage) file of cloud-portal-server module for additional details 