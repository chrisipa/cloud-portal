#!/bin/bash

# configuration
diskPath="/dev/sda5"
lvmPath="/dev/ubuntu-vg/root"

# get script folder
scriptPath="$(readlink -f $0)"
scriptFolder="$(dirname $(readlink -f $scriptPath))"

# resize lvm to 100% disk space
sudo "$scriptFolder/lvm-resize.sh" -p "$diskPath" -l "$lvmPath" -f