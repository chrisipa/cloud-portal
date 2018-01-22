#!/bin/bash

# configuration
diskPath="/dev/sda5"
lvmPath="/dev/ubuntu-vg/root"

# read parameters
username="$1"
password="$2"
publicKey="$3"

# get script folder
scriptPath="$(readlink -f $0)"
scriptFolder="$(dirname $(readlink -f $scriptPath))"

# create user
"$scriptFolder/create-user.sh" "$username" "$password" "$publicKey"

# resize lvm to 100% disk space
"$scriptFolder/lvm-resize.sh" -p "$diskPath" -l "$lvmPath" -f