#!/bin/bash

# read parameters
username="$1"
password="$2"
publicKey="$3"

# get script folder
scriptPath="$(readlink -f $0)"
scriptFolder="$(dirname $(readlink -f $scriptPath))"

# make script folder excecutable
chmod -R +x "$scriptFolder"

# create user
"$scriptFolder/create-user.sh" "$username" "$password" "$publicKey"