#!/bin/bash

# read parameters
username="$1"
password="$2"
publicKey="$3"

# update package repositories
apt-get update 

# install mkpasswd by installing whois package
apt-get install -y whois

# create new linux user
useradd -p "$(mkpasswd --hash=md5 $password)" -s "/bin/bash" "$username"

# add sudo group to user
usermod -aG sudo "$username"

# create home folder 
homeFolder="/home/$username"
mkdir -p "$homeFolder"
chown "$username.$username" "$homeFolder"  

# add public key for authentication
sshFolder="$homeFolder/.ssh"
mkdir "$sshFolder"
echo "$publicKey" >> "$sshFolder/authorized_keys" 