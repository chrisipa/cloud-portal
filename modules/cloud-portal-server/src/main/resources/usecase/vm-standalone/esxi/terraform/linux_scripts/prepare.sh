#!/bin/bash

# get script folder
scriptPath="$(readlink -f $0)"
scriptFolder="$(dirname $(readlink -f $scriptPath))"

# make script folder excecutable
chmod -R +x "$scriptFolder"

# change hostname
sudo "$scriptFolder/hostname.sh" "$1"