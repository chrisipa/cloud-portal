#!/bin/bash

# read new hostname from command line
newHostName="$1"

# get old hostname from config file
oldHostName="$(cat /etc/hostname)"

# set new hostname in config files
sed -i "s|$oldHostName|$newHostName|g" /etc/hosts
sed -i "s|$oldHostName|$newHostName|g" /etc/hostname