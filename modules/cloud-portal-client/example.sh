#!/bin/bash

# include library
source "$(dirname $(readlink -f $0))/lib.sh"

# login 
login 

# create vm 
apply "$@"

# get variables from output
username="$(getVariableFromOutput 'username')"
host="$(getVariableFromOutput 'host')"
provisioningId="$(getVariableFromOutput 'provisioning_id')"

# execute example command
command="ls -la /tmp"
execute "$username" "$host" "$command" "$provisioningId"

# destroy vm if user decides
if confirm "Do you want to remove this VM?"
then
    # destroy vm 
    destroy "$provisioningId"
fi

# logout 
logout

# cleanup temporary files
cleanup