#!/bin/bash

# get script folder
scriptPath="$(readlink -f $0)"
scriptFolder="$(dirname $(readlink -f $scriptPath))"

# get disk path
diskPath="$(sudo pvdisplay | grep 'PV Name' | awk '{print $3}')"

# get lvm path
lvmName="$(sudo pvdisplay | grep 'VG Name' | awk '{print $3}')"
lvmPath="$(find /dev/$lvmName -type l | head -n 1)"

# resize lvm to 100% disk space
sudo "$scriptFolder/lvm-resize.sh" -p "$diskPath" -l "$lvmPath" -f
