#!/bin/bash

# update package repos
sudo apt-get update

# install additional helper packages
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common

# add gpg key
sudo bash -c "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -"

# add apt sources to list
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

# update package repos again
sudo apt-get update

# install docker community edition
sudo apt-get install -y docker-ce

# allow execution of docker command without sudo
sudo gpasswd -a $(whoami) docker

# download docker-compose and make executable
sudo curl -L https://github.com/docker/compose/releases/download/1.16.1/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# download docker-compose-wrapper and make executable
sudo curl -L https://raw.githubusercontent.com/chrisipa/docker-compose-wrapper/master/docker-compose-wrapper -o /usr/local/bin/docker-compose-wrapper
sudo chmod +x /usr/local/bin/docker-compose-wrapper