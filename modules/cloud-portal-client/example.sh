#!/bin/bash

# include library
source "$(dirname $(readlink -f $0))/lib.sh"

# login 
login "$username" "$password"

# create vm 
apply "$provider"

# get variables from output
username="$(getVariableFromOutput 'username')"
host="$(getVariableFromOutput 'host')"
provisioningId="$(getVariableFromOutput 'provisioning_id')"

# execute example command
command="ls -la /tmp"
execute "$username" "$host" "$command" "$provisioningId"

# destroy vm 
destroy "$provider" "$provisioningId"

# logout 
logout

# cleanup temporary files
cleanup